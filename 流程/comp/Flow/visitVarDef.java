    public void visitVarDef(JCVariableDecl tree) {
		DEBUG.P(this,"visitVarDef(1)");
		boolean track = trackable(tree.sym);
		DEBUG.P("track="+track);
		//ע��:��JCBlock�ж���ı���,tree.sym.owner.kind��ΪMTH
		DEBUG.P("tree.sym.owner.kind="+Kinds.toString(tree.sym.owner.kind));
		if (track && tree.sym.owner.kind == MTH) newVar(tree.sym);
		DEBUG.P("tree.init="+tree.init);
		
		Bits initsPrev = inits.dup();//�Ҽ��ϵ�
		Bits uninitsPrev = uninits.dup();//�Ҽ��ϵ�
		
		if (tree.init != null) {
			Lint lintPrev = lint;
			lint = lint.augment(tree.sym.attributes_field);
			try{
				scanExpr(tree.init);
				if (track) letInit(tree.pos(), tree.sym);
			} finally {
				lint = lintPrev;
			}
		}
		DEBUG.P("inits  ǰ="+initsPrev);
		DEBUG.P("inits  ��="+inits);
		//ע�����������������������letInit������й�
		DEBUG.P("uninitsǰ="+uninitsPrev);
		DEBUG.P("uninits��="+uninits);
		DEBUG.P(0,this,"visitVarDef(1)");
    }