    /**
     * A new(...) operation.
     */
    //��EnumeratorDeclarationҲ����JCNewClass��ʾ�ĵط����ο�Parser��enumeratorDeclaration(Name enumName)
    public static class JCNewClass extends JCExpression implements NewClassTree {
        public JCExpression encl;//��ӦNewClassTree�е�enclosingExpression
        public List<JCExpression> typeargs;//��ӦNewClassTree�е�typeArguments
        public JCExpression clazz;//��ӦNewClassTree�е�identifier
        public List<JCExpression> args;//��ӦNewClassTree�е�arguments
        public JCClassDecl def;//��ӦNewClassTree�е�classBody
        public Symbol constructor;
        public Type varargsElement;
        protected JCNewClass(JCExpression encl,
			   List<JCExpression> typeargs,
			   JCExpression clazz,
			   List<JCExpression> args,
			   JCClassDecl def)
	{
            super(NEWCLASS);
            this.encl = encl;
	    this.typeargs = (typeargs == null) ? List.<JCExpression>nil()
		                               : typeargs;
            this.clazz = clazz;
            this.args = args;
            this.def = def;
        }
        @Override
        public void accept(Visitor v) { v.visitNewClass(this); }

        public Kind getKind() { return Kind.NEW_CLASS; }
        public JCExpression getEnclosingExpression() { // expr.new C< ... > ( ... )
            return encl;
        }
        public List<JCExpression> getTypeArguments() {
            return typeargs;
        }
        public JCExpression getIdentifier() { return clazz; }
        public List<JCExpression> getArguments() {
            return args;
        }
        public JCClassDecl getClassBody() { return def; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitNewClass(this, d);
        }
    }