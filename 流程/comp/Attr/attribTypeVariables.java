    /**
     * Attribute type variables (of generic classes or methods).
     * Compound types are attributed later in attribBounds.
     * @param typarams the type variables to enter
     * @param env      the current environment
     */
    //b10����
    void attribTypeVariables(List<JCTypeParameter> typarams, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypeVariables(2)");
    	DEBUG.P("typarams="+typarams);
    	DEBUG.P("env="+env);
    	
    	/*ע��:
		��class Test<S,P extends V, V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest>
		�����Ķ����ǺϷ��ģ�
		��ȻV��P֮�󣬵�P ��extends VҲ���ᱨ��
		��Ϊ���е����ͱ���(������S, P, V, T, E)����
		com.sun.tools.javac.comp.Enter===>visitTypeParameter(JCTypeParameter tree)
		�����������Ѽ�����Test��Ӧ��Env�������ʾΪ��������DEBUG.P()�Ľ��:
		typarams=S,P extends V,V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest & InterfaceTest
		env=Env(TK=CLASS EC=)[AttrContext[Scope[(nelems=5 owner=Test)E, T, V, P, S]],outer=Env(TK=COMPILATION_UNIT EC=)[AttrContext[Scope[(nelems=3 owner=test)Test, ExtendsTest, InterfaceTest]]]]
		
		��Ҫ�������ͱ���P��boundʱ����ΪJCTypeParameter.bounds=V��Ȼ��
		��env�в��ң�����V��env��Scope���ڣ������ǿ��Գ�ǰ����V�ģ�
		����Ҫ����Ϊ���ͱ����Ľ��������ͱ�����bound�Ľ����Ƿ��Ⱥ�����
		�׶ν��еģ����ǰѡ�P extends V���ĳɡ�P extends V2�����ͻ�
		�����Ҳ������š����������ΪV2����env�У������ط�Ҳ�Ҳ�����
		*/
    	
        for (JCTypeParameter tvar : typarams) {
            TypeVar a = (TypeVar)tvar.type;
            DEBUG.P("a.tsym.name="+a.tsym.name);
            DEBUG.P("a.bound="+a.bound);
            DEBUG.P("tvar="+tvar);
    		DEBUG.P("tvar.bounds="+tvar.bounds);
            if (!tvar.bounds.isEmpty()) {
                List<Type> bounds = List.of(attribType(tvar.bounds.head, env));
                for (JCExpression bound : tvar.bounds.tail)
                    bounds = bounds.prepend(attribType(bound, env));
                DEBUG.P("bounds="+bounds);
                DEBUG.P("bounds.reverse()="+bounds.reverse());
                types.setBounds(a, bounds.reverse());
            } else {
                // if no bounds are given, assume a single bound of
                // java.lang.Object.
                types.setBounds(a, List.of(syms.objectType));
            }
            DEBUG.P("a.bound="+a.bound);DEBUG.P("");
        }
        for (JCTypeParameter tvar : typarams)
            chk.checkNonCyclic(tvar.pos(), (TypeVar)tvar.type);
        attribStats(typarams, env);
        
        DEBUG.P(0,this,"attribTypeVariables(2)");
    }