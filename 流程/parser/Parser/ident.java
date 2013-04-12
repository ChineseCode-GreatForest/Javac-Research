    /**
     * Ident = IDENTIFIER
     */
    Name ident() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"ident()");
		
        if (S.token() == IDENTIFIER) {
            Name name = S.name();
            DEBUG.P("ident.name="+name);
            S.nextToken();
            return name;
        } else if (S.token() == ASSERT) {
            if (allowAsserts) {
            	/*
            	��:
                F:\Javac\bin\other>javac Test5.java
                Test5.java:4: �Ӱ汾 1.4 ��ʼ��'assert' ��һ���ؼ��֣�������������ʶ��
                ����ʹ�� -source 1.3 ����Ͱ汾�Ա㽫 'assert' ������ʶ����
                        int assert=0;
                            ^
                1 ����
                */
                log.error(S.pos(), "assert.as.identifier");
                S.nextToken();
                return names.error;//error��com.sun.tools.javac.util.Name.Table�ж���
            } else {
            	/*
            	��:
            	F:\Javac\bin\other>javac -source 1.3 Test5.java
                Test5.java:4: ���棺�Ӱ汾 1.4 ��ʼ��'assert' ��һ���ؼ��֣�������������ʶ��
                ����ʹ�� -source 1.4 ����߰汾�Ա㽫 'assert' �����ؼ��֣�
                                int assert=0;
                                    ^
                1 ����
                */
                log.warning(S.pos(), "assert.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else if (S.token() == ENUM) {
        	//��ASSERT����
            if (allowEnums) {
                log.error(S.pos(), "enum.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "enum.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else {
            accept(IDENTIFIER);
            return names.error;
        }

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"ident()");
		}        
	}