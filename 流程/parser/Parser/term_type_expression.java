    /** terms can be either expressions or types.
     */
    public JCExpression expression() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"expression()");
		
        return term(EXPR);

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"expression()");
		}        
    }

    public JCExpression type() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"type()");

        return term(TYPE);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"type()");
		}
    }

    JCExpression term(int newmode) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term(int newmode)");
		DEBUG.P("newmode="+myMode(newmode)+"  mode="+myMode(mode));
		
        int prevmode = mode;
        mode = newmode;
        JCExpression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term(int newmode)");
		}
    }