    /**
     * Qualident = Ident { DOT Ident }
     */
    public JCExpression qualident() {
    	DEBUG.P(this,"qualident()");
    	//ע����������F.at(S.pos())��Ȼ���ٵ���ident()
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
		DEBUGPos(t);
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            
            /*
            //�õ�ǰpos����TreeMaker���pos,Ȼ������һ��JCFieldAccess��
            //�����ɵ�JCFieldAccessʵ����TreeMaker���pos�����Լ���pos
            //JCFieldAccess��Ident��������Ƕ��
            
            //�統Qualident =java.lang.Byteʱ��ʾΪ:
            JCFieldAccess {
            	Name name = "Byte";
            	JCExpression selected = {
            		JCFieldAccess {
            			Name name="lang";
            			JCExpression selected = {
				            JCIdent {
				            	Name name = "java";
				            }
				        }
				    }
				}
			}
			*/
            //DEBUG.P("pos="+pos);//�����pos��"."�ŵĿ�ʼλ��
            t = toP(F.at(pos).Select(t, ident()));
			//DEBUGPos(t);//������������Ŀ�ʼλ�����ǵ�һ��ident�Ŀ�ʼλ��
        }
        
        DEBUG.P("qualident="+t);
		DEBUGPos(t);
        DEBUG.P(0,this,"qualident()");
        return t;
    }