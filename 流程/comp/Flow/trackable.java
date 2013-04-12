    /*-------------- Processing variables ----------------------*/

    /** Do we need to track init/uninit state of this symbol?
     *	I.e. is symbol either a local or a blank final variable?
     */
    boolean trackable(VarSymbol sym) {
    	/*
		return
	    (sym.owner.kind == MTH ||
	     ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
	      classDef.sym.isEnclosedBy((ClassSymbol)sym.owner)));
	      */
	      
		//�Ҽ��ϵ�
		DEBUG.P(this,"trackable(VarSymbol sym)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
		DEBUG.P("((sym.flags() & (FINAL | HASINIT | PARAMETER))="+Flags.toString(((sym.flags() & (FINAL | HASINIT | PARAMETER)))));
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
		
		//�����еı���(���ر���)��û�г�ʼ����FINAL��Ա����(����PARAMETER)��Ҫtrack
		boolean trackable=(sym.owner.kind == MTH ||
			 ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
			  classDef.sym.isEnclosedBy((ClassSymbol)sym.owner)));
		
		DEBUG.P("trackable="+trackable);
		DEBUG.P(0,this,"trackable(VarSymbol sym)");
		return trackable;
    }