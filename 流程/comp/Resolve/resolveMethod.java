    /** Resolve an unqualified method identifier.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param name      The identifier's name.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    Symbol resolveMethod(DiagnosticPosition pos,
                         Env<AttrContext> env,
                         Name name,//������
                         List<Type> argtypes,//���÷���ʱָ���Ĳ���������

						 //���÷���ʱָ�������Ͳ�����ʵ����typeargtypes.size=0��
						 //��Ϊ<ClassA>methodName(...)�������﷨�Ǵ����
                         List<Type> typeargtypes) {
        try {
        DEBUG.P(this,"resolveMethod(5)");   
        DEBUG.P("env="+env);
        DEBUG.P("name="+name);
        DEBUG.P("argtypes="+argtypes);
        DEBUG.P("typeargtypes="+typeargtypes);
        
        Symbol sym = findFun(env, name, argtypes, typeargtypes, false, env.info.varArgs=false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = findFun(env, name, argtypes, typeargtypes, true, false);
            if (sym.kind >= WRONG_MTHS)
                sym = findFun(env, name, argtypes, typeargtypes, true, env.info.varArgs=true);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(
                sym, pos, env.enclClass.sym.type, name, false, argtypes, typeargtypes);
        }
        return sym;
        
        }finally{
        DEBUG.P(0,this,"resolveMethod(5)");   
        }
    }