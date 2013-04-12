//supertype
    // <editor-fold defaultstate="collapsed" desc="supertype">
    public Type supertype(Type t) {
        //return supertype.visit(t);

		DEBUG.P(this,"supertype(Type t)");
		//DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		
		Type returnType = supertype.visit(t);
            
		//DEBUG.P("returnType="+returnType);
		//if(returnType!=null) DEBUG.P("returnType.tag="+TypeTags.toString(returnType.tag));
		
		DEBUG.P("t="+t);
		DEBUG.P("supertype="+returnType);
		DEBUG.P(1,this,"supertype(Type t)");
		return returnType;
    }
    // where
        private UnaryVisitor<Type> supertype = new UnaryVisitor<Type>() {

            public Type visitType(Type t, Void ignored) {
                // A note on wildcards: there is no good way to
                // determine a supertype for a super bounded wildcard.
                return null;
            }

            @Override
            public Type visitClassType(ClassType t, Void ignored) {
            	//DEBUG.P(this,"visitClassType(2)");
            	//DEBUG.P("t.supertype_fieldǰ="+t.supertype_field);
            	//Symtab���е��ֶ�predefClass������Ҳ��ClassType��������
            	//ClassType.supertype_field=null,����ΪType.noType
            	/*�����:
            	com.sun.tools.javac.code.Types===>supertype(Type t)
				-------------------------------------------------------------------------
				t=<���� null>6662015
				t.tag=CLASS
				com.sun.tools.javac.code.Types$18===>visitClassType(2)
				-------------------------------------------------------------------------
				t.supertype_fieldǰ=null
				com.sun.tools.javac.code.Symbol$ClassSymbol===>getSuperclass()
				-------------------------------------------------------------------------
				com.sun.tools.javac.code.Symbol$ClassSymbol===>complete()
				-------------------------------------------------------------------------
				name=   completer=null
				com.sun.tools.javac.code.Symbol$ClassSymbol===>complete()  END
				-------------------------------------------------------------------------
				com.sun.tools.javac.code.Symbol$ClassSymbol===>getSuperclass()  END
				-------------------------------------------------------------------------
				t.isInterface()=false
				t.supertype_field��=<none>
				t.supertype_field��=<none>
				com.sun.tools.javac.code.Types$18===>visitClassType(2)  END
				-------------------------------------------------------------------------
				returnType=<none>
				returnType.tag=NONE
				com.sun.tools.javac.code.Types===>supertype(Type t)  END
				-------------------------------------------------------------------------
				*/
                if (t.supertype_field == null) { //����capture����ʱҲΪnull
					DEBUG.P("t.supertype_field == null");
                    Type supertype = ((ClassSymbol)t.tsym).getSuperclass();
					DEBUG.P("supertype1="+supertype);
                    // An interface has no superclass; its supertype is Object.
                    DEBUG.P("t.isInterface()="+t.isInterface());
                    if (t.isInterface())
                        supertype = ((ClassType)t.tsym.type).supertype_field;
                    DEBUG.P("supertype2="+supertype);
                    DEBUG.P("t.supertype_field��="+t.supertype_field);
                    if (t.supertype_field == null) {
                        List<Type> actuals = classBound(t).allparams();
                        List<Type> formals = t.tsym.type.allparams();
                        
                        DEBUG.P("actuals="+actuals);
						DEBUG.P("formals="+formals);
						
                        if (actuals.isEmpty()) {
                            if (formals.isEmpty())
                                // Should not happen.  See comments below in interfaces
                                t.supertype_field = supertype;
                            else
                                t.supertype_field = erasure(supertype);
                        } else {
                            t.supertype_field = subst(supertype, formals, actuals);
                        }
                    }
                }
                
                //DEBUG.P("t.supertype_field��="+t.supertype_field);
                //DEBUG.P(0,this,"visitClassType(2)");
                return t.supertype_field;
            }

            /**
             * The supertype is always a class type. If the type
             * variable's bounds start with a class type, this is also
             * the supertype.  Otherwise, the supertype is
             * java.lang.Object.
             */
			/*
			����<T,V extends T,M extends interfaceA,N extends ClassA&interfaceA,L extends interfaceB&interfaceA,O extends ClassA>
			��ô:
			supertype(T)=Object
			supertype(V)=T
			supertype(M)=supertype(interfaceA)=Object
			supertype(N)=ClassA
			supertype(L)=supertype(interfaceB)=Object
			supertype(O)=ClassA
			*/
            @Override
            public Type visitTypeVar(TypeVar t, Void ignored) {
            	try {//�Ҽ��ϵ�
				DEBUG.P(this,"visitTypeVar(2)");
				DEBUG.P("t.bound="+t.bound);
				DEBUG.P("t.bound.tag="+TypeTags.toString(t.bound.tag));
				DEBUG.P("t.bound.isCompound() ="+t.bound.isCompound());
				DEBUG.P("t.bound.isInterface()="+t.bound.isInterface());
				
                if (t.bound.tag == TYPEVAR ||
                    (!t.bound.isCompound() && !t.bound.isInterface())) {
                    return t.bound;
                } else {
                    return supertype(t.bound);
                }
                
                }finally{//�Ҽ��ϵ�
				DEBUG.P(0,this,"visitTypeVar(2)");
				}
            }

			/*
			��������Ԫ��������ԭʼ���ͻ�Object����int[],Object[]
			��ôsupertype(ArrayType t)=java.lang.Object&java.io.Serializable&java.lang.Cloneable

			����supertype(ArrayType t)=Ԫ�����͵ĳ����[]
			��:supertype(Integer[])=Number[]
			
			*/
            @Override
            public Type visitArrayType(ArrayType t, Void ignored) {
                if (t.elemtype.isPrimitive() || isSameType(t.elemtype, syms.objectType))
                    return arraySuperType();
                else
                    return new ArrayType(supertype(t.elemtype), t.tsym);
            }

            @Override
            public Type visitErrorType(ErrorType t, Void ignored) {
                return t;
            }
        };
    // </editor-fold>
//