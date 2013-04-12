    /**
     * A formal class parameter.
     * @param name name
     * @param bounds bounds
     */
    //��JCTree�̳е��ֶΡ�type����ֵ��com.sun.tools.javac.comp.Enter���
    //visitTypeParameter(JCTypeParameter tree)���������ã�type�ֶ�ָ��TypeVar��ʵ��
    public static class JCTypeParameter extends JCTree implements TypeParameterTree {
        //��:������Test<S extends TestBound & MyInterfaceA>
        //name��Ӧ��S����
        //bounds[0]��Ӧ��TestBound����
        //bounds[1]��Ӧ��MyInterfaceA��
        //���û��extends����ؼ��֣�bounds.size=0
        public Name name;
        public List<JCExpression> bounds;
        protected JCTypeParameter(Name name, List<JCExpression> bounds) {
            super(TYPEPARAMETER);
            this.name = name;
            this.bounds = bounds;
        }
        @Override
        public void accept(Visitor v) { v.visitTypeParameter(this); }

        public Kind getKind() { return Kind.TYPE_PARAMETER; }
        public Name getName() { return name; }
        public List<JCExpression> getBounds() {
            return bounds;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitTypeParameter(this, d);
        }
    }