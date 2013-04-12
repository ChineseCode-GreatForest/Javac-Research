    /** Emit a byte of code.
     */
    private  void emit1(int od) {
        if (!alive) return;
		if (cp == code.length) {
			byte[] newcode = new byte[cp * 2];
			//����code.length=100����Ӧcode���������Ŵ�0��99��
			//��cp��0��ʼ��������code���������Ŵ�0��99����������ʱ��
			//cp��ֵҲ�����100������arraycopyҪcopy 100�����ݵ�newcode
			System.arraycopy(code, 0, newcode, 0, cp);
			code = newcode;
		}
		code[cp++] = (byte)od;
    }

    /** Emit two bytes of code.
     */
    private void emit2(int od) {
        if (!alive) return;
		/*
		int od��4�ֽڵ�(Ҳ����һ��intռ32 bit)����emit2(int od)��
		�ɵĹ�����Ҫ��code�����з��������ֽ�(2 byte=16 bit)����������
		�ֽ���int od�ĵ�16λ��code������һ���ֽ����飬���Ե÷����ΰ���
		�����ֽڷ���code���飬���Ȱ�15--8bitλ����һ�ֽڣ��������
		�еġ�(byte)(od >> 8)����od�����ƶ�8λ�����൱�ڰ�ԭ����15--8bitλ
		���7--0bitλ������int��ֵǿ��ת����byteʱ��Ĭ��ȡint��ֵ�ĵ�8λ��
		���8λҲ����ԭ����15--8bitλ��ִ���ꡰ(byte)(od >> 8)����Ҳ�Ͱѵ�һ��
		��λ�ֽڼ�����code�����У�����ʱod��ֵû�䣬����(byte)od����ȡ��8λ��Ҳ
		���ǵڶ�����λ�ֽ�
		*/
		if (cp + 2 > code.length) {//���ﲻ��>=����Ϊemit1����������==
			
			emit1(od >> 8);//��λ��ǰ(Ҳ���Ǹ�8λ��code�����е��±�ȵ�8λС)
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }

    /** Emit four bytes of code.
     */
    public void emit4(int od) {
        if (!alive) return;
		//�ο�����emit2(int od)��ע�ͣ�ֻ���������Ψһ������ĸ��ֽ�
		if (cp + 4 > code.length) {
			emit1(od >> 24);
			emit1(od >> 16);
			emit1(od >> 8);
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 24);
			code[cp++] = (byte)(od >> 16);
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }