    /** ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    JCExpression arrayInitializer(int newpos, JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"arrayInitializer(2)");
		
        accept(LBRACE);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE) {
        	//arrayInitializer()��variableInitializer()�����໥����
        	//����ʵ�ֶ�ά����(��{{1,2},{3,4}}�ĳ�ʼ��
            elems.append(variableInitializer());
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE) break;
                elems.append(variableInitializer());
            }
        }
        accept(RBRACE);
        return toP(F.at(newpos).NewArray(t, List.<JCExpression>nil(), elems.toList()));
    	
    	}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"arrayInitializer(2)");
		}  
    }