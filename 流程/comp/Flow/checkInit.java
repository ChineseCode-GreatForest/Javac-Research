    /** Check that trackable variable is initialized.
     */
    void checkInit(DiagnosticPosition pos, VarSymbol sym) {
		DEBUG.P(this,"checkInit(2)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("firstadr="+firstadr);
		DEBUG.P("inits="+inits);
		if ((sym.adr >= firstadr || sym.owner.kind != TYP) &&
			trackable(sym) &&
			!inits.isMember(sym.adr)) {
			DEBUG.P("������δ��ʼ������:"+sym);
			//����ж��������δ��ʼ���ı���,log.error()ֻ����һ������
			log.error(pos, "var.might.not.have.been.initialized",
					  sym);
			inits.incl(sym.adr);
		}
		DEBUG.P("inits="+inits);
		DEBUG.P(0,this,"checkInit(2)");
    }