    /** Find an unqualified type symbol.
     *  @param env       The current environment.
     *  @param name      The type's name.
     */
    Symbol findType(Env<AttrContext> env, Name name) {
    	Symbol bestSoFar = typeNotFound;
        Symbol sym;
        
    	try{
    	//DEBUG.ON();
    	DEBUG.P(this,"findType(2)");
    	DEBUG.P("name="+name);
    	DEBUG.P("env="+env);
    	DEBUG.P("env.outer="+env.outer);
    	
    	/*
    	�ȴӵ�ǰenv.info.scope�в���name��
    	û�ҵ�ʱ�ٸ���env.enclClass.sym���ң�
    	��Ϊenv.enclClass.sym��ClassSymbol���ʵ������,
    	����ʵ������ClassSymbol��members_field�в���name,
    	�绹û�ҵ�����ClassSymbol.type�ĳ����Լ�����ʵ�ֵĽӿ�,
    	�绹û�ҵ�����env.outer�а�����ķ�ʽ����.
    	
    	���������(ֱ��env.outer==null����topLevelEnv),�绹û�ҵ�
    	����env.toplevel.namedImportScope,
    	����env.toplevel.packge.members(),
    	����env.toplevel.starImportScope
    	*/
        
        boolean staticOnly = false;
		//Ϊʲô����������env1.outer != null��?
		//��Ϊ��env1.outer == nullʱ����ʾenv1������ˣ�
		//����env.enclClass.sym.members_field��Symtab��Ԥ����
		//�ķ�����û��TYP���͵Ļ������ţ�����û��Ҫ�����ˣ�
		//����©��namedImportScope���˳�forʱ�ٲ���
        for (Env<AttrContext> env1 = env; env1.outer != null; env1 = env1.outer) {
            // <editor-fold defaultstate="collapsed">
            DEBUG.P("env1.info.staticLevel="+env1.info.staticLevel);
            DEBUG.P("env1.outer.info.staticLevel="+env1.outer.info.staticLevel);
            if (isStatic(env1)) staticOnly = true;
            DEBUG.P("staticOnly="+staticOnly);
            DEBUG.P("env1.info.scope="+env1.info.scope);
            for (Scope.Entry e = env1.info.scope.lookup(name);
                 e.scope != null;
                 e = e.next()) {
				DEBUG.P("e.sym="+e.sym);
                DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
				if (e.sym.kind == TYP) {
					DEBUG.P("e.sym.type.tag="+TypeTags.toString(e.sym.type.tag));
					DEBUG.P("e.sym.owner="+e.sym.owner);
					DEBUG.P("e.sym.owner.kind="+Kinds.toString(e.sym.owner.kind));
						
					/*
						��������:
                        bin\mysrc\my\test\Test.java:28: �޷��Ӿ�̬�����������÷Ǿ�̬ ���ͱ��������Ʒ�ΧT
                                        public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,Str
                        ing s2,int... ii)[] throws Exception,Error{
                                                                                         ^
                        ����Ĵ�����ʾλ���е�֣���Ȼ��������static����myMethod�����÷Ǿ�̬ ���ͱ���T��
                        ��������ʾλ������Exception����������<M extends T,S>��
                        
						class VisitSelectTest<T> {
							static T a2;
						}
						test\attr\VisitSelectTest.java:3: �޷��Ӿ�̬�����������÷Ǿ�̬ ���ͱ��������Ʒ� Χ T
								static T a2;
									   ^
						1 ����						
					*/
					if (staticOnly &&
                        e.sym.type.tag == TYPEVAR &&
                        e.sym.owner.kind == TYP) return new StaticError(e.sym);

                    DEBUG.P("���ҵ� "+e.sym+" ��env="+env1.info.scope);
                    return e.sym;
				}
            }

            sym = findMemberType(env1, env1.enclClass.sym.type, name,
                                 env1.enclClass.sym);
            if (staticOnly && sym.kind == TYP &&
                sym.type.tag == CLASS &&
                sym.type.getEnclosingType().tag == CLASS &&
                env1.enclClass.sym.type.isParameterized() &&
                sym.type.getEnclosingType().isParameterized())
                return new StaticError(sym);
            else if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            JCClassDecl encl = env1.baseClause ? (JCClassDecl)env1.tree : env1.enclClass;
            if ((encl.sym.flags() & STATIC) != 0)
                staticOnly = true;
            
            // </editor-fold>
        }
        
        DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
        if (env.tree.tag != JCTree.IMPORT) {
            sym = findGlobalType(env, env.toplevel.namedImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.packge.members(), name);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.exists()="+sym.exists());
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.starImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        
        return bestSoFar;
        
        
    	}finally{
    	//DEBUG.P("env.toplevel.namedImportScope="+env.toplevel.namedImportScope);
    	//DEBUG.P("env.toplevel.packge.members()="+env.toplevel.packge.members());
    	//DEBUG.P("env.toplevel.starImportScope="+env.toplevel.starImportScope);
    	//DEBUG.P("Symbol bestSoFar="+bestSoFar);
    	DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
    	DEBUG.P(0,this,"findType(2)");
    	//DEBUG.OFF();
    	}
    }