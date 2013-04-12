    /** Given a field, return its name.
     */
    Name fieldName(Symbol sym) {
		//����˽��(PRIVATE)��Ǳ���(PROTECTED)��ǹ���(PUBLIC)��Ա��
		//�������ƽ��ң�����sym.name.index����
        if (scramble && (sym.flags() & PRIVATE) != 0 ||
            scrambleAll && (sym.flags() & (PROTECTED | PUBLIC)) == 0) //����������ǲ���ȷ������Ա���д���
            return names.fromString("_$" + sym.name.index);
        else
            return sym.name;
    }