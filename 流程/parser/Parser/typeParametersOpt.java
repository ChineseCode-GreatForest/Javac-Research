    /** TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    List<JCTypeParameter> typeParametersOpt() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"typeParametersOpt()");
    	
        if (S.token() == LT) {
            checkGenerics();
            ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
            S.nextToken();
            typarams.append(typeParameter());
            while (S.token() == COMMA) {
                S.nextToken();
                typarams.append(typeParameter());
            }
            accept(GT);
            return typarams.toList();
        } else {
            return List.nil();
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeParametersOpt()");
		}
    }
    
    /*ע��TypeParameter��TypeArgument�Ĳ��
     *	TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type
     *               | "?" SUPER Type
    
    �Աȷ����������β���ʵ�������TypeParameter��TypeArgument
    */
    
    /** TypeParameter = TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident
     */
    JCTypeParameter typeParameter() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"typeParameter()");
    	
        int pos = S.pos();
        Name name = ident();
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        if (S.token() == EXTENDS) {
            S.nextToken();
            bounds.append(type());
            while (S.token() == AMP) {
                S.nextToken();
                bounds.append(type());
            }
        }
		//���ֻ��<T>����ôbounds.toList()��һ��new List<JCExpression>(null,null)
        return toP(F.at(pos).TypeParameter(name, bounds.toList()));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeParameter()");
		}
    }