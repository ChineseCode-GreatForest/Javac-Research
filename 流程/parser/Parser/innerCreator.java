    /** InnerCreator = Ident [TypeArguments] ClassCreatorRest
     */
    JCExpression innerCreator(int newpos, List<JCExpression> typeArgs, JCExpression encl) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"innerCreator(3)");
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("encl="+encl);
		
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        return classCreatorRest(newpos, encl, typeArgs, t);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"innerCreator(3)");
		}
    }