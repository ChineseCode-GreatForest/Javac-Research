    public void visitIdent(JCIdent tree) {
		DEBUG.P(this,"visitIdent(1)");
		DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));
		
		//�����JCIdent�����Ƿ��������߱�Ķ���������Ҫ�ж�һ��
		if (tree.sym.kind == VAR)
			checkInit(tree.pos(), (VarSymbol)tree.sym);
			
		DEBUG.P(0,this,"visitIdent(1)");    
    }