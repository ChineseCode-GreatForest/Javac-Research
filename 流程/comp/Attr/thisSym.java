    /** The current `this' symbol.
     *  @param env    The current environment.
     */
    Symbol thisSym(DiagnosticPosition pos, Env<AttrContext> env) {
		try {//�Ҽ��ϵ�
            DEBUG.P(this,"thisSym(2)");
        return rs.resolveSelf(pos, env, env.enclClass.sym, names._this);

		}finally{//�Ҽ��ϵ�
            DEBUG.P(0,this,"thisSym(2)");
        }
    }