//makeCompoundType
    // <editor-fold defaultstate="collapsed" desc="makeCompoundType">
    /**
     * Make a compound type from non-empty list of types
     *
     * @param bounds            the types from which the compound type is formed
     * @param supertype         is objectType if all bounds are interfaces,
     *                          null otherwise.
     */
    public Type makeCompoundType(List<Type> bounds,
                                 Type supertype) {
        DEBUG.P(this,"makeCompoundType(2)");  
        DEBUG.P("bounds="+bounds);
        DEBUG.P("supertype="+supertype);  
		
		//���javac�����м��ˡ�-moreInfo��ѡ��ʱ��ClassSymbol��name
		//����bounds���ַ���������Ϊ�մ�
        ClassSymbol bc =
        //����һ��UNATTRIBUTED��־����
        //com.sun.tools.javac.comp.Attr===>visitTypeParameter(1)����
            new ClassSymbol(ABSTRACT|PUBLIC|SYNTHETIC|COMPOUND|ACYCLIC,
                            Type.moreInfo
                                ? names.fromString(bounds.toString())
                                : names.empty,
                            syms.noSymbol);
        //ע��:�ڵ��õ�makeCompoundTypeʱ�����������﷨T extends V&InterfaceA
		//������ģ�ֻ�ǵ��˺�������׶��ǲż������ͱ���V���ܸ��������Ʒ�Χ
        if (bounds.head.tag == TYPEVAR)
            // error condition, recover
            bc.erasure_field = syms.objectType;
        else //CompoundType��erasure_fieldȡ��һ��bound��erasure����
            bc.erasure_field = erasure(bounds.head);
        DEBUG.P("ClassSymbol bc.name="+bc.name); 
        DEBUG.P("bc.erasure_field="+bc.erasure_field);  
        bc.members_field = new Scope(bc);
        ClassType bt = (ClassType)bc.type;
        bt.allparams_field = List.nil();
        if (supertype != null) {
            bt.supertype_field = supertype;
            bt.interfaces_field = bounds;
        } else {
            bt.supertype_field = bounds.head;
            bt.interfaces_field = bounds.tail;
        }
		DEBUG.P("bt.supertype_field.tsym.completer="+bt.supertype_field.tsym.completer);  
        assert bt.supertype_field.tsym.completer != null
            || !bt.supertype_field.isInterface()
            : bt.supertype_field;
        /*
		������<V extends InterfaceTest & InterfaceTest2>�����ķ��Ͷ���
		����������:
		------------------------------------
		ClassSymbol bc.name=my.test.InterfaceTest,my.test.InterfaceTest2
		bc.erasure_field=my.test.InterfaceTest
		bt.supertype_field=java.lang.Object
		bt.interfaces_field=my.test.InterfaceTest,my.test.InterfaceTest2
		------------------------------------
		Ҳ����˵�����ͱ�����bounds���ǽӿ�(��������������)ʱ��
		��ô������ͱ�����ClassType��Compound���͵ģ�
		ClassType.supertype_field��java.lang.Object��
		ClassType.interfaces_field��bounds�е����нӿ�
		������ͱ�����Ӧ��ClassSymbol��erasure_field��bounds�еĵ�һ���ӿڡ�

		���Կ��԰ѷ��Ͷ���<V extends InterfaceTest&InterfaceTest2>����
		����<V extends Object & InterfaceTest & InterfaceTest2>
		*/
        DEBUG.P("bt.supertype_field="+bt.supertype_field);  
        DEBUG.P("bt.interfaces_field="+bt.interfaces_field);  
        DEBUG.P("return bt="+bt);
        DEBUG.P(0,this,"makeCompoundType(2)");  
        return bt;
    }

    /**
     * Same as {@link #makeCompoundType(List,Type)}, except that the
     * second parameter is computed directly. Note that this might
     * cause a symbol completion.  Hence, this version of
     * makeCompoundType may not be called during a classfile read.
     */
    public Type makeCompoundType(List<Type> bounds) {
        Type supertype = (bounds.head.tsym.flags() & INTERFACE) != 0 ?
            supertype(bounds.head) : null;
        return makeCompoundType(bounds, supertype);
    }

    /**
     * A convenience wrapper for {@link #makeCompoundType(List)}; the
     * arguments are converted to a list and passed to the other
     * method.  Note that this might cause a symbol completion.
     * Hence, this version of makeCompoundType may not be called
     * during a classfile read.
     */
    public Type makeCompoundType(Type bound1, Type bound2) {
        return makeCompoundType(List.of(bound1, bound2));
    }
    // </editor-fold>
//