//erasure
    // <editor-fold defaultstate="collapsed" desc="erasure">
    /**
     * The erasure of t {@code |t|} -- the type that results when all
     * type parameters in t are deleted.
     */
    /**
		��νerasure�����ǽ�type parametersȥ��������Test<T>��erasure��ͱ�ΪTest��
		���erasure������ܣ�ʵ��������Type�༰��������Ӧ��map(Mapping f)����ʵ��
		��(ClassType����,ClassType��ClassSymbol.erasure(Types types)����ʵ��)��
		���Type�༰�������type parameters����type parametersȥ��������
		��ԭ����Type�༰�������ʵ�����ֶ�����һ����Ӧ��ʵ�����͵õ�erasure�������
		
		ע:�����ClassType��flags��COMPOUND����ôerasure��makeCompoundType����
		���Ѿ��������ã�������ClassSymbol.erasure(Types types)����ʱ�Ϳ�ֱ�ӷ���
		erasure�������
		
		
		��:<E extends ExtendsTest&InterfaceTest>���������ͱ���E��erasure�������
		
		����������:
		
		com.sun.tools.javac.code.Types===>erasure(Type t)
		-------------------------------------------------------------------------
		t.tag=(TYPEVAR)14  lastBaseTag=8
		com.sun.tools.javac.code.Types===>erasure(Type t)
		-------------------------------------------------------------------------
		t.tag=(CLASS)10  lastBaseTag=8
		com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)
		-------------------------------------------------------------------------
		erasure_field=my.ExtendsTest  //erasure_field�Ѿ�����
		com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)  END
		-------------------------------------------------------------------------
		t=my.ExtendsTest,my.InterfaceTest  erasureType=my.ExtendsTest
		com.sun.tools.javac.code.Types===>erasure(Type t)  END
		-------------------------------------------------------------------------
		t=E23195919  erasureType=my.ExtendsTest
		com.sun.tools.javac.code.Types===>erasure(Type t)  END
		-------------------------------------------------------------------------
    */
    public Type erasure(Type t) {
    	//if (t.tag <= lastBaseTag)
        //    return t; /* fast special case */
        //else
        //    return erasure.visit(t);

		DEBUG.P(this,"erasure(Type t)");
		DEBUG.P("t="+t+"  t.tag=("+TypeTags.toString(t.tag)+")"+t.tag+"  lastBaseTag="+lastBaseTag);
		
		Type returnType;
		//lastBaseTag=BOOLEAN��Ҳ����8���������Ͳ���erasure
        if (t.tag <= lastBaseTag)
            returnType = t; 
        else
            returnType =  erasure.visit(t);
            
		DEBUG.P("t="+t+"  erasureType="+returnType);
		DEBUG.P(1,this,"erasure(Type t)");
		return returnType;
    }
    // where
        private UnaryVisitor<Type> erasure = new UnaryVisitor<Type>() {
            public Type visitType(Type t, Void ignored) {
                if (t.tag <= lastBaseTag)
                    return t; /*fast special case*/
                else
                    return t.map(erasureFun);
            }

            @Override
            public Type visitWildcardType(WildcardType t, Void ignored) {
                //return erasure(upperBound(t));
                
                try {//�Ҽ��ϵ�
				DEBUG.P(this,"erasure==>visitWildcardType(2)");
                
                return erasure(upperBound(t));
                
                }finally{//�Ҽ��ϵ�
				DEBUG.P(0,this,"erasure==>visitWildcardType(2)");
				}
            }

            @Override
            public Type visitClassType(ClassType t, Void ignored) {
                //return t.tsym.erasure(Types.this);
                try {//�Ҽ��ϵ�
				DEBUG.P(this,"erasure==>visitClassType(2)");
                
                return t.tsym.erasure(Types.this);
                
                }finally{//�Ҽ��ϵ�
				DEBUG.P(0,this,"erasure==>visitClassType(2)");
				}
            }
            /*
            ����Դ��:
            class ClassA {}
			public class Test<T extends ClassA,E extends T>{}
            
            ������:
            com.sun.tools.javac.code.Types===>erasure(Type t)
			-------------------------------------------------------------------------
			t=T{ bound=my.test.ClassA }  t.tag=(TYPEVAR)14  lastBaseTag=8
			com.sun.tools.javac.code.Types$16===>erasure==>visitTypeVar(2)
			-------------------------------------------------------------------------
			com.sun.tools.javac.code.Types===>erasure(Type t)
			-------------------------------------------------------------------------
			t=my.test.ClassA  t.tag=(CLASS)10  lastBaseTag=8
			com.sun.tools.javac.code.Types$16===>visitClassType(2)
			-------------------------------------------------------------------------
			com.sun.tools.javac.code.Types$16===>visitClassType(2)  END
			-------------------------------------------------------------------------
			
			t=my.test.ClassA  erasureType=my.test.ClassA
			com.sun.tools.javac.code.Types===>erasure(Type t)  END
			-------------------------------------------------------------------------
			
			com.sun.tools.javac.code.Types$16===>erasure==>visitTypeVar(2)  END
			-------------------------------------------------------------------------
			
			t=T{ bound=my.test.ClassA }  erasureType=my.test.ClassA
			com.sun.tools.javac.code.Types===>erasure(Type t)  END
			-------------------------------------------------------------------------
			*/
            @Override
            public Type visitTypeVar(TypeVar t, Void ignored) {
            	try {//�Ҽ��ϵ�
				DEBUG.P(this,"erasure==>visitTypeVar(2)");
                
                return erasure(t.bound);
                
                }finally{//�Ҽ��ϵ�
				DEBUG.P(0,this,"erasure==>visitTypeVar(2)");
				}
            }

            @Override
            public Type visitErrorType(ErrorType t, Void ignored) {
                return t;
            }
        };
    private Mapping erasureFun = new Mapping ("erasure") {
            public Type apply(Type t) { return erasure(t); }
        };

    public List<Type> erasure(List<Type> ts) {
        return Type.map(ts, erasureFun);
    }
    // </editor-fold>
//