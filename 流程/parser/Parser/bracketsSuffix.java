    /** BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    JCExpression bracketsSuffix(JCExpression t) {
    	DEBUG.P(this,"bracketsSuffix(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
		//��:Class c=int[][].class;
        if ((mode & EXPR) != 0 && S.token() == DOT) {
            mode = EXPR;
            int pos = S.pos();
            S.nextToken();
            accept(CLASS);
            if (S.pos() == errorEndPos) {
                // error recovery
                Name name = null;
                if (S.token() == IDENTIFIER) {//��:Class c=int[][].classA;
                    name = S.name();
                    S.nextToken();
                } else {//��:Class c=int[][].char;//���Դ������δ����ֻ��һ��
                    name = names.error;
                }
				DEBUG.P("name="+name);
                t = F.at(pos).Erroneous(List.<JCTree>of(toP(F.at(pos).Select(t, name))));
            } else {
                t = toP(F.at(pos).Select(t, names._class));
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE; //ע������ ��:public int[][] i1={{1,2},{3,4}};
        } else {
			//��:Class c=int[][];
			//��:Class c=int[][].123;
            syntaxError(S.pos(), "dot.class.expected");
        }
        
        DEBUG.P(0,this,"bracketsSuffix(JCExpression t)");
        return t;
    }