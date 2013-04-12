    /** Parse contents of input stream.
     *  @param filename     The name of the file from which input stream comes.
     *  @param input        The input stream to be parsed.
     */
    protected JCCompilationUnit parse(JavaFileObject filename, CharSequence content) {
        DEBUG.P(this,"parse(2)");
        
        long msec = now();
        
        //����һ�ÿ�JCCompilationUnit����
        //JCCompilationUnit�����ĳ����﷨��(abstract syntax tree)
        //�ο�com.sun.tools.javac.tree.JCTree����com.sun.tools.javac.tree.TreeMaker��
        JCCompilationUnit tree = make.TopLevel(List.<JCTree.JCAnnotation>nil(),
                                      null, List.<JCTree>nil());
        if (content != null) {
            if (verbose) {
                printVerbose("parsing.started", filename);
            }
            
            //taskListener����Ϊ��,��Ϊ����汾��Javac��û����
            //����ʵ��com.sun.source.util.TaskListener�ӿ�
        	DEBUG.P("taskListener="+taskListener);
            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, filename);
                taskListener.started(e);
            }
            
	    	int initialErrorCount = log.nerrors;
	    	
	    	//����һ���ʷ�������Scanner��ʵ��,��ָ���һ���ַ�
            Scanner scanner = getScannerFactory().newScanner(content);
            
            //����һ���﷨������Parser��ʵ��,��ָ���һ��token
            Parser parser = parserFactory.newParser(scanner, keepComments(), genEndPos);
            
            //java���Ե��﷨����LL(1)�ķ�,���Բ��õ��ǵݹ��½������㷨,
            //���ڶ�Ԫ������ʽ������������ȼ��㷨
            //Parserͨ��nextToken()������Scanner
            tree = parser.compilationUnit();
            
	    	parseErrors |= (log.nerrors > initialErrorCount);
            if (lineDebugInfo) {
                tree.lineMap = scanner.getLineMap();
            }
            if (verbose) {
                printVerbose("parsing.done", Long.toString(elapsed(msec)));
            }
        }

        tree.sourcefile = filename;

        if (content != null && taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, tree);
            taskListener.finished(e);
        }
        
        DEBUG.P(0,this,"parse(2)");
        return tree;
    }