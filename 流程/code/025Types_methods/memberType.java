//memberType
    // <editor-fold defaultstate="collapsed" desc="memberType">
    /**
     * The type of given symbol, seen as a member of t.
     *
     * @param t a type
     * @param sym a symbol
     */
	/*sym��t��һ����Ա(����:�������ֶΡ����캯��)������ڶ���tʱ��t�������ͱ�����
	t�ĳ�Ա�п�����������Щ���ͱ����������ڴ������Ͳ����������ʹ��tʱ��
	��������õ�t�����ͱ����ĳ�Ա�������Ͳ��������ʹ��tʱ���������Ͳ�������ô
	��ʹt�ĳ�Ա������t�����ͱ���������t�ĳ�Աʱ���ͱ����ᱻ����
	*/
    public Type memberType(Type t, Symbol sym) {
        //return (sym.flags() & STATIC) != 0
        //    ? sym.type
        //    : memberType.visit(t, sym);

		DEBUG.P(this,"memberType(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym+" sym.flags()="+Flags.toString(sym.flags()));
		
		Type returnType = (sym.flags() & STATIC) != 0
            ? sym.type
            : memberType.visit(t, sym);
            
		DEBUG.P("returnType="+returnType);
		DEBUG.P(1,this,"memberType(Type t, Symbol sym)");
		return returnType;
    }
    // where
        private SimpleVisitor<Type,Symbol> memberType = new SimpleVisitor<Type,Symbol>() {

            public Type visitType(Type t, Symbol sym) {
                return sym.type;
            }

            @Override
            public Type visitWildcardType(WildcardType t, Symbol sym) {
                return memberType(upperBound(t), sym);
            }

            @Override
            public Type visitClassType(ClassType t, Symbol sym) {
            	try {//�Ҽ��ϵ�
            	DEBUG.P(this,"visitClassType(2)");
				DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
				
                Symbol owner = sym.owner;
                long flags = sym.flags();
                DEBUG.P("sym="+sym+" sym.flags()="+Flags.toString(sym.flags()));
                DEBUG.P("owner="+owner+" owner.flags()="+Flags.toString(owner.flags()));
                DEBUG.P("owner.type.isParameterized()="+owner.type.isParameterized());
                if (((flags & STATIC) == 0) && owner.type.isParameterized()) {
                    Type base = asOuterSuper(t, owner);
                    if (base != null) {
                        List<Type> ownerParams = owner.type.allparams();
                        List<Type> baseParams = base.allparams();
                        DEBUG.P("ownerParams="+ownerParams);
                        DEBUG.P("baseParams ="+baseParams);
                        if (ownerParams.nonEmpty()) {
                            if (baseParams.isEmpty()) {
                                // then base is a raw type
                                return erasure(sym.type);
                            } else {
                                return subst(sym.type, ownerParams, baseParams);
                            }
                        }
                    }
                }
                return sym.type;
                
                }finally{//�Ҽ��ϵ�
				DEBUG.P(0,this,"visitClassType(2)");
				}
            }

            @Override
            public Type visitTypeVar(TypeVar t, Symbol sym) {
                return memberType(t.bound, sym);
            }

            @Override
            public Type visitErrorType(ErrorType t, Symbol sym) {
                return t;
            }
        };
    // </editor-fold>
//