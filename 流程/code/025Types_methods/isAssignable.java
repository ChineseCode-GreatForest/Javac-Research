//isAssignable
    // <editor-fold defaultstate="collapsed" desc="isAssignable">
    public boolean isAssignable(Type t, Type s) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"isAssignable(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        return isAssignable(t, s, Warner.noWarnings);

		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"isAssignable(2)");
		}
    }

    /**
     * Is t assignable to s?<br>
     * Equivalent to subtype except for constant values and raw
     * types.<br>
     * (not defined for Method and ForAll types)
     */
	//ע�⸳ֵ(isAssignable)��ͬ��ǿ��ת��(isCastable)
	//��ֵֻ�������ำ�����࣬�������ǳ��ำ������
	//��:
	/*
		Integer aInteger = 10;
		Number aNumber=10;
		aNumber=aInteger;//��ȷ
		aInteger=aNumber;//����

		//��������ǿ��ת�����Ϸ�
		aNumber=(Number)aInteger;
		aInteger=(Integer)aNumber;
	*/
    public boolean isAssignable(Type t, Type s, Warner warn) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"isAssignable(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

		boolean returnResult= myIsAssignable(t, s, warn);
            
		
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("returnResult="+returnResult);
		return returnResult;

	  /*
        if (t.tag == ERROR)
            return true;
        if (t.tag <= INT && t.constValue() != null) {
            int value = ((Number)t.constValue()).intValue();
            switch (s.tag) {
            case BYTE:
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
                    return true;
                break;
            case CHAR:
                if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                    return true;
                break;
            case SHORT:
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                    return true;
                break;
            case INT:
                return true;
            case CLASS:
                switch (unboxedType(s).tag) {
                case BYTE:
                case CHAR:
                case SHORT://����Integer aInteger = 10;ʱΪINT��
						   //��������ʡ���ˣ�INT�����ת��return isConvertible(t, s, warn);
                    return isAssignable(t, unboxedType(s), warn);
                }
                break;
            }
        }
        return isConvertible(t, s, warn);
	  */
		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"isAssignable(3)");
		}
    }

	private boolean myIsAssignable(Type t, Type s, Warner warn) {
        if (t.tag == ERROR)
            return true;
        if (t.tag <= INT && t.constValue() != null) {
            int value = ((Number)t.constValue()).intValue();
            switch (s.tag) {
            case BYTE:
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
                    return true;
                break;
            case CHAR:
                if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                    return true;
                break;
            case SHORT:
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                    return true;
                break;
            case INT:
                return true;
            case CLASS:
                switch (unboxedType(s).tag) {
                case BYTE:
                case CHAR:
                case SHORT:
                    return isAssignable(t, unboxedType(s), warn);
                }
                break;
            }
        }
        return isConvertible(t, s, warn);
    }
    // </editor-fold>
//