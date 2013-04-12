    /**
     * A class definition.
     * @param modifiers the modifiers
     * @param name the name of the class
     * @param typarams formal class parameters
     * @param extending the classes this class extends
     * @param implementing the interfaces implemented by this class
     * @param defs all variables and methods defined in this class
     * @param sym the symbol
     */
    public static class JCClassDecl extends JCStatement implements ClassTree {
    	//����:public class Test<S extends TestBound & MyInterfaceA, T> extends TestOhter<Integer,String> implements MyInterfaceA,MyInterfaceB
    	
        public JCModifiers mods; //��Ӧ public
        public Name name; //��Ӧ Test ֻ��һ���򵥵�����(��������)

		//typaramsһ����Ϊnull(��Parser.classDeclaration(2))
        public List<JCTypeParameter> typarams; //��Ӧ <S extends TestBound & MyInterfaceA, T>
        public JCTree extending; //��Ӧ TestOhter<Integer,String>
        public List<JCExpression> implementing; //��Ӧ MyInterfaceA,MyInterfaceB
        public List<JCTree> defs;
        
        //sym.members_field��һ��Scope,���Scope���ÿһ��Entry
        //����һ����Ա��(���Ա�ӿ�)�����ǲ�����type parameter
        //ÿ��Entry����Enter�׶μ����
        public ClassSymbol sym;
        protected JCClassDecl(JCModifiers mods,
			   Name name,
			   List<JCTypeParameter> typarams,
			   JCTree extending,
			   List<JCExpression> implementing,
			   List<JCTree> defs,
			   ClassSymbol sym)
	{
            super(CLASSDEF);
            this.mods = mods;
            this.name = name;
            this.typarams = typarams;
            this.extending = extending;
            this.implementing = implementing;
            this.defs = defs;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitClassDef(this); }

        public Kind getKind() { return Kind.CLASS; }
        public JCModifiers getModifiers() { return mods; }
        public Name getSimpleName() { return name; }
        public List<JCTypeParameter> getTypeParameters() {
            return typarams;
        }
        public JCTree getExtendsClause() { return extending; }
        public List<JCExpression> getImplementsClause() {
            return implementing;
        }
        public List<JCTree> getMembers() {
            return defs;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitClass(this, d);
        }
    }