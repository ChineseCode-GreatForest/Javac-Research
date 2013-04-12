    /** Class enter visitor method for type parameters.
     *	Enter a symbol for type parameter in local scope, after checking that it
     *	is unique.
     */
    /*
    TypeParameter�������ClassSymbol.members_field�У�
    ֻ������JCClassDecl��Ӧ��Env<AttrContext>.info.Scope�С�

    ���⣬�ڷ������ඨ���TypeParameter��������ͬ�����ͱ�������
    ���߻���Ӱ�졣������ʾ:
    class Test<T,S> {
            public <T> void method(T t){}
    }
    */
    public void visitTypeParameter(JCTypeParameter tree) {
        DEBUG.P(this,"visitTypeParameter(JCTypeParameter tree)");
        DEBUG.P("tree.name="+tree.name);
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
        if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);
    
    
		TypeVar a = (tree.type != null)
			? (TypeVar)tree.type
			: new TypeVar(tree.name, env.info.scope.owner);
		tree.type = a;
		/*TypeParameter���������������������TypeParameter��
		��������Parser�׶μ�������ģ����������checkUnique()�����С�
		
		��������:
		bin\mysrc\my\test\Test.java:64: ���� my.test.Test2 �ж��� T
		class Test2<T,T>{}
					  ^
		1 ����
		*/
		if (chk.checkUnique(tree.pos(), a.tsym, env.info.scope)) {
			env.info.scope.enter(a.tsym);
		}
		result = a;
		
		
		if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);
        DEBUG.P(0,this,"visitTypeParameter(JCTypeParameter tree)");
    }