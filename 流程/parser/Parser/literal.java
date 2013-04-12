    /**
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */

     //Ϊʲôû��byte,short�أ���Ϊ��Scanner�з������ֻ��ַ��������������������ģ�
     //ֻ�ǵ���������ֵ����������û��byte,short����������ֵ(LITERAL)
    JCExpression literal(Name prefix) {
    	DEBUG.P(this,"literal(Name prefix)");
    	DEBUG.P("prefix="+prefix);
    	//prefix��ָ��������(Literal)��ǰ׺,���Ƿ������(-)
    	
        int pos = S.pos();
        JCExpression t = errorTree;

        switch (S.token()) {
        case INTLITERAL:
            try {
            	//��ȫ�޶�����:com.sun.tools.javac.code.TypeTags
            	//��ȫ�޶�����:com.sun.tools.javac.util.Convert
                t = F.at(pos).Literal(
                    TypeTags.INT,
                    Convert.string2int(strval(prefix), S.radix()));
            } catch (NumberFormatException ex) {
            	/*��������:
            	bin\mysrc\my\test\Test3.java:29: ����������� 099
		        public final int c=099;
		                           ^
		        */                   
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case LONGLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.LONG,
                    new Long(Convert.string2long(strval(prefix), S.radix())));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case FLOATLITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Float n;
            try {
				//�����ʷ�������proper����ĸ�������ʽ�϶�����ȷ�ģ�
				//���Ǵʷ�����ʱ����֪����������ֵ�Ƿ��С���ǹ���
				//�����С����ôFloat.valueOf(proper)���Ƿ���0.0f��
				//����������0.0f�޷����֣�����������ͨ��!isZero(proper)���жϣ�
				//���proper("0x"����)�е�ÿ���ַ�ֻҪ��һ������0��'.'�ţ�
				//��һ���ǹ�С�ĸ�����
				//���⣬���ڹ���ĸ�������Float.valueOf(proper)���Ƿ���Float.POSITIVE_INFINITY
                n = Float.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already repoted in scanner
                n = Float.NaN;
            }
            if (n.floatValue() == 0.0f && !isZero(proper)) //��:float f1=1.1E-33333f;
                log.error(S.pos(), "fp.number.too.small");
            else if (n.floatValue() == Float.POSITIVE_INFINITY) //��:float f2=1.1E+33333f;
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.FLOAT, n);
            break;
        }
        case DOUBLELITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Double n;
            try {
                n = Double.valueOf(proper); //ͬ��
            } catch (NumberFormatException ex) {
                // error already reported in scanner
                n = Double.NaN;
            }
            if (n.doubleValue() == 0.0d && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.doubleValue() == Double.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.DOUBLE, n);
            break;
        }
        case CHARLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CHAR,
                S.stringVal().charAt(0) + 0); //ע������ַ�ת��������,Literal�������յ���Integer����
            break;
        case STRINGLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CLASS,
                S.stringVal());
            break;
        case TRUE: case FALSE:
            t = F.at(pos).Literal(
                TypeTags.BOOLEAN,
                (S.token() == TRUE ? 1 : 0));
            break;
        case NULL:
            t = F.at(pos).Literal(
                TypeTags.BOT,
                null);
            break;
        default:
            assert false;
        }
        if (t == errorTree)
            t = F.at(pos).Erroneous();
        storeEnd(t, S.endPos());
        S.nextToken();
        
        DEBUG.P("return t="+t);
        DEBUG.P(0,this,"literal(Name prefix)");
        return t;
    }
//where
        boolean isZero(String s) {
            char[] cs = s.toCharArray();
            int base = ((Character.toLowerCase(s.charAt(1)) == 'x') ? 16 : 10);
            int i = ((base==16) ? 2 : 0);
            while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) i++;
            return !(i < cs.length && (Character.digit(cs[i], base) > 0));
        }

        String strval(Name prefix) {
        	//��������(Literal)��Scanner�б�����
        	//�ַ����������ʱ�����ַ�������
            String s = S.stringVal();
            return (prefix.len == 0) ? s : prefix + s;
        }