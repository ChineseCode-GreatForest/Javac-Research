    /** VariableInitializer = ArrayInitializer | Expression
     */
    public JCExpression variableInitializer() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableInitializer()");
		        
        return S.token() == LBRACE ? arrayInitializer(S.pos(), null) : expression();

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableInitializer()");
		}    
    }
