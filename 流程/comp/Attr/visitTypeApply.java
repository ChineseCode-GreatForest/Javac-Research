    /** Visitor method for parameterized types.
     *  Bound checking is left until later, since types are attributed
     *  before supertype structure is completely known
     */
	//��:List<String>
    public void visitTypeApply(JCTypeApply tree) {
		DEBUG.P(this,"visitTypeApply(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.clazz="+tree.clazz);
		DEBUG.P("tree.arguments="+tree.arguments);
		
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
					/*��:
					class Aclass<T> {
						Aclass<?> a;
					}
					com.sun.tools.javac.code.Type$WildcardType===>withTypeVar(Type t)
					-------------------------------------------------------------------------
					bound=null
					t    =T {bound=Object}

					�������βΣ�T {bound=Object}
					������ʵ�Σ�?
					com.sun.tools.javac.code.Type$WildcardType===>withTypeVar(Type t)  END
					-------------------------------------------------------------------------
					*/
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
					/*��:
						import test.attr.Aclass.*;
						class Aclass<T> {
							class Bclass<V>{
								//site=test.attr.Aclass<T {bound=Object}>.Bclass<V {bound=Object}>
								//site.tag=CLASS
								(clazzOuter.tag == CLASS && site != clazzOuter)=true
								//site=test.attr.Aclass<T {bound=Object}>
								//clazzOuter=test.attr.Aclass
								//clazzOuter=test.attr.Aclass<T {bound=Object}>
								Bclass<Aclass3> b1;
							}
						}

						class Aclass2<T> {
							//���͵ĸ�ʽ����ȷ����������ͨ���͵����Ͳ���
							//��Ϊimport�е����Aclass����������
							//�൱�ڡ�Aclass.Bclass<Aclass3> b2;�������ĸ�ʽ�Ǵ����
							//site = types.asOuterSuper(site, clazzOuter.tsym)=null
							//���clazzOuter=test.attr.Aclass
							Bclass<Aclass3> b2;

							//��һ��clazzOuter=test.attr.Aclass��
							//����site=test.attr.Aclass<test.attr.Aclass3>
							//����(clazzOuter.tag == CLASS && site != clazzOuter)=true
							//����site = types.asOuterSuper(site, clazzOuter.tsym);
							//����site=test.attr.Aclass<test.attr.Aclass3>
							//���clazzOuter=test.attr.Aclass<test.attr.Aclass3>
							Aclass<Aclass3>.Bclass<Aclass3> b3;
						}
						class Aclass3{}
					*/
                    if (clazzOuter.tag == CLASS && site != clazzOuter) {
                        if (site.tag == CLASS)
                            site = types.asOuterSuper(site, clazzOuter.tsym);

						DEBUG.P("site="+site);
						DEBUG.P("clazzOuter="+clazzOuter);
                        if (site == null)
                            site = types.erasure(clazzOuter);
                        clazzOuter = site;
                    }
                }
				DEBUG.P("clazzOuter="+clazzOuter);
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
		DEBUG.P(0,this,"visitTypeApply(1)");
    }