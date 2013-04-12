    /** TypeDeclaration = ClassOrInterfaceOrEnumDeclaration
     *                  | ";"
     */
    JCTree typeDeclaration(JCModifiers mods) {
        try {//�Ҽ��ϵ�
        DEBUG.P(this,"typeDeclaration(1)");
        if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        else DEBUG.P("mods=null");
        DEBUG.P("S.token()="+S.token()+"  S.pos()="+S.pos());

        int pos = S.pos();

		//�����ġ�;"��ǰ�治�������η�
        if (mods == null && S.token() == SEMI) {
            S.nextToken();
            return toP(F.at(pos).Skip());
        } else {
            String dc = S.docComment();
			DEBUG.P("dc="+dc);
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }


        }finally{//�Ҽ��ϵ�
        DEBUG.P(2,this,"typeDeclaration(1)");
        }
    }