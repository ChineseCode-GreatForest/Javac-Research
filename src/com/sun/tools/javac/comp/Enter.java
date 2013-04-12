/*
 * @(#)Enter.java	1.136 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.comp;

import java.util.*;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This class enters symbols for all encountered definitions into
 *  the symbol table. The pass consists of two phases, organized as
 *  follows:
 *
 *  <p>In the first phase, all class symbols are intered into their
 *  enclosing scope, descending recursively down the tree for classes
 *  which are members of other classes. The class symbols are given a
 *  MemberEnter object as completer.
 *
 *  <p>In the second phase classes are completed using
 *  MemberEnter.complete().  Completion might occur on demand, but
 *  any classes that are not completed that way will be eventually
 *  completed by processing the `uncompleted' queue.  Completion
 *  entails (1) determination of a class's parameters, supertype and
 *  interfaces, as well as (2) entering all symbols defined in the
 *  class into its scope, with the exception of class symbols which
 *  have been entered in phase 1.  (2) depends on (1) having been
 *  completed for a class and all its superclasses and enclosing
 *  classes. That's why, after doing (1), we put classes in a
 *  `halfcompleted' queue. Only when we have performed (1) for a class
 *  and all it's superclasses and enclosing classes, we proceed to
 *  (2).
 *
 *  <p>Whereas the first phase is organized as a sweep through all
 *  compiled syntax trees, the second phase is demand. Members of a
 *  class are entered when the contents of a class are first
 *  accessed. This is accomplished by installing completer objects in
 *  class symbols for compiled classes which invoke the member-enter
 *  phase for the corresponding class tree.
 *
 *  <p>Classes migrate from one phase to the next via queues:
 *
 *  <pre>
 *  class enter -> (Enter.uncompleted)         --> member enter (1)
 *		-> (MemberEnter.halfcompleted) --> member enter (2)
 *		-> (Todo)	               --> attribute
 *						(only for toplevel classes)
 *  </pre>
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Enter.java	1.136 07/03/21")
public class Enter extends JCTree.Visitor {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Enter);//�Ҽ��ϵ�
	
    protected static final Context.Key<Enter> enterKey =
	new Context.Key<Enter>();

    Log log;
    Symtab syms;
    Check chk;
    TreeMaker make;
    ClassReader reader;
    Annotate annotate;
    MemberEnter memberEnter;
    Lint lint;
    JavaFileManager fileManager;

    private final Todo todo;
    
    private final Name.Table names;//�Ҽ��ϵ�

    public static Enter instance(Context context) {
		Enter instance = context.get(enterKey);
		if (instance == null)
			instance = new Enter(context);
		return instance;
    }

    protected Enter(Context context) {
		DEBUG.P(this,"Enter(1)");
		context.put(enterKey, this);

		log = Log.instance(context);
		reader = ClassReader.instance(context);
		make = TreeMaker.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
		memberEnter = MemberEnter.instance(context);
		annotate = Annotate.instance(context);
		lint = Lint.instance(context);

		predefClassDef = make.ClassDef(
			make.Modifiers(PUBLIC),
			syms.predefClass.name, null, null, null, null);
		//predefClass��һ��ClassSymbol(PUBLIC|ACYCLIC, names.empty, rootPackage)
		//������Scope members_field���г�Ա(�����������ͷ���(symbols for basic types)������������)
		//��ο�Systab���predefClass�ֶ�˵��
		predefClassDef.sym = syms.predefClass;

		todo = Todo.instance(context);
		fileManager = context.get(JavaFileManager.class);
		
		names = Name.Table.instance(context);    //�Ҽ��ϵ�
		DEBUG.P(0,this,"Enter(1)");
    }

    /** A hashtable mapping classes and packages to the environments current
     *  at the points of their definitions.
     */
    Map<TypeSymbol,Env<AttrContext>> typeEnvs =
	    new HashMap<TypeSymbol,Env<AttrContext>>();

    /** Accessor for typeEnvs
     */
    public Env<AttrContext> getEnv(TypeSymbol sym) {
		return typeEnvs.get(sym);
    }
    
    public Env<AttrContext> getClassEnv(TypeSymbol sym) {
        Env<AttrContext> localEnv = getEnv(sym);
        Env<AttrContext> lintEnv = localEnv;
        //lint��AttrContext�ж���
        while (lintEnv.info.lint == null)
            lintEnv = lintEnv.next;
        localEnv.info.lint = lintEnv.info.lint.augment(sym.attributes_field, sym.flags());
        return localEnv;
    }

    /** The queue of all classes that might still need to be completed;
     *	saved and initialized by main().
     */
    ListBuffer<ClassSymbol> uncompleted;//����ֵ��Enter��Ӧ��visitXXX()������

    /** A dummy class to serve as enclClass for toplevel environments.
     */
    private JCClassDecl predefClassDef;

