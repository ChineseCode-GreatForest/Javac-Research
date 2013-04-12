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