    /** Read longest possible sequence of special characters and convert
     *  to token.
     */
    private void scanOperator() {
	while (true) {
	    putChar(ch);
	    Name newname = names.fromChars(sbuf, 0, sp);
	    
	    //DEBUG.P("newname="+newname);
	    //���һ���ַ�����Ϊһ�������Ĳ�������һ���֣������ܵİ����ӵ��������У�
	    //������������ַ�ʹ��ԭ���Ĳ����������һ����ʶ���ˣ���ô������һ��
        //��:������ǰ�����Ĳ�����Ϊ��!="�����Ŷ����ַ���*������ˡ�!=*"������һ
        //����ʶ��(IDENTIFIER)�ˣ���ʱ�͵�������һ�񣬻�ԭ�ɡ�!="
        if (keywords.key(newname) == IDENTIFIER) {
			sp--;
			break;
	    }
	    
        name = newname;
        token = keywords.key(newname);
	    scanChar();
	    if (!isSpecial(ch)) break;
	}
    }