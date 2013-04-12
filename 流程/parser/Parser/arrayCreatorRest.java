    /** ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    JCExpression arrayCreatorRest(int newpos, JCExpression elemtype) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"arrayCreatorRest(2)");
        DEBUG.P("newpos="+newpos);
        DEBUG.P("elemtype="+elemtype);
        
        accept(LBRACKET);
        if (S.token() == RBRACKET) {
            accept(RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (S.token() == LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                //��:int a[]=new int[];
                //src/my/test/ParserTest.java:6: ȱ������ά��
                //int a[]=new int[];
                //                 ^

                return syntaxError(S.pos(), "array.dimension.missing");
            }
        } else {
            //��ָ��������ά����Ͳ����ô�����'{}'��������г�ʼ����
            //�����������������﷨:
            //int a[]=new int[2]{1,2};
            //int b[][]=new int[2][3]{{1,2,3},{4,5,6}};
            
            ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
            //��:int a[]=new int[8][4];
            dims.append(expression());
            accept(RBRACKET);
            while (S.token() == LBRACKET) {
                int pos = S.pos();
                S.nextToken();
				//int b[][]=new int[2][];      //�޴�
				//int c[][][]=new int[2][][3]; //�д�
				//��һά����Ĵ�С����ָ����������......ά֮��Ŀ�����[][][]
                if (S.token() == RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims.append(expression());
                    accept(RBRACKET);
                }
            }
            DEBUG.P("dims.toList()="+dims.toList());
            DEBUG.P("elemtype="+elemtype);
            return toP(F.at(newpos).NewArray(elemtype, dims.toList(), null));
        }
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"arrayCreatorRest(2)");
        }
    }