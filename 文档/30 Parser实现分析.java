AST��һ��Ƕ��ʽ�������������JCCompilationUnit,

JCCompilationUnit��JCTree�����࣬
JCTree�ǳ���ģ�JCTreeֻ��pos��type����ʵ���ֶΣ�
pos��ֵ������Դ�����е�λ�ã�����TreeMaker����ÿһ��JCTree������ʱ��pos��ֵ��
Parser�׶β����type�ֶθ�ֵ��
��ͬJCTree�����type�ֶ�ֵ���ڲ�ͬ�׶���ɡ�

JCCompilationUnit��������Щ�ֶ�;
		public List<JCAnnotation> packageAnnotations; //��ע�⣬ֻ������package-info.jar��
        public JCExpression pid; //����

		//import���,skip(Ҳ����";"��),�����࣬ͬһ��Դ�ļ��п���������������࣬
		//����ֻ����Դ�ļ�����ͬ���������public�ģ������ı����ǰ�˽�е�(package-private)
        public List<JCTree> defs;
        public JavaFileObject sourcefile;��//Դ�ļ���
        public PackageSymbol packge;
        public ImportScope namedImportScope;
        public StarImportScope starImportScope;
        public long flags; //����ֶ�û��ʹ��
        public Position.LineMap lineMap = null; //��"-g:lines"ѡ����߲�����"-g:"ѡ��(��������������)����
        public Map<JCTree, String> docComments = null; //-printsource �� -stubs����
        public Map<JCTree, Integer> endPositions = null; //-Xjcov��ע����javax.tools.DiagnosticListenerʱ����

Parser�׶�Ҫ�����¾��ǰ�JCCompilationUnit�е�������Щ�ֶ����:
packageAnnotations
pid
defs
lineMap
docComments   //ǰ����5����com.sun.tools.javac.parser.JavacParser.parseCompilationUnit()����
endPositions  //����-Xjcovѡ���ע����javax.tools.DiagnosticListenerʱ������JavacParser������EndPosParserҪ������
              //��ʱ��com.sun.tools.javac.parser.EndPosParser.parseCompilationUnit()����endPositions

sourcefile    //��com.sun.tools.javac.main.JavaCompiler.parse(JavaFileObject filename, CharSequence content)����


