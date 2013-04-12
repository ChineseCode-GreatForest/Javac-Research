//asSuper
    // <editor-fold defaultstate="collapsed" desc="asSuper">
    /**
     * Return the (most specific) base type of t that starts with the
     * given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    //��t��ʼ���ϲ���t�ļ̳�����ʵ������ֱ���ҵ���һ��type�����type.tsym��sym
	//ָ��ͬһ��Symbol(Ҳ����type.tsym==sym)����󷵻����type���Ҳ���ʱ����null
    public Type asSuper(Type t, Symbol sym) {
        //return asSuper.visit(t, sym);

		DEBUG.P(this,"asSuper(Type t, Symbol sym)");
		//DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		//DEBUG.P("sym="+sym);

		Type returnType = asSuper.visit(t, sym);
            
		//DEBUG.P("returnType="+returnType);

		DEBUG.P("t="+t);
		DEBUG.P("sym="+sym);
		DEBUG.P("��t�ļ̳�����Ѱ��sym�õ� asSuper="+returnType);
		DEBUG.P(1,this,"asSuper(Type t, Symbol sym)");
		return returnType;
    }
    // where
        private SimpleVisitor<Type,Symbol> asSuper = new SimpleVisitor<Type,Symbol>() {

            public Type visitType(Type t, Symbol sym) {
                return null;
            }

            @Override
            public Type visitClassType(ClassType t, Symbol sym) {
                if (t.tsym == sym)
                    return t;

                Type st = supertype(t);
                if (st.tag == CLASS || st.tag == ERROR) {
                    Type x = asSuper(st, sym);
                    if (x != null)
                        return x;
                }
                if ((sym.flags() & INTERFACE) != 0) {
                    for (List<Type> l = interfaces(t); l.nonEmpty(); l = l.tail) {
                        Type x = asSuper(l.head, sym);
                        if (x != null)
                            return x;
                    }
                }
                return null;
            }

            @Override
            public Type visitArrayType(ArrayType t, Symbol sym) {
                return isSubtype(t, sym.type) ? sym.type : null;
            }

            @Override
            public Type visitTypeVar(TypeVar t, Symbol sym) {
                return asSuper(t.bound, sym);
            }

            @Override
            public Type visitErrorType(ErrorType t, Symbol sym) {
                return t;
            }
        };

    /**
     * Return the base type of t or any of its outer types that starts
     * with the given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    //�ȴ�t��ʼ���ϲ���t�ļ̳�����ʵ������ֱ���ҵ���һ��type�����type.tsym��sym
	//ָ��ͬһ��Symbol(Ҳ����type.tsym==sym)���ҵ��򷵻����type������Ҳ�������
	//t�л���t��outer_field���������Ұ�ǰ��ķ�ʽ���ң�ֱ��t��outer_field.tag����CLASSΪֹ
    public Type asOuterSuper(Type t, Symbol sym) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"asOuterSuper(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym);
		
        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
                if (s != null) return s;
                t = t.getEnclosingType();
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"asOuterSuper(Type t, Symbol sym)");
		}
    }

    /**
     * Return the base type of t or any of its enclosing types that
     * starts with the given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    public Type asEnclosingSuper(Type t, Symbol sym) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"asEnclosingSuper(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym);

        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
				DEBUG.P("s="+s);
                if (s != null) return s;
                Type outer = t.getEnclosingType();
				DEBUG.P("outer="+outer+" outer.tag="+TypeTags.toString(outer.tag));
                t = (outer.tag == CLASS) ? outer :
                    (t.tsym.owner.enclClass() != null) ? t.tsym.owner.enclClass().type :
                    Type.noType;
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }

		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"asEnclosingSuper(Type t, Symbol sym)");
		}
    }
    // </editor-fold>
//