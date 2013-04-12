    /**  TypeArgumentsOpt = [ TypeArguments ]
     */
    JCExpression typeArgumentsOpt(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgumentsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		/*��������ǲ���������������
		class MemberClassH<T> {}
		MemberClassH<?> Mh1;
		MemberClassH<String> Mh2;
		MemberClassH<? extends Number> Mh3;
		*/
		
        if (S.token() == LT &&
            (mode & TYPE) != 0 &&
            (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeArguments(t);
        } else {
            return t;
        }

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgumentsOpt(JCExpression t)");
		}       
    }
    
    List<JCExpression> typeArgumentsOpt() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgumentsOpt()");
		
        return typeArgumentsOpt(TYPE);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgumentsOpt()");
		}
    }

    List<JCExpression> typeArgumentsOpt(int useMode) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"typeArgumentsOpt(int useMode)");
        DEBUG.P("useMode="+myMode(useMode));
        DEBUG.P("mode="+myMode(mode));
        DEBUG.P("S.token()="+S.token());

        if (S.token() == LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeArguments();
        }
        return null;
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"typeArgumentsOpt(int useMode)");
        }
    }

    /**  TypeArguments  = "<" TypeArgument {"," TypeArgument} ">"
     */
    List<JCExpression> typeArguments() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArguments()");
		DEBUG.P("S.token()="+S.token()+" mode="+myMode(mode));
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LT) {
            S.nextToken();
            //TypeArguments���������� expr=<?>
            
            //ֻ��mode����EXPRʱ((mode & EXPR) == 0)��
            //�����ڡ�<>���з��롰������
            args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            while (S.token() == COMMA) {
                S.nextToken();
                args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            }
            switch (S.token()) {
            case GTGTGTEQ:
                S.token(GTGTEQ);
                break;
            case GTGTEQ:
                S.token(GTEQ);
                break;
            case GTEQ:
                S.token(EQ);
                break;
            case GTGTGT:
                S.token(GTGT);
                break;
            case GTGT:
                S.token(GT);
                break;
            default:
                accept(GT);
                break;
            }
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LT));
        }
        return args.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArguments()");
		}
    }

    /** TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     */
     
     /*
     ��Java Language Specification, Third Edition
	 18.1. The Grammar of the Java Programming Language
	 �еĶ�������:
     TypeArgument:
      Type
      ? [( extends | super ) Type]
     ����������﷨�Ǵ���ġ�
     "?" EXTENDS Type {"&" Type} Ӧ�ĳ� "?" EXTENDS Type
     */
    JCExpression typeArgument() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgument()");
		
        if (S.token() != QUES) return type();
		//����JCWildcard�����Ŀ�ʼλ��pos�Ǵ�"?"�����token�Ŀ�ʼλ�������
        int pos = S.pos();
        S.nextToken();
        if (S.token() == EXTENDS) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.EXTENDS));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == SUPER) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.SUPER));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == IDENTIFIER) {
			/*����:
			class MemberClassH<T> {}
			MemberClassH<? mh;
			*/
            //error recovery
            reportSyntaxError(S.prevEndPos(), "expected3",
                    keywords.token2string(GT),
                    keywords.token2string(EXTENDS),
                    keywords.token2string(SUPER));
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            JCExpression wc = toP(F.at(pos).Wildcard(t, null));
            JCIdent id = toP(F.at(S.pos()).Ident(ident()));
            return F.at(pos).Erroneous(List.<JCTree>of(wc, id));
        } else {
			/*���������������:
			class MemberClassH<T> {}
			MemberClassH<? <;

			��ô����������ﲢ��������������UNBOUND���͵�JCWildcard��
			���ǽ����Ϸ���"<"�ַ�����������������ĵ��������д���
			����ͨ��typeArguments()�����������ʱ����typeArguments()���
			"default:
                accept(GT);"��δ�����ͻᱨ��"��Ҫ >"�����Ĵ�����ʾ
			*/
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            return toP(F.at(pos).Wildcard(t, null));
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgument()");
		}
    }

    JCTypeApply typeArguments(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArguments(JCExpression t)");
		
        int pos = S.pos();
        List<JCExpression> args = typeArguments();
        return toP(F.at(pos).TypeApply(t, args));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArguments(JCExpression t)");
		}
    }