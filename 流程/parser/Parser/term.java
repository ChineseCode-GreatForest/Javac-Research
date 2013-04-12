    /**
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    JCExpression term() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term()");
		
        JCExpression t = term1();   
        /*
        ����"="֮������и�ֵ�������Token.java�еĶ���˳������:
        PLUSEQ("+="),
	    SUBEQ("-="),
	    STAREQ("*="),
	    SLASHEQ("/="),
	    AMPEQ("&="),
	    BAREQ("|="),
	    CARETEQ("^="),
	    PERCENTEQ("%="),
	    LTLTEQ("<<="),
	    GTGTEQ(">>="),
	    GTGTGTEQ(">>>="),
	    
	    ���PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0
	    ��ʾS.token()����������Token֮һ��
        
        PLUSEQ.compareTo(S.token()) <= 0��ʾPLUSEQ.ordinal<=S.token().ordinal
        compareTo()������java.lang.Enum<E>����,����:
        public final int compareTo(E o) {
		Enum other = (Enum)o;
		Enum self = this;
		............
		return self.ordinal - other.ordinal;
	    }
        */
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        //���if����Ϊtrue˵����һ����ֵ���ʽ���
        if ((mode & EXPR) != 0 &&
            S.token() == EQ || PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0)
            return termRest(t);
        else
            return t;
            
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term()");
		}    
    }

    JCExpression termRest(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"termRest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        switch (S.token()) {
        case EQ: {
            int pos = S.pos();
            S.nextToken();
            mode = EXPR;
            /*ע��������term()��������term1()�������﷨:
            Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			�о�Ӧ��term1()�Ŷԣ���Ϊjava����������a=b=c=d�������﷨,
			���԰�ExpressionRest = [AssignmentOperator Expression1]
			����  ExpressionRest = [AssignmentOperator Expression]
			����ֱ��������һ���﷨:
			Expression = Expression1 {AssignmentOperator Expression1}
			�滻
			Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			�����ַ�ʽ����ԭ���ĺ����
			
			������
			Java Language Specification, Third Edition
			18.1. The Grammar of the Java Programming Language
			�еĶ�������:
			   Expression:
      		   Expression1 [AssignmentOperator Expression1]]
      		   
      		��]]���е�Ī�������֪���ǲ��Ƕ���˸���]��
			*/
            JCExpression t1 = term();
            return toP(F.at(pos).Assign(t, t1));
        }
        case PLUSEQ:
        case SUBEQ:
        case STAREQ:
        case SLASHEQ:
        case PERCENTEQ:
        case AMPEQ:
        case BAREQ:
        case CARETEQ:
        case LTLTEQ:
        case GTGTEQ:
        case GTGTGTEQ:
            int pos = S.pos();
            Token token = S.token();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term(); //ͬ��
            return F.at(pos).Assignop(optag(token), t, t1);
        default:
            return t;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"termRest(JCExpression t)");
		}  
    }