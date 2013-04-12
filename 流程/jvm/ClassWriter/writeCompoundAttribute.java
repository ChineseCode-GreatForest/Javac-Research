    /** Write a compound attribute excluding the '@' marker. */
    void writeCompoundAttribute(Attribute.Compound c) {
		DEBUG.P(this,"writeCompoundAttribute(1)");
		DEBUG.P("c="+c);
                //u2 ע������ȫ�޶����ڳ������е�����
                //u2 ע�������ֶθ���
                //������ע�������ֶα�(����ע�������ֶθ���)
                
                //ע�������ֶα�ÿ�������������:
                //u2 ע�������ֶ��������ڳ������е�����
                //u1 ע�������ֶε����ࣨB����boolean,s����String,e����Enum
                //c����Class,@�����ֶλ���ע�����ͣ��۴�������
                //���忴�����visitXXX����

        databuf.appendChar(pool.put(typeSig(c.type)));
        databuf.appendChar(c.values.length());
        DEBUG.P("c.values="+c.values);
        DEBUG.P("c.values.length()="+c.values.length());
        for (Pair<Symbol.MethodSymbol,Attribute> p : c.values) {
			DEBUG.P("p.snd.getClass().getName()="+p.snd.getClass().getName());
            databuf.appendChar(pool.put(p.fst.name));
            p.snd.accept(awriter);
        }

		DEBUG.P(0,this,"writeCompoundAttribute(1)");
    }