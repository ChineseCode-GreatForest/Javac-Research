    //ʲôʱ��õ�������������Ӵ����лָ��أ���S.pos() <= errorEndPos������
    //��ʲôʱ����ж�S.pos() <= errorEndPos����errorEndPos�п��ܸı���
    /** Skip forward until a suitable stop token is found.
     */
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"skip(4)");
		DEBUG.P("stopAtImport    ="+stopAtImport);
		DEBUG.P("stopAtMemberDecl="+stopAtMemberDecl);
		DEBUG.P("stopAtIdentifier="+stopAtIdentifier);
		DEBUG.P("stopAtStatement ="+stopAtStatement);

		while (true) {
			switch (S.token()) {
				case SEMI:
                    S.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                	//���֮ǰ�Ĵ������ڷ���import���ʱ���ֵ�,�������ɴ�
                	//nextToken()���ҵ����µĽ�IMPORT��token��˵���ҵ���
                	//һ���µ�import��䣬���ھͿ�������������
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
					if (stopAtIdentifier)
						return;
					break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            S.nextToken();
        }
        
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"skip(4)");
		}
    }