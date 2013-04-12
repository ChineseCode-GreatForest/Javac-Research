    /** Derived visitor method: attribute an expression tree with
     *  no constraints on the computed type.
     */
    Type attribExpr(JCTree tree, Env<AttrContext> env) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"attribExpr(2)");
		
        return attribTree(tree, env, VAL, Type.noType);
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"attribExpr(2)");
		}
     