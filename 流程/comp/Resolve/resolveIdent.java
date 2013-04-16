/* ***************************************************************************
 *  Name resolution
 *  Naming conventions are as for symbol lookup
 *  Unlike the find... methods these methods will report access errors
 ****************************************************************************/

    /** Resolve an unqualified (non-method) identifier.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the identifier use.
     *  @param name      The identifier's name.
     *  @param kind      The set of admissible symbol kinds for the identifier.
     */
    Symbol resolveIdent(DiagnosticPosition pos, Env<AttrContext> env,
                        Name name, int kind) {
		Symbol sym=null;//我加上的
        try {//我加上的
        DEBUG.P(this,"resolveIdent(4)");   
        //DEBUG.P("env="+env);
        //DEBUG.P("name="+name);
        //DEBUG.P("kind="+Kinds.toString(kind));
               	
        return sym= access(
            findIdent(env, name, kind),
            pos, env.enclClass.sym.type, name, false);
            
        
        }finally{
		DEBUG.P("name="+name);
        DEBUG.P("kind="+Kinds.toString(kind));
		if(sym!=null)  {
			DEBUG.P("sym     ="+sym);
			DEBUG.P("sym.type="+sym.type);
		} else 
			DEBUG.P("sym     =null");
        DEBUG.P(0,this,"resolveIdent(4)");  
        }
    }