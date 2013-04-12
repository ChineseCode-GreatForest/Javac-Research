    /** Read fractional part of hexadecimal floating point number.
     */
    private void scanHexExponentAndSuffix() {
    	//16���Ƹ���ָ������(ע:p(��P)������ָ��,����ʡ��,�����float������f(��F)Ҳ�Ǳ����)
        if (ch == 'p' || ch == 'P') {
	    	putChar(ch);
            scanChar();
            
            if (ch == '+' || ch == '-') {
				putChar(ch);
                scanChar();
	    	}
	    	
		    if ('0' <= ch && ch <= '9') {
				do {
				    putChar(ch);
				    scanChar();
				} while ('0' <= ch && ch <= '9');
				
				if (!allowHexFloats) {
					//����:0x.1p-1f����ָ��ѡ��:-source 1.4ʱ
					//����:�� -source 5 ֮ǰ����֧��ʮ�����Ƹ�������ֵ
				    lexError("unsupported.fp.lit");
		            allowHexFloats = true;
		        }
		        else if (!hexFloatsWork)
					//�� VM ��֧��ʮ�����Ƹ�������ֵ
				    lexError("unsupported.cross.fp.lit");
		    } else
				//��:0x.1p-wf���ַ�w��������0-9������������:��������ֵ������
				//�������:0x.1p-2wf����Ȼ�ַ�w��������0-9�����������ﱨ��
				//����ֻ���+-�ź�����ַ��Ƿ�������
				lexError("malformed.fp.lit");
				
		} else {
			//��:0x.1-1f�������ַ�p(��P)������������:��������ֵ������
		    lexError("malformed.fp.lit");
		}
		if (ch == 'f' || ch == 'F') {
		    putChar(ch);
		    scanChar();
	        token = FLOATLITERAL;
		} else {
			/*
			�����������û��ָ����׺f(��F)����ô������������˫���ȵģ�
			��ʱ���������ֵ��һ��float���͵��ֶΣ��������������ط�(Check����)���飬
			�磺public float myFloat2=0x.1p-2;

			������ʾ����:

			bin\mysrc\my\test\ScannerTest.java:9: ������ʧ����
			�ҵ��� double
			��Ҫ�� float
					public float myFloat2=0x.1p-2;
										  ^
			1 ����
			*/
		    if (ch == 'd' || ch == 'D') {
				putChar(ch);
				scanChar();
		    }
		    token = DOUBLELITERAL;
		}
    }