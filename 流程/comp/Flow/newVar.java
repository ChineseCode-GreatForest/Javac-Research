    /** Initialize new trackable variable by setting its address field
     *	to the next available sequence number and entering it under that
     *	index into the vars array.
     */
	void newVar(VarSymbol sym) {
		DEBUG.P(this,"newVar(VarSymbol sym)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("nextadr="+nextadr+"   vars.length="+vars.length);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		if (nextadr == vars.length) {
			VarSymbol[] newvars = new VarSymbol[nextadr * 2];
			System.arraycopy(vars, 0, newvars, 0, nextadr);
			vars = newvars;
		}
		//ע��:uninits��ĳһbit�Լ�vars[nextadr]�п��ܱ����ǵ����
		sym.adr = nextadr;
		DEBUG.P("vars["+nextadr+"]ǰ="+vars[nextadr]);
		vars[nextadr] = sym;
		DEBUG.P("vars["+nextadr+"]��="+vars[nextadr]);
		inits.excl(nextadr);
		uninits.incl(nextadr);
		nextadr++;
		DEBUG.P("nextadr="+nextadr);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		DEBUG.P(0,this,"newVar(VarSymbol sym)");
    }