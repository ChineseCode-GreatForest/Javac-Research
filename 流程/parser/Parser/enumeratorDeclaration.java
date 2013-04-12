    //�ο�jdk1.6.0docs/technotes/guides/language/enums.html
    /** EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    JCTree enumeratorDeclaration(Name enumName) {
    	DEBUG.P(this,"enumeratorDeclaration(Name enumName)");
        String dc = S.docComment();
        int flags = Flags.PUBLIC|Flags.STATIC|Flags.FINAL|Flags.ENUM;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        int pos = S.pos();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? Position.NOPOS : pos).Modifiers(flags, annotations);
        
        /*��Java Language Specification, Third Edition
		 18.1. The Grammar of the Java Programming Language
		 �������¶���:
		 EnumConstant:
      	 Annotations Identifier [Arguments] [ClassBody]
      	 ����������﷨AnnotationsOpt [TypeArguments] IDENTIFIER�Ǵ����
      	 
      	 ���ơ�<?>SUPER("? super ")��������ö�ٳ����Ǵ����(�Ƿ��ı��ʽ��ʼ)
      	 */
        List<JCExpression> typeArgs = typeArgumentsOpt();//���Ƿ���null
        int identPos = S.pos();
        Name name = ident();
        int createPos = S.pos();
        List<JCExpression> args = (S.token() == LPAREN)
            ? arguments() : List.<JCExpression>nil();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
        	/*���´���Ƭ��:
        		public static enum MyBoundKind {
			    @Deprecated EXTENDS("? extends ") {
			    	 String toString() {
			    	 	return "extends"; 
			    	 }
			    },
			*/
            JCModifiers mods1 = F.at(Position.NOPOS).Modifiers(Flags.ENUM | Flags.STATIC);
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            body = toP(F.at(identPos).AnonymousClassDef(mods1, defs));
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        JCIdent ident = F.at(Position.NOPOS).Ident(enumName);
        //ÿ��ö�ٳ������൱���Ǵ�ö�����͵�һ��ʵ��
        JCNewClass create = F.at(createPos).NewClass(null, typeArgs, ident, args, body);
        if (createPos != Position.NOPOS)
            storeEnd(create, S.prevEndPos());
        ident = F.at(Position.NOPOS).Ident(enumName);//ע�����������治��ͬһ��JCIdent��ʵ��
        JCTree result = toP(F.at(pos).VarDef(mods, name, ident, create));
        attach(result, dc);
        
        DEBUG.P(0,this,"enumeratorDeclaration(Name enumName)");
        return result;
    }