/* ************************************************************************
 * environment construction
 *************************************************************************/


    /** Create a fresh environment for class bodies.
     *	This will create a fresh scope for local symbols of a class, referred
     *	to by the environments info.scope field.
     *	This scope will contain
     *	  - symbols for this and super
     *	  - symbols for any type parameters
     *	In addition, it serves as an anchor for scopes of methods and initializers
     *	which are nested in this scope via Scope.dup().
     *	This scope should not be confused with the members scope of a class.
     *
     *	@param tree	The class definition.
     *	@param env	The environment current outside of the class definition.
     */
     
    //�ڲ��಻����JCCompilationUnit(topLevelEnv),��ֻ����JCClassDecl(classEnv)
    public Env<AttrContext> classEnv(JCClassDecl tree, Env<AttrContext> env) {
		DEBUG.P(this,"classEnv(2)");
    	DEBUG.P("env="+env);
		Env<AttrContext> localEnv =
			env.dup(tree, env.info.dup(new Scope(tree.sym)));
		localEnv.enclClass = tree;
		localEnv.outer = env;
		localEnv.info.isSelfCall = false;
		localEnv.info.lint = null; // leave this to be filled in by Attr, 
								   // when annotations have been processed
		DEBUG.P("localEnv="+localEnv);
		DEBUG.P(0,this,"classEnv(2)");
		return localEnv;
    }

    /** Create a fresh environment for toplevels.
     *	@param tree	The toplevel tree.
     */
    Env<AttrContext> topLevelEnv(JCCompilationUnit tree) {
		Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
		localEnv.toplevel = tree;
		localEnv.enclClass = predefClassDef;
		tree.namedImportScope = new Scope.ImportScope(tree.packge);
		tree.starImportScope = new Scope.ImportScope(tree.packge);
		localEnv.info.scope = tree.namedImportScope;//ע������
		
		//����Scope[]
		//DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
		//DEBUG.P("tree.starImportScope="+tree.starImportScope);
		//DEBUG.P("localEnv.info.scope="+localEnv.info.scope);
			
		localEnv.info.lint = lint;
		return localEnv;
    } 

    public Env<AttrContext> getTopLevelEnv(JCCompilationUnit tree) {
		Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
		localEnv.toplevel = tree;
		localEnv.enclClass = predefClassDef;
		localEnv.info.scope = tree.namedImportScope;
		localEnv.info.lint = lint;
		return localEnv;
    }

    /** The scope in which a member definition in environment env is to be entered
     *	This is usually the environment's scope, except for class environments,
     *	where the local scope is for type variables, and the this and super symbol
     *	only, and members go into the class member scope.
     */
    Scope enterScope(Env<AttrContext> env) {
		try {
    	DEBUG.P(this,"enterScope(1)");
		if((env.tree.tag == JCTree.CLASSDEF))
    		DEBUG.P("ѡ���Scope�� "+((JCClassDecl) env.tree).sym+" JCClassDecl.sym.members_field)");
		else
			DEBUG.P("ѡ���Scope�� env.info.scope ӵ������"+env.info.scope.owner);

		return (env.tree.tag == JCTree.CLASSDEF)
			? ((JCClassDecl) env.tree).sym.members_field
			: env.info.scope;


		} finally {
    	DEBUG.P(0,this,"enterScope(1)");
    	}
    }

