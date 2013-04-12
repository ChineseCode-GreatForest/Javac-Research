/* ********************************************************************
 * Source completer
 *********************************************************************/

    /** Complete entering a class.
     *  @param sym         The symbol of the class to be completed.
     */
    public void complete(Symbol sym) throws CompletionFailure {
        // <editor-fold defaultstate="collapsed">
    	try {
    	DEBUG.P(this,"complete(Symbol sym)");
    	DEBUG.P("sym="+sym+"  sym.kind="+Kinds.toString(sym.kind)+" isFirst="+isFirst+"  completionEnabled="+completionEnabled);

        // Suppress some (recursive) MemberEnter invocations
        if (!completionEnabled) {
            // Re-install same completer for next time around and return.
            assert (sym.flags() & Flags.COMPOUND) == 0;
            sym.completer = this;
            return;
        }

        ClassSymbol c = (ClassSymbol)sym;
        ClassType ct = (ClassType)c.type;
        Env<AttrContext> env = enter.typeEnvs.get(c);
        JCClassDecl tree = (JCClassDecl)env.tree;
        boolean wasFirst = isFirst;
        isFirst = false;
        
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        DEBUG.P("env="+env);
		// </editor-fold>

        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            // <editor-fold defaultstate="collapsed">
            // Save class environment for later member enter (2) processing.
            halfcompleted.append(env);

            // If this is a toplevel-class, make sure any preceding import
            // clauses have been seen.
            if (c.owner.kind == PCK) {
                memberEnter(env.toplevel, env.enclosing(JCTree.TOPLEVEL));
                todo.append(env);
            }


        	DEBUG.P(2);
        	DEBUG.P("***JCTree.TOPLEVEL MemberEnter��***");
        	DEBUG.P("--------------------------------------");
        	DEBUG.P("env.toplevel.packge               ="+env.toplevel.packge);
        	DEBUG.P("env.toplevel.packge.members_field ="+env.toplevel.packge.members_field);
        	DEBUG.P("toplevel.env.info.scope           ="+env.enclosing(JCTree.TOPLEVEL).info.scope);
        	DEBUG.P("env.toplevel.namedImportScope     ="+env.toplevel.namedImportScope);
        	DEBUG.P("env.toplevel.starImportScope      ="+env.toplevel.starImportScope);
        	DEBUG.P(2);
       	


            // Mark class as not yet attributed.
            c.flags_field |= UNATTRIBUTED;
            
            DEBUG.P("c="+c);
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        	
        
            if (c.owner.kind == TYP)
                c.owner.complete();
                
            

            // create an environment for evaluating the base clauses
            Env<AttrContext> baseEnv = baseEnv(tree, env);
            
            

            //DEBUG.P("env="+env);
            //DEBUG.P("baseEnv="+env);
            DEBUG.P("tree.extending="+tree.extending);
            DEBUG.P("ct.supertype_fieldǰ="+ct.supertype_field);
			DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
            // Determine supertype.
            Type supertype =
                (tree.extending != null)
                ? attr.attribBase(tree.extending, baseEnv, true, false, true)
                : ((tree.mods.flags & Flags.ENUM) != 0 && !target.compilerBootstrap(c))
                ? attr.attribBase(enumBase(tree.pos, c), baseEnv,
                                  true, false, false)//ö�����Ͳ��ܴ�extends�����Բ��ü��̳У��������һ��������false
                : (c.fullname == names.java_lang_Object)
                ? Type.noType
                : syms.objectType;
            ct.supertype_field = supertype;
            //DEBUG.P("ct.supertype_field��="+ct.supertype_field);
            DEBUG.P("ct.supertype_field.tag="+TypeTags.toString(ct.supertype_field.tag));
            DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
            
            DEBUG.P("");
            DEBUG.P("ct.interfaces_fieldǰ="+ct.interfaces_field);
            DEBUG.P("tree.implementing="+tree.implementing);
            
            // </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // Determine interfaces.
            ListBuffer<Type> interfaces = new ListBuffer<Type>();
            Set<Type> interfaceSet = new HashSet<Type>();
            List<JCExpression> interfaceTrees = tree.implementing;
			DEBUG.P("((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c))="+((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c)));
			/*��-target jsr14ѡ������������Ϳ���ʹifΪtrue
			package com.sun.tools;

			enum CompilerBootstrapEnumTest {
				A,
				B,
				C;
			}
			*/
            //ö������Ĭ��ʵ����java.lang.Comparable��java.io.Serializable�ӿ�
            if ((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c)) {
                // add interface Comparable<T>
                interfaceTrees =
                    interfaceTrees.prepend(make.Type(new ClassType(syms.comparableType.getEnclosingType(),
                                                                   List.of(c.type),
                                                                   syms.comparableType.tsym)));
                // add interface Serializable
                interfaceTrees =
                    interfaceTrees.prepend(make.Type(syms.serializableType));
            }
            for (JCExpression iface : interfaceTrees) {
                Type i = attr.attribBase(iface, baseEnv, false, true, true);
				DEBUG.P("i="+i);
				DEBUG.P("i.tag="+TypeTags.toString(i.tag));
                if (i.tag == CLASS) {
                    interfaces.append(i);
                    chk.checkNotRepeated(iface.pos(), types.erasure(i), interfaceSet);
                }
            }
            if ((c.flags_field & ANNOTATION) != 0)
                ct.interfaces_field = List.of(syms.annotationType);
            else
                ct.interfaces_field = interfaces.toList();
            DEBUG.P("");
            DEBUG.P("ct.interfaces_field��="+ct.interfaces_field);
            DEBUG.P("c.fullname="+c.fullname);    
            //java.lang.Objectû�г��࣬Ҳ��ʵ���κνӿ�
            if (c.fullname == names.java_lang_Object) {
                if (tree.extending != null) {
                    chk.checkNonCyclic(tree.extending.pos(),
                                       supertype);
                    ct.supertype_field = Type.noType;
                }
                else if (tree.implementing.nonEmpty()) {
                    chk.checkNonCyclic(tree.implementing.head.pos(),
                                       ct.interfaces_field.head);
                    ct.interfaces_field = List.nil();
                }
            }
            // </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // Annotations.
            // In general, we cannot fully process annotations yet,  but we
            // can attribute the annotation types and then check to see if the
            // @Deprecated annotation is present.
            attr.attribAnnotationTypes(tree.mods.annotations, baseEnv);
            if (hasDeprecatedAnnotation(tree.mods.annotations))
                c.flags_field |= DEPRECATED;
                
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            
            annotateLater(tree.mods.annotations, baseEnv, c);
            
            DEBUG.P(3);
            //DEBUG.P("baseEnv="+baseEnv);
            //DEBUG.P("tree.typarams="+tree.typarams);
            
            
            attr.attribTypeVariables(tree.typarams, baseEnv);
            
            DEBUG.P("c.type="+c.type);
            DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
            
            //��ͬһ��type�ںܶ�ط�����������ͬ��ѭ����⣬
			//ֱ��type.tsym.flags_field����ACYCLIC��־Ϊֹ��
			//��һ���Ƿ��иĽ��Ŀռ䣿
            chk.checkNonCyclic(tree.pos(), c.type);
            
            DEBUG.P("c="+c);
            DEBUG.P("c.name="+c.name);
            DEBUG.P("c.flags()="+Flags.toString(c.flags()));
            // Add default constructor if needed.
            if ((c.flags() & INTERFACE) == 0 &&
                !TreeInfo.hasConstructors(tree.defs)) {
                List<Type> argtypes = List.nil();
                List<Type> typarams = List.nil();
                List<Type> thrown = List.nil();
                long ctorFlags = 0;
                boolean based = false;
                DEBUG.P("c.name.len="+c.name.len);
                if (c.name.len == 0) {
                    JCNewClass nc = (JCNewClass)env.next.tree;
                    DEBUG.P("nc.constructor="+nc.constructor);
                    if (nc.constructor != null) {
                        Type superConstrType = types.memberType(c.type,
                                                                nc.constructor);
                        argtypes = superConstrType.getParameterTypes();
                        typarams = superConstrType.getTypeArguments();
                        ctorFlags = nc.constructor.flags() & VARARGS;
                        if (nc.encl != null) {
                            argtypes = argtypes.prepend(nc.encl.type);
                            based = true;
                        }
                        thrown = superConstrType.getThrownTypes();
                    }
                }
                JCTree constrDef = DefaultConstructor(make.at(tree.pos), c,
                                                    typarams, argtypes, thrown,
                                                    ctorFlags, based);
                tree.defs = tree.defs.prepend(constrDef);
            }
			// </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // If this is a class, enter symbols for this and super into
            // current scope.
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            if ((c.flags_field & INTERFACE) == 0) {
                VarSymbol thisSym =
                    new VarSymbol(FINAL | HASINIT, names._this, c.type, c);
                thisSym.pos = Position.FIRSTPOS;
                
                DEBUG.P("thisSym="+thisSym);
                DEBUG.P("env.info.scope="+env.info.scope);
                
                env.info.scope.enter(thisSym);
                DEBUG.P("env.info.scope="+env.info.scope);
                DEBUG.P("ct.supertype_field="+ct.supertype_field);
				DEBUG.P("ct.supertype_field.tag="+TypeTags.toString(ct.supertype_field.tag));
                
                if (ct.supertype_field.tag == CLASS) {
                    VarSymbol superSym =
                        new VarSymbol(FINAL | HASINIT, names._super,
                                      ct.supertype_field, c);
                    superSym.pos = Position.FIRSTPOS;
                    
                    DEBUG.P("superSym="+superSym);
                	DEBUG.P("env.info.scope="+env.info.scope);
                
                    env.info.scope.enter(superSym);
                    
                    DEBUG.P("env.info.scope="+env.info.scope);
                }
            }
            
            DEBUG.P("checkClash="+checkClash);
            DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
            DEBUG.P("c.owner="+c.owner);
            DEBUG.P("c.fullname="+c.fullname);

            // check that no package exists with same fully qualified name,
            // but admit classes in the unnamed package which have the same
            // name as a top-level package.
            //��ִ��reader.packageExists(c.fullname))ʱ��Ҳ���һ��������һ��������
            //��Map<Name, PackageSymbol> packages
            //ע:��Ա�಻�ü��
            DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());

			//ͬʱ����test/memberEnter/EnumTest.java
			//test/memberEnter/Clash/ClassA.java�ɲ��Գ�����
			DEBUG.P("reader.packageExists(c.fullname)="+reader.packageExists(c.fullname));
            if (checkClash &&
                c.owner.kind == PCK && c.owner != syms.unnamedPackage &&
                reader.packageExists(c.fullname))
                {
                    log.error(tree.pos, "clash.with.pkg.of.same.name", c);
                }
            DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		// </editor-fold>
        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            log.useSource(prev);
        }
        
        // <editor-fold defaultstate="collapsed">
        DEBUG.P("wasFirst="+wasFirst);
        DEBUG.P("halfcompleted.nonEmpty()="+halfcompleted.nonEmpty());
        if(halfcompleted.nonEmpty()) 
        DEBUG.P("halfcompleted.size="+halfcompleted.size());
        
        // Enter all member fields and methods of a set of half completed
        // classes in a second phase.
        if (wasFirst) {
        	//ע:��Ա����MemberEnter�׶β���finish
            try {
                while (halfcompleted.nonEmpty()) {
                    finish(halfcompleted.next());
                }
            } finally {
                isFirst = true;
            }

            // commit pending annotations
            annotate.flush();
        }
        
    	} finally {
    	//DEBUG.P("sym.members_field="+((ClassSymbol)sym).members_field);
    	DEBUG.P(3,this,"complete(Symbol sym)");
    	}
        
        // </editor-fold>
    }
