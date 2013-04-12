    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     */
    JCTree importDeclaration() {
    	DEBUG.P(this,"importDeclaration()");
        int pos = S.pos();//���һ����import���token�Ŀ�ʼλ��
		DEBUG.P("pos="+pos);
        S.nextToken();
        boolean importStatic = false;
        if (S.token() == STATIC) {
            checkStaticImports();
            importStatic = true;
            S.nextToken();
        }

		//����ǡ�import my.test;������ô����õ���pid�Ŀ�ʼλ����my���token��pos
		//pid�Ľ���λ����my���token��endpos��
		//��ӦnextToken(157,159)=|my|�е�(157,159)
        JCExpression pid = toP(F.at(S.pos()).Ident(ident()));
        do {
            int pos1 = S.pos();
            accept(DOT);
            if (S.token() == STAR) {
                pid = to(F.at(pos1).Select(pid, names.asterisk));//���롰.*"�����
                S.nextToken();
                break;
            } else {
				DEBUG.P("pos1="+pos1);
				//����ǡ�import my.test;������ô����õ���pid��һ��JCFieldAccess
				//���Ŀ�ʼλ���ǡ�.����pos������λ����test���token��endpos
                pid = toP(F.at(pos1).Select(pid, ident()));
            }
        } while (S.token() == DOT);
        accept(SEMI);
        DEBUG.P(2,this,"importDeclaration()");
		//����ǡ�import my.test;������ô����õ���pid��һ��JCImport
		//���Ŀ�ʼλ���ǡ�import����pos������λ����";"���token��endpos
        return toP(F.at(pos).Import(pid, importStatic));
    }