    /** FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    List<JCVariableDecl> formalParameters() { //ָ��һ�������������������Ĳ���
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"formalParameters()");
    	
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        JCVariableDecl lastParam = null;
        accept(LPAREN);
        DEBUG.P("S.token()="+S.token());
        if (S.token() != RPAREN) {
            params.append(lastParam = formalParameter());
            //Vararrgs�������ڵĻ������Ƿ����������������Ĳ��������һ��
            while ((lastParam.mods.flags & Flags.VARARGS) == 0 && S.token() == COMMA) {
                S.nextToken();
                params.append(lastParam = formalParameter());
            }
        }
        accept(RPAREN);
        return params.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"formalParameters()");
		}
    }

    JCModifiers optFinal(long flags) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//���������еĲ���ֻ����final��deprecated(��JAVADOC)��ָ��
		//ParserTest(/** @deprecated */ final int i){}

		//ע����������ı������ǲ�һ����
		//ParserTest(final /** @deprecated */ int i){} //�д�
		//ParserTest(/** @deprecated */ final int i){} //�޴�
		//��Ϊ��modifiersOpt()���ȿ��Ƿ���DEPRECATED�ٽ���whileѭ����
		//��final���ȣ�����whileѭ��nextToken�����˷����Ƿ���DEPRECATED��
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }

    /** FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    JCVariableDecl formalParameter() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"formalParameter()");
    	
        JCModifiers mods = optFinal(Flags.PARAMETER);
        JCExpression type = type();
        if (S.token() == ELLIPSIS) { //���һ���β���varargs�����
            checkVarargs();
            mods.flags |= Flags.VARARGS;
            type = to(F.at(S.pos()).TypeArray(type));
            S.nextToken();
        }
        return variableDeclaratorId(mods, type);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"formalParameter()");
		}        
    }