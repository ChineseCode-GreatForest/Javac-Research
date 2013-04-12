    /** ClassDeclaration = CLASS Ident TypeParametersOpt [EXTENDS Type]
     *                     [IMPLEMENTS TypeList] ClassBody
     *  @param mods    The modifiers starting the class declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCClassDecl classDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"classDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());

        int pos = S.pos(); //��Ӧclass���token����ʼλ��(pos)
        accept(CLASS);
		//��Ϊ������һ����ʶ����
		//�������ڵ���Scanner���nextToken����ʱ�ֵ�����scanIdent()��
		//ͨ��scanIdent()�������ӽ���Name.Table.names����ֽ��������ˡ�
        Name name = ident();

        List<JCTypeParameter> typarams = typeParametersOpt();//����<>
        DEBUG.P("typarams="+typarams);
        DEBUG.P("typarams.size="+typarams.size());
        

        JCTree extending = null;
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = type();
        }
        DEBUG.P("extending="+extending);
        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }
        DEBUG.P("implementing="+implementing);
        List<JCTree> defs = classOrInterfaceBody(name, false);
		DEBUG.P("defs.size="+defs.size());
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, typarams, extending, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"classDeclaration(2)");
        return result;
    }