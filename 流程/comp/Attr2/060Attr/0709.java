        /** Does this symbol override `other' symbol, when both are seen as
         *  members of class `origin'?  It is assumed that _other is a member
         *  of origin.
         *
         *  It is assumed that both symbols have the same name.  The static
         *  modifier is ignored for this test.
         *
         *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
         */
        //��鵱ǰSymbol�Ƿ񸲸���Symbol _other
		//��ǰSymbol�п�����ԭʼʵ����(origin)���߳����еķ���
        public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        	try {//�Ҽ��ϵ�
			DEBUG.P(this,"overrides(4)");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("_other.kind ="+Kinds.toString(_other.kind));
			DEBUG.P("isConstructor()="+isConstructor());
            if (isConstructor() || _other.kind != MTH) return false;
            
            DEBUG.P("");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
            DEBUG.P("(this == _other)="+(this == _other));
            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation

			/*���жϵ�ǰ�����ܷ񸲸�other����ǰ���ȵ���isOverridableIn
			�б�other���������η�(PRIVATE,PUBLIC,PROTECTED��û��)
			�Ƿ����ڵ�ǰ������owner�и���other������˵�����other
			���������η���PRIVATE����ô��owner�в��ܸ�������

			���isOverridableIn����true�ˣ�������ȷ��other������owner
			�ǵ�ǰ��ǰ������owner�ĳ���
			*/
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null) {
                Type mt = types.memberType(owner.type, this);
                Type ot = types.memberType(owner.type, other);
                if (types.isSubSignature(mt, ot)) {
                    if (!checkResult) //��鷽����������
                        return true;
                    if (types.returnTypeSubstitutable(mt, ot))
                        return true;
                }
            }
			DEBUG.P("");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("");
			DEBUG.P("this.flags() ="+Flags.toString(this.flags()));
			DEBUG.P("other.flags()="+Flags.toString(other.flags()));

            // check for an inherited implementation
            if ((flags() & ABSTRACT) != 0 ||
                (other.flags() & ABSTRACT) == 0 ||
                !other.isOverridableIn(origin) ||
                !this.isMemberOf(origin, types))
                return false;

            // assert types.asSuper(origin.type, other.owner) != null;
            Type mt = types.memberType(origin.type, this);
            Type ot = types.memberType(origin.type, other);
            return
                types.isSubSignature(mt, ot) &&
                (!checkResult || types.resultSubtype(mt, ot, Warner.noWarnings));
            }finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"overrides(4)");
			}
        }