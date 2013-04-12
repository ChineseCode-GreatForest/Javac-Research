    /** Visitor method for parameterized types.
     *  Bound checking is left until later, since types are attributed
     *  before supertype structure is completely known
     */
    public void visitTypeApply(JCTypeApply tree) {
		DEBUG.P(this,"visitTypeApply(1)");
		DEBUG.P("tree="+tree);
		
        Type owntype = syms.errType;

        // Attribute functor part of application and make sure it's a class.
        Type clazztype = chk.checkClassType(tree.clazz.pos(), attribType(tree.clazz, env));
        
        

        // Attribute type parameters
        List<Type> actuals = attribTypes(tree.arguments, env);
        
        DEBUG.P("");
        DEBUG.P("actuals="+actuals);
        DEBUG.P("clazztype="+clazztype);
        DEBUG.P("clazztype.tag="+TypeTags.toString(clazztype.tag));
        if (clazztype.tag == CLASS) {
            List<Type> formals = clazztype.tsym.type.getTypeArguments();
            DEBUG.P("formals="+formals);
            
            DEBUG.P("actuals.length()="+actuals.length());
            DEBUG.P("formals.length()="+formals.length());
            if (actuals.length() == formals.length()) {
                List<Type> a = actuals;
                List<Type> f = formals;
                while (a.nonEmpty()) {
                    a.head = a.head.withTypeVar(f.head);//ֻ��WildcardType����
                    a = a.tail;
                    f = f.tail;
                }
                // Compute the proper generic outer
                Type clazzOuter = clazztype.getEnclosingType();
                DEBUG.P("");
                DEBUG.P("clazzOuter="+clazzOuter);
        		DEBUG.P("clazzOuter.tag="+TypeTags.toString(clazzOuter.tag));
                if (clazzOuter.tag == CLASS) {
                	DEBUG.P("tree.clazz="+tree.clazz);
        			DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());
        			DEBUG.P("env="+env);
                    Type site;
                    if (tree.clazz.tag == JCTree.IDENT) {
                        site = env.enclClass.sym.type;
                    } else if (tree.clazz.tag == JCTree.SELECT) {
                        site = ((JCFieldAccess) tree.clazz).selected.type;
                    } else throw new AssertionError(""+tree);
                    
                    DEBUG.P("site="+site);
        			DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        			DEBUG.P("(clazzOuter.tag == CLASS && site != clazzOuter)="+(clazzOuter.tag == CLASS && site != clazzOuter));
                    if (clazzOuter.tag == CLASS && site != clazzOuter) {
                        if (site.tag == CLASS)
                            site = types.asOuterSuper(site, clazzOuter.tsym);
                        if (site == null)
                            site = types.erasure(clazzOuter);
                        clazzOuter = site;
                    }
                }
                owntype = new ClassType(clazzOuter, actuals, clazztype.tsym);
            } else {
                if (formals.length() != 0) {
                	/*����:
                	class ExtendsTest<T,S,B>  {}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					������ʾ(����):
					bin\mysrc\my\test\Test.java:8: ���ͱ�����Ŀ������Ҫ 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        ������ʾ(Ӣ��):
			        bin\mysrc\my\test\Test.java:8: wrong number of type arguments; required 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        ע:���Ĵ�����ʾ���벻׼ȷ,��type arguments�����ܷ���ɡ����ͱ�������
			        �����ͱ���������ָ�����ඨ���еġ����ͱ���������Test<T>����T������
			        һ�������ͱ�����������type arguments����ָ��������ķ�����Ĳ�����
			        ��Test<String>��String����һ����type argument��������׼ȷһ���
			        ����Ӧ���ǡ����Ͳ�����Ŀ���󡱡�
			        */                                     
					
                    log.error(tree.pos(), "wrong.number.type.args",
                              Integer.toString(formals.length()));
                } else {
                	/*����:
                	class ExtendsTest{}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					������ʾ(����):
					bin\mysrc\my\test\Test.java:8: ���� my.test.ExtendsTest �����в���
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        ������ʾ(Ӣ��):
			        bin\mysrc\my\test\Test.java:8: type my.test.ExtendsTest does not take parameters
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        */                                  
                    log.error(tree.pos(), "type.doesnt.take.params", clazztype.tsym);
                }
                owntype = syms.errType;
            }
        }
        result = check(tree, owntype, TYP, pkind, pt);
        
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("tree.type.tsym.type="+tree.type.tsym.type);
		DEBUG.P(0,this,"visitTypeApply(JCTypeApply tree)");
    }