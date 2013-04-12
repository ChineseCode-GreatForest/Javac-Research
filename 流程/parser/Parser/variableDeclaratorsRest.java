	/*
	<T extends ListBuffer<? super JCVariableDecl>> T vdefs�������?
	��˼��:������T vdefs���ġ�type argument�������ͱ�����ListBuffer��������,
	����ListBuffer��������ġ�parameterized type������JCVariableDecl���䳬�ࡣ
	
	���Ӳο�forInit()�����е����´���Ƭ��:
	ListBuffer<JCStatement> stats......
	variableDeclarators(......, stats)

	���С�type argument��ָ����stats������ָ��ListBuffer<JCStatement>��ʵ�������ã�
	ListBuffer�ġ�parameterized type��ָ����JCStatement����JCStatement
	����JCVariableDecl�ĳ��ࡣ
	*/

    /** VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    public <T extends ListBuffer<? super JCVariableDecl>> T variableDeclarators(JCModifiers mods,
                                                                         JCExpression type,
                                                                         T vdefs)
    {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclarators(3)");
		
        return variableDeclaratorsRest(S.pos(), mods, type, ident(), false, null, vdefs);

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclarators(3)");
		}         
    }

    /** VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    <T extends ListBuffer<? super JCVariableDecl>> T variableDeclaratorsRest(int pos,
                                                                     JCModifiers mods,
                                                                     JCExpression type,
                                                                     Name name,
                                                                     boolean reqInit,
                                                                     String dc,
                                                                     T vdefs)
    {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclaratorsRest(7)");
		
        vdefs.append(variableDeclaratorRest(pos, mods, type, name, reqInit, dc));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
            storeEnd((JCTree)vdefs.elems.last(), S.endPos());
            S.nextToken();
            vdefs.append(variableDeclarator(mods, type, reqInit, dc));
        }
        return vdefs;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclaratorsRest(7)");
		}          
    }

    /** VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    JCVariableDecl variableDeclarator(JCModifiers mods, JCExpression type, boolean reqInit, String dc) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclarator(4)");
		
        return variableDeclaratorRest(S.pos(), mods, type, ident(), reqInit, dc);
       
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclarator(4)");
		}      
    }

    /** VariableDeclaratorRest = BracketsOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt "=" VariableInitializer
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    JCVariableDecl variableDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name,
                                  boolean reqInit, String dc) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclaratorRest(6)");
		DEBUG.P("pos="+pos);
		DEBUG.P("mods="+mods);
		DEBUG.P("type="+type);
		DEBUG.P("name="+name);
		//�ӿ��ж���ĳ�Ա������Ҫ��ʼ��
		//reqInit��ʱ����isInterface��ֵ
		DEBUG.P("reqInit="+reqInit);
		DEBUG.P("dc="+dc);
		
        type = bracketsOpt(type); //����:String s1[]
        JCExpression init = null;
        if (S.token() == EQ) {
            S.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(S.pos(), "expected", keywords.token2string(EQ));
        //���ڽӿ��ж���ĳ�Ա���������û��ָ�����η���
        //��Parser�׶�Ҳ�����Զ�����
        //DEBUG.P("mods="+mods);
        JCVariableDecl result =
            toP(F.at(pos).VarDef(mods, name, type, init));
        attach(result, dc);
        return result;

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclaratorRest(6)");
		}       
    }

    /** VariableDeclaratorId = Ident BracketsOpt
     */
    JCVariableDecl variableDeclaratorId(JCModifiers mods, JCExpression type) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"variableDeclaratorId(2)");
		
        int pos = S.pos();
        Name name = ident();
        if ((mods.flags & Flags.VARARGS) == 0)
		//mothodName(N[] n[],S s)�����﷨Ҳ���ᱨ��
		//mothodName(N... n[],S s)�����﷨�ͻᱨ��
		//mothodName(N[8] n[9],S s)�����﷨Ҳ�ᱨ��
		//��Ϊ���������е��������Ͳ����ǲ���ָ�������С��
            type = bracketsOpt(type);
        //�����β�û�г�ʼ�����֣�����VarDef�����ĵ�4������Ϊnull
        return toP(F.at(pos).VarDef(mods, name, type, null));

        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"variableDeclaratorId(2)");
        }  
    }