/* ************************************************************************
 * Visitor methods for phase 1: class enter
 *************************************************************************/

    /** Visitor argument: the current environment.
     */
    protected Env<AttrContext> env;

    /** Visitor result: the computed type.
     */
    Type result;//����ֵ��Enter��Ӧ��visitXXX()������

    /** Visitor method: enter all classes in given tree, catching any
     *	completion failure exceptions. Return the tree's type.
     *
     *	@param tree    The tree to be visited.
     *	@param env     The environment visitor argument.
     */
    Type classEnter(JCTree tree, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(JCTree tree, Env<AttrContext> env)");
		//Enter��ֻ��JCCompilationUnit��JCClassDecl��JCTypeParameter��������������visitXXX()����
		//�����������ֻ��һ��Ĭ�ϵ�visitTree(��д�˳���JCTree.Visitor��visitTree)
		DEBUG.P("tree.tag="+tree.myTreeTag());
		Env<AttrContext> prevEnv = this.env;
		DEBUG.P("��ǰEnv="+prevEnv);
		DEBUG.P("��ǰEnv="+env);
		try {
			this.env = env;
			//����JCTree�������accept(Visitor v),�����е�Visitor��Enter���,
			//��JCTree�������accept(Visitor v)�ڲ��ص�Enter�ж�Ӧ��visitXXX()
			tree.accept(this);
			return result;
		}  catch (CompletionFailure ex) {//��ȫ�޶�����:com.sun.tools.javac.code.Symbol.CompletionFailure
			return chk.completionError(tree.pos(), ex);
		} finally {
			DEBUG.P(1,this,"classEnter(JCTree tree, Env<AttrContext> env)");
			this.env = prevEnv;
		}
    }

    /** Visitor method: enter classes of a list of trees, returning a list of types.
     */
    <T extends JCTree> List<Type> classEnter(List<T> trees, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(2)");
		DEBUG.P("List<T> trees.size()="+trees.size());
		ListBuffer<Type> ts = new ListBuffer<Type>();
		for (List<T> l = trees; l.nonEmpty(); l = l.tail)
			ts.append(classEnter(l.head, env));
		DEBUG.P(2,this,"classEnter(2)");
		return ts.toList();
    }



    public void visitTopLevel(JCCompilationUnit tree) {
		JavaFileObject prev = log.useSource(tree.sourcefile);
		DEBUG.P(this,"visitTopLevel(1)");
		//��û�н��е�Enter�׶ε�ʱ��JCCompilationUnit��PackageSymbol packge
		//��null����Ҳ˵����:Parser�ĺ����׶ε��������������JCTree�С��������ݡ�
		DEBUG.P("JCCompilationUnit tree.sourcefile="+tree.sourcefile);
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
        DEBUG.P("JCCompilationUnit tree.pid="+tree.pid);

		boolean addEnv = false;
		
		//DEBUG.P("JCCompilationUnit tree.sourcefile.className="+tree.sourcefile.getClass().getName());
		//���һ����:com.sun.tools.javac.util.JavacFileManager$RegularFileObject
		//JavacFileManager.RegularFileObject, JavacFileManager.ZipFileObject��ʵ����
		//JavaFileObject�ӿ�
		
		//���JCCompilationUnit tree.sourcefile���ļ����Ƿ���package-info.java
		boolean isPkgInfo = tree.sourcefile.isNameCompatible("package-info",
									 JavaFileObject.Kind.SOURCE);
		DEBUG.P("isPkgInfo="+isPkgInfo);

		//tree.pid��Դ�ļ����ڰ���ȫ��					     
		if (tree.pid != null) {
				//��ִ����TreeInfo.fullName(tree.pid)�󣬽�����һ�������İ���������
				//�����Name.Table��
				//(ע:���������:my.test,��Name.Table�л�������name:(my),(test)��(my.test)
				//����һ��ֻ�����javac��ִ���ٶ�
				//DEBUG.P(names.myNames());
			tree.packge = reader.enterPackage(TreeInfo.fullName(tree.pid));
			//DEBUG.P(names.myNames());
			DEBUG.P("tree.packageAnnotations="+tree.packageAnnotations);
			if (tree.packageAnnotations.nonEmpty()) {
					if (isPkgInfo) {
						addEnv = true;
					} else {
						//ֻ��package-info.java�����а�ע��
						//�ο�:Parser.compilationUnit()
						log.error(tree.packageAnnotations.head.pos(),
								  "pkg.annotations.sb.in.package-info.java");
					}
			}
		} else {
				//Դ�ļ�δ��������package�����
			tree.packge = syms.unnamedPackage;
		}
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
		DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		
		/*
		complete()��com.sun.tools.javac.code.Symbol����
		tree.packge��com.sun.tools.javac.code.Symbol.PackageSymbol��ʵ������
		com.sun.tools.javac.jvm.ClassReaderʵ����com.sun.tools.javac.code.Symbol.Completer�ӿ�
		����Symbol.complete()��ͨ��Symbol.Completer completer(��ClassReader��enterPackage�����и�ֵ)
		��ӵ���ClassReader��complete(Symbol sym)����
		
		���ù���:com.sun.tools.javac.code.Symbol::complete()==>
				 com.sun.tools.javac.jvm.ClassReader::complete(1)

		��ûִ��complete()ǰ����ִ���������enterPackage�󣬵õ���һ��
		PackageSymbol�������PackageSymbol��Scope members_field��null�ģ�
		ִ��complete()��Ŀ�ľ���Ϊ���ҳ�PackageSymbol����ʾ�İ����е�
		�������ļ���������Щ���ļ�����װ����һ��ClassSymbol����members_field
		*/

		//��Ȼcomplete()�����׳�CompletionFailure��
		//����ΪCompletionFailure��RuntimeException�����࣬
		//������visitTopLevel�˷����п��Բ�����
		tree.packge.complete(); // Find all classes in package.

		//��ԱҲ�п�����δ�����.java�ļ�
		//����ļ���Package-Info1.java��
		//����Ϊ"-"������ClassReader�ķ���fillIn(3)�е�SourceVersion.isIdentifier(simpleName)�������˵���
		//�����ļ�Package-Info1.java��ClassReader�ķ���includeClassFile(2)�б�����tree.packge.package_info�������Ǽ���tree.packge.members_field
		DEBUG.P(3);
		DEBUG.P(tree.packge+"���е����г�Աװ�����(Enter)");
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
        DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		DEBUG.P(3);

        Env<AttrContext> env = topLevelEnv(tree);

		// Save environment of package-info.java file.
		if (isPkgInfo) {
			Env<AttrContext> env0 = typeEnvs.get(tree.packge);
			if (env0 == null) {
				typeEnvs.put(tree.packge, env);
			} else {
				JCCompilationUnit tree0 = env0.toplevel;
				if (!fileManager.isSameFile(tree.sourcefile, tree0.sourcefile)) {
					/* ��ͬʱ���������ڲ�ͬĿ¼��ͬ��package-info.java�ļ�ʱ��
					���������package-info.java�����ݶ�����ͬ�İ��磬:package test.enter;
					��ᷢ��"���棺[package-info] ���ҵ������ test.enter �� package-info.java �ļ�"
					//test\enter\package-info.java
					package test.enter;
					//test\enter\package-info.java
					package test.enter;
					*/
					log.warning(tree.pid != null ? tree.pid.pos()
								: null,
								"pkg-info.already.seen",
								tree.packge);
					if (addEnv || (tree0.packageAnnotations.isEmpty() &&
						   tree.docComments != null &&
						   tree.docComments.get(tree) != null)) {
						typeEnvs.put(tree.packge, env);
					}
				}
			}
		}

		classEnter(tree.defs, env);
        if (addEnv) {//��ע�ʹ�����
            todo.append(env);
        }
		log.useSource(prev);
		result = null;
	
	/*******************���¶��Ǵ�ӡ��Ϣ�����(������;)********************/
        DEBUG.P(2);
        DEBUG.P("***��һ�׶�Enter���***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("����: "+tree.packge);
        DEBUG.P("--------------------------");
        DEBUG.P("tree.packge.members_field: "+tree.packge.members_field);
        DEBUG.P("tree.namedImportScope    : "+tree.namedImportScope);
        DEBUG.P("tree.starImportScope     : "+tree.starImportScope);
        DEBUG.P("");
        
        //ListBuffer<ClassSymbol> uncompleted
        DEBUG.P("�ȴ�������������: "+uncompleted.size());
        DEBUG.P("--------------------------");
        for(ClassSymbol myClassSymbol:uncompleted) {
        	DEBUG.P("����             : "+myClassSymbol);
        	DEBUG.P("members_field    : "+myClassSymbol.members_field);
        	DEBUG.P("flags            : "+Flags.toString(myClassSymbol.flags_field));
        	DEBUG.P("sourcefile       : "+myClassSymbol.sourcefile);
        	DEBUG.P("classfile        : "+myClassSymbol.classfile);
        	DEBUG.P("completer        : "+myClassSymbol.completer);
        	ClassType myClassType=(ClassType)myClassSymbol.type;
        	DEBUG.P("type             : "+myClassType);
        	DEBUG.P("outer_field      : "+myClassType.getEnclosingType());
        	DEBUG.P("supertype_field  : "+myClassType.supertype_field);
        	DEBUG.P("interfaces_field : "+myClassType.interfaces_field);
        	DEBUG.P("typarams_field   : "+myClassType.typarams_field);
        	DEBUG.P("allparams_field  : "+myClassType.allparams_field);
        	DEBUG.P("");
        }
        DEBUG.P("");
        DEBUG.P("Env����: "+typeEnvs.size());
        DEBUG.P("--------------------------");
        for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
        	DEBUG.P(""+myMapEntry);
        DEBUG.P(2);
        
        DEBUG.P("Todo����: "+todo.size());
        DEBUG.P("--------------------------");
        for(List<Env<AttrContext>> l=todo.toList();l.nonEmpty();l=l.tail)
        	DEBUG.P(""+l.head);
        DEBUG.P(2);
        
    	DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
        DEBUG.P(2);
		DEBUG.P(2,this,"visitTopLevel(1)");
	//
    }





    public void visitClassDef(JCClassDecl tree) {
        DEBUG.P(this,"visitClassDef(1)");
        //��û�н��е�Enter�׶ε�ʱ��JCClassDecl��ClassSymbol sym
		//��null����Ҳ˵����:Parser�ĺ����׶ε��������������JCTree�С��������ݡ�
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.name="+tree.name);
		
		Symbol owner = env.info.scope.owner;
		Scope enclScope = enterScope(env);
		ClassSymbol c;
		
		DEBUG.P("Symbol owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("Symbol owner="+owner);
		/*
		ע��Scope enclScope��
		JCCompilationUnit.PackageSymbol packge.members_field�Ĳ��
		Scope enclScope�п�����ָ��JCCompilationUnit.namedImportScope(�ο�topLevelEnv())
		������������������:Scope enclScope=Scope[]
		*/
		DEBUG.P("Scope enclScope="+enclScope);
		if (owner.kind == PCK) {
				// <editor-fold defaultstate="collapsed">
			// We are seeing a toplevel class.
			PackageSymbol packge = (PackageSymbol)owner;
			//һ����ClassReader.includeClassFile()�������
			DEBUG.P("PackageSymbol packge.flags_field(1)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			for (Symbol q = packge; q != null && q.kind == PCK; q = q.owner)
			q.flags_field |= EXISTS;//EXISTS��com.sun.tools.javac.code.Flags
			
				DEBUG.P("PackageSymbol packge.name="+packge);
				DEBUG.P("PackageSymbol packge.flags_field(2)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			
				//JCClassDecl.nameֻ��һ���򵥵�����(��������)
			c = reader.enterClass(tree.name, packge);
			
			DEBUG.P("packge.members()ǰ="+packge.members());
			packge.members().enterIfAbsent(c);
			DEBUG.P("packge.members()��="+packge.members());
			
			//���һ������public�ģ���Դ�ļ����������һ��
			//���򱨴�:��:
			//Test4.java:25: class Test4s is public, should be declared in a file named Test4s.java
			//public class Test4s {
			//       ^
			if ((tree.mods.flags & PUBLIC) != 0 && !classNameMatchesFileName(c, env)) {
			log.error(tree.pos(),
				  "class.public.should.be.in.file", tree.name);
			}
				// </editor-fold>
		} else {
				// <editor-fold defaultstate="collapsed">
			if (tree.name.len != 0 &&
			!chk.checkUniqueClassName(tree.pos(), tree.name, enclScope)) {
				/*
				���������������ϵĳ�Ա��(��ӿ�)ͬ��ʱ��������Parser�׶η��ִ����
				����������ͨ��checkUniqueClassName()���
				��������Ĵ���:
				package my.test;
				public class Test {
					public class MyInnerClass {
					}
					
					public interface MyInnerClass {
					}
				}
				ͨ��compiler.properties�ļ��е�"compiler.err.already.defined"����:
				bin\mysrc\my\test\Test.java:12: ���� my.test.Test �ж��� my.test.Test.MyInnerClass
						public interface MyInnerClass {
							   ^
				1 ����
				*/
				
				DEBUG.P(2,this,"visitClassDef(1)");
				result = null;
				return;
			}
				// </editor-fold>
				// <editor-fold defaultstate="collapsed">
			if (owner.kind == TYP) {
				// We are seeing a member class.
				c = reader.enterClass(tree.name, (TypeSymbol)owner);

				DEBUG.P("owner.flags_field="+Flags.toString(owner.flags_field));
				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
				
				//�ӿ��еĳ�Ա�����η�������PUBLIC��STATIC
				//ע���ڽӿ��ڲ�Ҳ�ɶ���ӿڡ��ࡢö������
				if ((owner.flags_field & INTERFACE) != 0) {
					tree.mods.flags |= PUBLIC | STATIC;
				}

				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

			} else {
				DEBUG.P("owner.kind!=TYP(ע��)");
				// We are seeing a local class.
				c = reader.defineClass(tree.name, owner);
				c.flatname = chk.localClassName(c);
				DEBUG.P("c.flatname="+c.flatname);
				if (c.name.len != 0)
					chk.checkTransparentClass(tree.pos(), c, env.info.scope);
			}
				// </editor-fold>
		}
		tree.sym = c;
		
		DEBUG.P(2);
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.sym.members_field="+tree.sym.members_field);
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("ClassSymbol c.classfile="+c.classfile);
		DEBUG.P("if (chk.compiled.get(c.flatname) != null)="+(chk.compiled.get(c.flatname) != null));
		
		//��com.sun.tools.javac.comp.Check����Ϊ:public Map<Name,ClassSymbol> compiled = new HashMap<Name, ClassSymbol>();
		
		// Enter class into `compiled' table and enclosing scope.
		if (chk.compiled.get(c.flatname) != null) {
			//��ͬһԴ�ļ��ж���������ͬ������
			duplicateClass(tree.pos(), c);
			result = new ErrorType(tree.name, (TypeSymbol)owner);
			tree.sym = (ClassSymbol)result.tsym;
			
			DEBUG.P(2,this,"visitClassDef(1)");
			return;
		}
		chk.compiled.put(c.flatname, c);
		enclScope.enter(c);
		DEBUG.P("Scope enclScope="+enclScope);
		//DEBUG.P("env="+env);
		

		// Set up an environment for class block and store in `typeEnvs'
		// table, to be retrieved later in memberEnter and attribution.
		Env<AttrContext> localEnv = classEnv(tree, env);
		typeEnvs.put(c, localEnv);
		
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

		// Fill out class fields.
		c.completer = memberEnter;//����Ҫע��,����complete()�ĵ���ת��MemberEnter��
		c.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, c, tree);
		c.sourcefile = env.toplevel.sourcefile;
		c.members_field = new Scope(c);
		
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		
		ClassType ct = (ClassType)c.type;
		DEBUG.P("owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		
		/*����ǷǾ�̬��Ա��(��������Ա�ӿڣ���Աö����)����owner��type�������outer_field
		���´���:ֻ��MyInnerClass������������outer_fieldָ��Test
		public class Test {
			public class MyInnerClass {}
			public static class MyInnerClassStatic {}
			public interface MyInnerInterface {}
			public static interface MyInnerInterfaceStatic {}
			public enum MyInnerEnum {}
			public static enum MyInnerEnumStatic {}
		}
		*/
		if (owner.kind != PCK && (c.flags_field & STATIC) == 0) {
			// We are seeing a local or inner class.
			// Set outer_field of this class to closest enclosing class
			// which contains this class in a non-static context
			// (its "enclosing instance class"), provided such a class exists.
			Symbol owner1 = owner;
			//ע:�ھ�̬������(�磺��̬������)���ǲ������÷Ǿ�̬��ģ�
			//����������һ�������while��������ϾͲ���Ī��������
				
			//��һ��������
			/*��:
			class EnterTest {
				static void methodA() {
					class LocalClass{} //ct.getEnclosingType()=<none>
				}
				void methodB() {
					class LocalClass{} //ct.getEnclosingType()=my.test.EnterTest
				}
			}
			*/
			while ((owner1.kind & (VAR | MTH)) != 0 &&
			   (owner1.flags_field & STATIC) == 0) { //��̬�����еı�����û��outer
				owner1 = owner1.owner;
			}
			if (owner1.kind == TYP) {
				ct.setEnclosingType(owner1.type);

				DEBUG.P("ct      ="+ct.tsym);
				DEBUG.P("ct.outer="+ct.getEnclosingType());
			}
		}
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

		// Enter type parameters.
		ct.typarams_field = classEnter(tree.typarams, localEnv);
		
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

        DEBUG.P(2);
        DEBUG.P("***Enter��Type Parameter***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("����: "+c);
        //ע��Type Parameter������c.members_field�ĳ�Ա
        DEBUG.P("��Ա: "+c.members_field);
        DEBUG.P("Type Parameter: "+localEnv.info.scope);
       	DEBUG.P(2);

		DEBUG.P("if (!c.isLocal() && uncompleted != null)="+(!c.isLocal() && uncompleted != null));
		
		// Add non-local class to uncompleted, to make sure it will be
		// completed later.
		if (!c.isLocal() && uncompleted != null) uncompleted.append(c);
		//	System.err.println("entering " + c.fullname + " in " + c.owner);//DEBUG

		// Recursively enter all member classes.
		

		DEBUG.P("tree.type="+tree.type);
		classEnter(tree.defs, localEnv);
		//DEBUG.P("Enter.visitClassDef(JCClassDecl tree) stop",true);

		result = c.type;
		
			DEBUG.P(2);
			DEBUG.P("***������г�ԱEnter���***");
			DEBUG.P("-----------------------------------------------");
			DEBUG.P("����: "+c);
			DEBUG.P("��Ա: "+c.members_field);
			DEBUG.P("Type Parameter: "+localEnv.info.scope);
		
		//ע��:�����ж������(������)����Enter
		DEBUG.P(2,this,"visitClassDef(1)");
    }
    
    
    
    
    
    
    //where
	/** Does class have the same name as the file it appears in?
	 */
	private static boolean classNameMatchesFileName(ClassSymbol c,
							Env<AttrContext> env) {
	    return env.toplevel.sourcefile.isNameCompatible(c.name.toString(),
							    JavaFileObject.Kind.SOURCE);
	}

    /** Complain about a duplicate class. */
    protected void duplicateClass(DiagnosticPosition pos, ClassSymbol c) {
		log.error(pos, "duplicate.class", c.fullname);
    }

    /** Class enter visitor method for type parameters.
     *	Enter a symbol for type parameter in local scope, after checking that it
     *	is unique.
     */
    /*
    TypeParameter�������ClassSymbol.members_field�У�
    ֻ������JCClassDecl��Ӧ��Env<AttrContext>.info.Scope�С�

    ���⣬�ڷ������ඨ���TypeParameter��������ͬ�����ͱ�������
    ���߻���Ӱ�졣������ʾ:
    class Test<T,S> {
            public <T> void method(T t){}
    }
    */
    public void visitTypeParameter(JCTypeParameter tree) {
        DEBUG.P(this,"visitTypeParameter(JCTypeParameter tree)");
        DEBUG.P("tree.name="+tree.name);
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
        if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);

		TypeVar a = (tree.type != null)
			? (TypeVar)tree.type
			: new TypeVar(tree.name, env.info.scope.owner);
		tree.type = a;
		/*TypeParameter���������������������TypeParameter��
		��������Parser�׶μ�������ģ����������checkUnique()�����С�
		
		��������:
		bin\mysrc\my\test\Test.java:64: ���� my.test.Test2 �ж��� T
		class Test2<T,T>{}
					  ^
		1 ����
		*/
		if (chk.checkUnique(tree.pos(), a.tsym, env.info.scope)) {
			env.info.scope.enter(a.tsym);
		}
		result = a;


		if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);
        DEBUG.P(0,this,"visitTypeParameter(JCTypeParameter tree)");
    }

    /** Default class enter visitor method: do nothing.
     */
    public void visitTree(JCTree tree) {
    	DEBUG.P(this,"visitTree(1)");
        result = null;
        DEBUG.P(0,this,"visitTree(1)");
    }

    /** Main method: enter all classes in a list of toplevel trees.
     *	@param trees	  The list of trees to be processed.
     */
    public void main(List<JCCompilationUnit> trees) {
		DEBUG.P(this,"main(1)");
		complete(trees, null);
		DEBUG.P(0,this,"main(1)");
    }

    /** Main method: enter one class from a list of toplevel trees and
     *  place the rest on uncompleted for later processing.
     *  @param trees      The list of trees to be processed.
     *  @param c          The class symbol to be processed.
     */
     
    //�ڴ�MemberEnter�׶ν��е�Resolve.loadClass(Env<AttrContext> env, Name name)ʱ��
    //���һ����ĳ��໹û�б��룬���ȴ�ͷ��ʼ���볬�࣬�ֻ��JavaCompiler.complete(ClassSymbol c)
    //ת�������ʱ ClassSymbol c�Ͳ�Ϊnull��
    public void complete(List<JCCompilationUnit> trees, ClassSymbol c) {
    	DEBUG.P(this,"complete(2)");
    	//DEBUG.P("���EnterǰList<JCCompilationUnit> trees������: trees.size="+trees.size());
    	//DEBUG.P("------------------------------------------------------------------------------");
    	//DEBUG.P(""+trees);
    	//DEBUG.P("------------------------------------------------------------------------------");
		/*
    	if(typeEnvs!=null) {
            DEBUG.P("");
            DEBUG.P("Env����: "+typeEnvs.size());
            DEBUG.P("--------------------------");
            for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
                    DEBUG.P(""+myMapEntry);
            DEBUG.P("");	
        }
        DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
		*/
    	
       
        annotate.enterStart();
        ListBuffer<ClassSymbol> prevUncompleted = uncompleted;
        if (memberEnter.completionEnabled) uncompleted = new ListBuffer<ClassSymbol>();

        DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//0

        try {
            // enter all classes, and construct uncompleted list
            classEnter(trees, null);


            DEBUG.P(5);
            DEBUG.P("***����ڶ��׶�MemberEnter***");
            DEBUG.P("-----------------------------------------------");

            //uncompleted�в���������
            DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
            //DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//!=0

            // complete all uncompleted classes in memberEnter
            if (memberEnter.completionEnabled) {
                if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
                else DEBUG.P("uncompleted=null");
                
                // <editor-fold defaultstate="collapsed">

                while (uncompleted.nonEmpty()) {
                    ClassSymbol clazz = uncompleted.next();
                    DEBUG.P("Uncompleted SymbolName="+clazz);
                    DEBUG.P("clazz.completer="+clazz.completer);
                    DEBUG.P("(c == null)="+(c == null));
                    DEBUG.P("(c == clazz)="+(c == clazz));
                    DEBUG.P("(prevUncompleted == null)="+(prevUncompleted == null));
                    /*
                    if(c!=null) DEBUG.P("c.name="+c.name+" c.kind="+c.kind);
                    else DEBUG.P("c.name=null c.kind=null");
                    if(clazz!=null) DEBUG.P("clazz.name="+clazz.name+" clazz.kind="+clazz.kind);
                    else DEBUG.P("clazz.name=null clazz.kind=null");
                    */

                    //����MemberEnter�׶ν��е�����ʱ��c!=null��c��uncompleted�У�
                    //����c == clazz��������һ�Σ����Զ�c����complete()��
                    //�������c���ڲ��࣬��Ϊc!=null��c != clazz(�ڲ���)��
                    //prevUncompleted != null(���һ�ν���MemberEnter�׶�ʱuncompleted!=null)
                    //����c�������ڲ�����ʱ������complete()���ȷ���prevUncompleted�У������������
                    if (c == null || c == clazz || prevUncompleted == null)
                        clazz.complete();
                    else
                        // defer
                        prevUncompleted.append(clazz);

                    DEBUG.P("");
                }
                // </editor-fold>

				DEBUG.P("trees="+trees);

                // if there remain any unimported toplevels (these must have
                // no classes at all), process their import statements as well.
                for (JCCompilationUnit tree : trees) {
                    DEBUG.P(2);
                    DEBUG.P("tree.starImportScope="+tree.starImportScope);
                    DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
					DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
                    if (tree.starImportScope.elems == null) {
                        JavaFileObject prev = log.useSource(tree.sourcefile);
                        //�е��typeEnvs =new HashMap<TypeSymbol,Env<AttrContext>>();
                        //��tree��JCCompilationUnit����ôget???????????

						//ͬʱ����package-info.javaʱ�ͻ�����������
                        Env<AttrContext> env = typeEnvs.get(tree);
						DEBUG.P("env="+env);
                        if (env == null)
                            env = topLevelEnv(tree);
                        memberEnter.memberEnter(tree, env);
                        log.useSource(prev);
                    }
                }

				DEBUG.P("Enter����:for (JCCompilationUnit tree : trees)");
				DEBUG.P(3);
            }
        } finally {
            uncompleted = prevUncompleted;
            annotate.enterDone();

            if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
            else DEBUG.P("uncompleted=null");

            //DEBUG.P(2);
            //DEBUG.P("���Enter��List<JCCompilationUnit> trees������: trees.size="+trees.size());
            //DEBUG.P("------------------------------------------------------------------------------");
            //DEBUG.P(""+trees);
            //DEBUG.P("------------------------------------------------------------------------------");
            
			/*
			if(typeEnvs!=null) {
				DEBUG.P("");
				DEBUG.P("Env����: "+typeEnvs.size());
				DEBUG.P("--------------------------");
				for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet()) {
					Env<AttrContext> e = myMapEntry.getValue();
					DEBUG.P("e.tree.type="+e.tree.type); //JCClassDecl.typeΪnull
				}
				DEBUG.P("");	
			}
			DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
			*/
			
			DEBUG.P(2,this,"complete(2)");
        }
    }
}
