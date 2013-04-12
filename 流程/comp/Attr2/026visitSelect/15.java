    /** Is class accessible in given evironment?
     *  @param env    The current environment.
     *  @param c      The class whose accessibility is checked.
     */
    //��env����һ���������Ƿ���Ȩ�޷���TypeSymbol c
    public boolean isAccessible(Env<AttrContext> env, TypeSymbol c) {
    	try {//�Ҽ��ϵ�
    	//DEBUG.ON();    		
		DEBUG.P(this,"isAccessible(Env<AttrContext> env, TypeSymbol c)");
		DEBUG.P("env="+env);
		DEBUG.P("c="+c);
		DEBUG.P("c.flags()="+Flags.toString(c.flags()));
		DEBUG.P("c.flags() & AccessFlags="+Flags.toString(c.flags() & AccessFlags));
		DEBUG.P("env.enclClass.sym.name="+env.enclClass.sym.name);
		DEBUG.P("env.enclClass.sym.outermostClass()="+env.enclClass.sym.outermostClass());
		DEBUG.P("c.owner.name="+c.owner.name);
		DEBUG.P("c.owner.outermostClass()="+c.owner.outermostClass());
		
		//AccessFlags = PUBLIC | PROTECTED | PRIVATE��Flags���ж���
        switch ((short)(c.flags() & AccessFlags)) {
        case PRIVATE:
            return
                env.enclClass.sym.outermostClass() ==
                c.owner.outermostClass();
        case 0:
            return
                env.toplevel.packge == c.owner // fast special case
                ||
                env.toplevel.packge == c.packge()
                ||
                // Hack: this case is added since synthesized default constructors
                // of anonymous classes should be allowed to access
                // classes which would be inaccessible otherwise.
                env.enclMethod != null &&
                (env.enclMethod.mods.flags & ANONCONSTR) != 0;
        default: // error recovery
        case PUBLIC:
            return true;
        case PROTECTED:
            return
                env.toplevel.packge == c.owner // fast special case
                ||
                env.toplevel.packge == c.packge()
                ||
                isInnerSubClass(env.enclClass.sym, c.owner);
        }
        
        }finally{//�Ҽ��ϵ�        	 	
			DEBUG.P(0,this,"isAccessible(Env<AttrContext> env, TypeSymbol c)");
			//DEBUG.OFF();   
		}
    }