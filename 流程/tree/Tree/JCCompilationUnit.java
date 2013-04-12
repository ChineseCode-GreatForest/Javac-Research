    /**
     * Everything in one source file is kept in a TopLevel structure.
     * @param pid              The tree representing the package clause.
     * @param sourcefile       The source file name.
     * @param defs             All definitions in this file (ClassDef, Import, and Skip)
     * @param packge           The package it belongs to.
     * @param namedImportScope A scope for all named imports.
     * @param starImportScope  A scope for all import-on-demands.
     * @param lineMap          Line starting positions, defined only
     *                         if option -g is set.
     * @param docComments      A hashtable that stores all documentation comments
     *                         indexed by the tree nodes they refer to.
     *                         defined only if option -s is set.
     * @param endPositions     A hashtable that stores ending positions of source
     *                         ranges indexed by the tree nodes they belong to.
     *                         Defined only if option -Xjcov is set.
     */
    public static class JCCompilationUnit extends JCTree implements CompilationUnitTree {
        public List<JCAnnotation> packageAnnotations;//��ע��
        public JCExpression pid;//Դ�ļ����ڰ���ȫ��
        public List<JCTree> defs;
        public JavaFileObject sourcefile; //��JavaCompiler.parse(2)����
        
        //packge.members_field��һ��Scope,���Scope���ÿһ��Entry
        //�����˰���Ŀ¼�µ����г���Ա���뱾�����������
        //ÿ��Entry����Enter�׶μ����
        public PackageSymbol packge;
        
        //��Env.topLevelEnv(JCCompilationUnit tree)�н��г�ʼ��
        public Scope namedImportScope;
        public Scope starImportScope;//��java.lang���е�������,�ӿ�
        
        public long flags;
       
        //��JavaCompiler.parse(2)����
        public Position.LineMap lineMap = null;//com.sun.tools.javac.util.Position
        
        //��Parser.compilationUnit()����
        public Map<JCTree, String> docComments = null;
        
        //��EndPosParser.compilationUnit()����(�ӡ�-Xjcov��ѡ��)
        public Map<JCTree, Integer> endPositions = null;
        
        protected JCCompilationUnit(List<JCAnnotation> packageAnnotations,
                        JCExpression pid,
                        List<JCTree> defs,
                        JavaFileObject sourcefile,
                        PackageSymbol packge,
                        Scope namedImportScope,
                        Scope starImportScope) {
            super(TOPLEVEL);
            this.packageAnnotations = packageAnnotations;
            this.pid = pid;
            this.defs = defs;
            this.sourcefile = sourcefile;
            this.packge = packge;
            this.namedImportScope = namedImportScope;
            this.starImportScope = starImportScope;
        }
        @Override
        public void accept(Visitor v) { v.visitTopLevel(this); }//��ָJCTree.Visitor 
        
        //��ָcom.sun.source.tree.Tree.Kind
        //COMPILATION_UNIT(CompilationUnitTree.class)
        //JCCompilationUnitҲʵ����CompilationUnitTree�ӿ�
        public Kind getKind() { return Kind.COMPILATION_UNIT; }
        public List<JCAnnotation> getPackageAnnotations() {
            return packageAnnotations;
        }
        public List<JCImport> getImports() {
            ListBuffer<JCImport> imports = new ListBuffer<JCImport>();
            for (JCTree tree : defs) {
                if (tree.tag == IMPORT)
                    imports.append((JCImport)tree);
                else
                    break;//Ϊʲô�˳���?��Ϊimport�����������һ����ֵ�
            }
            return imports.toList();
        }
        public JCExpression getPackageName() { return pid; }
        public JavaFileObject getSourceFile() {
            return sourcefile;
        }
		public Position.LineMap getLineMap() {
	    	return lineMap;
        }  
        public List<JCTree> getTypeDecls() {//����һ��û��IMPORT��JCTree
        	//List�е�head��<JCTree>,tail�Ǹ���head����List<JCTree>
            List<JCTree> typeDefs;
            for (typeDefs = defs; !typeDefs.isEmpty(); typeDefs = typeDefs.tail)
                if (typeDefs.head.tag != IMPORT)
                    break;
            return typeDefs;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitCompilationUnit(this, d);
        }
    }