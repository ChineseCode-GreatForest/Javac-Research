    /*ͨ���<?>��<? extends type>��<? super type>

	����<?>��JCWildcard���ֶ�Ϊ:
	kind=?
	inner=null

	����<? extends String>��JCWildcard���ֶ�Ϊ:
	kind=? extends 
	inner=String

	����<? extends String>��JCWildcard���ֶ�Ϊ:
	kind=? super
	inner=String
	*/
    public static class JCWildcard extends JCExpression implements WildcardTree {
        public TypeBoundKind kind;
        public JCTree inner;
        protected JCWildcard(TypeBoundKind kind, JCTree inner) {
            super(WILDCARD);
            kind.getClass(); // null-check
            this.kind = kind;
            this.inner = inner;
        }
        @Override
        public void accept(Visitor v) { v.visitWildcard(this); }

        public Kind getKind() {
            switch (kind.kind) {
            case UNBOUND:
                return Kind.UNBOUNDED_WILDCARD;
            case EXTENDS:
                return Kind.EXTENDS_WILDCARD;
            case SUPER:
                return Kind.SUPER_WILDCARD;
            default:
                throw new AssertionError("Unknown wildcard bound " + kind);
            }
        }
        public JCTree getBound() { return inner; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitWildcard(this, d);
        }
    }