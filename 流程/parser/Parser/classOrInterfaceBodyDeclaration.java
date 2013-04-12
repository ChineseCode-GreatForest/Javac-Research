    /** ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      **********************������6���ǲ��е�**********************
     *      ( Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | TypeParameters (Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | TypeParameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      )
     *      **********************������6���ǲ��е�**********************
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    List<JCTree> classOrInterfaceBodyDeclaration(Name className, boolean isInterface) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"classOrInterfaceBodyDeclaration(2)");
 		DEBUG.P("S.token()="+S.token());

        if (S.token() == SEMI) {//���ﲻ��������JCSkip��ֻ������������(���)���ŵ�";"����JCSkip
            S.nextToken();
            return List.<JCTree>of(F.at(Position.NOPOS).Block(0, List.<JCStatement>nil()));
        } else {
            String dc = S.docComment();
            int pos = S.pos();
            JCModifiers mods = modifiersOpt();
            
            //�ڲ�CLASS,INTERFACE,ENUM
            if (S.token() == CLASS ||
                S.token() == INTERFACE ||
				//�����-source 1.4 -target 1.4�����ڲ�enum���ͣ��������λ�û����
                allowEnums && S.token() == ENUM) {
                return List.<JCTree>of(classOrInterfaceOrEnumDeclaration(mods, dc));
				//����(����static����(STATIC�ؼ�����modifiersOpt()���ѷ�����))
            } else if (S.token() == LBRACE && !isInterface &&
                       (mods.flags & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                       mods.annotations.isEmpty()) {
                       //����ǰ������ע��,ֻ����static
                return List.<JCTree>of(block(pos, mods.flags));
            } else {
                pos = S.pos();
                //ֻ��Method��Constructor֮ǰ����TypeParameter
                List<JCTypeParameter> typarams = typeParametersOpt();
                DEBUG.P("mods.pos="+mods.pos);
                
                // Hack alert:  if there are type arguments(ע����typeParameters) but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.length() > 0 && mods.pos == Position.NOPOS) {
                    mods.pos = pos;
                }
                Token token = S.token();
                Name name = S.name();//���췽��(Constructor)������ �� �ֶ������� �� �����ķ���ֵ��������
                pos = S.pos();
                JCExpression type;//�ֶε����� �� �����ķ���ֵ������
                
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("name="+name);
                
                boolean isVoid = S.token() == VOID;
                if (isVoid) {
                	//typetagΪvoid��JCPrimitiveTypeTree
                    type = to(F.at(pos).TypeIdent(TypeTags.VOID));
                    S.nextToken(); 
                } else {
                    type = type();
                }
                //���Constructor,��������Constructor�����ƣ���term3()������JCTree.JCIdent
                if (S.token() == LPAREN && !isInterface && type.tag == JCTree.IDENT) {
                	
                	//isInterface���������ȫ����ȥ������Ϊͨ��ǰһ��if����
                	//isInterface��ֵ�϶�Ϊfalse
                    if (isInterface || name != className)
                    	//���췽��(Constructor)�����ƺ�������һ��ʱ
                    	//�ᱨ��ֻ�Ǳ�����Ϣ��:������������Ч����Ҫ�������͡�
                        log.error(pos, "invalid.meth.decl.ret.type.req");
                    return List.of(methodDeclaratorRest(
                        pos, mods, null, names.init, typarams,
                        isInterface, true, dc));
                } else {
                    pos = S.pos();
                    name = ident(); //�ֶ����򷽷���������ȡ��һ��token

                    if (S.token() == LPAREN) { //����
                        return List.of(methodDeclaratorRest(
                            pos, mods, type, name, typarams,
                            isInterface, isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty()) { //�ֶ���
						//�ڽӿ��ж�����ֶ���Ҫ��ʾ�ĳ�ʼ��(isInterface=true)
                        List<JCTree> defs =
                            variableDeclaratorsRest(pos, mods, type, name, isInterface, dc,
                                                    new ListBuffer<JCTree>()).toList();
                        storeEnd(defs.last(), S.endPos());
                        accept(SEMI);
                        return defs;
                    } else {
                        pos = S.pos();
                        List<JCTree> err = isVoid
                            ? List.<JCTree>of(toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null)))
                            : null;
                            
                        /*
                        ��:
                        bin\mysrc\my\test\Test.java:32: ��Ҫ '('
						        public <M extends T,S> int myInt='\uuuuu5df2';
						                                        ^
						1 ����
						*/
                        return List.<JCTree>of(syntaxError(S.pos(), err, "expected", keywords.token2string(LPAREN)));
                    }
                }
            }
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(2,this,"classOrInterfaceBodyDeclaration(2)");
		}   
    }