    /**
     * Main method: compile a list of files, return all compiled classes
     *
     * @param sourceFileObjects file objects to be compiled
     * @param classnames class names to process for annotations
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void compile(List<JavaFileObject> sourceFileObjects,
                        List<String> classnames,
			Iterable<? extends Processor> processors)
        throws IOException // TODO: temp, from JavacProcessingEnvironment
    {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(3);DEBUG.P(this,"compile(3) һϵ�б�����������......");
    	DEBUG.P("sourceFileObjects="+sourceFileObjects);
    	DEBUG.P("classnames="+classnames);
    	DEBUG.P("processors="+processors);
    	
    	//ͨ��com.sun.tools.javac.api.JavacTaskImpl���call()����
    	//����com.sun.tools.javac.main.Main��compile(4)�������
    	//���õ�����ʱ��processors��Ϊnull��
    	//���ͨ��com.sun.tools.javac.main.Main���compile(2)����
    	//����com.sun.tools.javac.main.Main��compile(4)�������
    	//���õ�����ʱ��processorsΪnull��
    	
        if (processors != null && processors.iterator().hasNext())
            explicitAnnotationProcessingRequested = true;
        // as a JavaCompiler can only be used once, throw an exception if
        // it has been used before.
        if (hasBeenUsed)
	    throw new AssertionError("attempt to reuse JavaCompiler");
        hasBeenUsed = true;

        start_msec = now();//��¼��ʼ����ʱ��
        try {
            initProcessAnnotations(processors);

            // These method calls must be chained to avoid memory leaks
            delegateCompiler = processAnnotations(enterTrees(stopIfError(parseFiles(sourceFileObjects))),
                                                  classnames);
            /*���������������ɵı���������:
            1.�ʷ�����(Scanner)
            2.�﷨����(Parser)
            3.Enter��MemberEnter
            4.ע�ʹ���(JavacProcessingEnvironment)
            */

            delegateCompiler.compile2();
            /*������compile2()������ɵı���������:
            1.���Է���(Attr)
            2.����������(Flow)
            3.Desugar
            4.�����ֽ���(Gen,ClassWriter)
            */
            
            /*
            ������������ֻ�ǶԱ��������һ�����Ի���,����ϸ�ڻ���
            ������ÿһ�׶�ʱ�������ˣ�������ڴ��������޴����ڵģ�
            ÿһ�׶ζ����ض��Ĵ���Ҫ���ҡ�
            
            ���ĵ��ڲ����ݽṹ�����漸�����ж���:
            com.sun.tools.javac.util.Name
            com.sun.tools.javac.tree.JCTree
            com.sun.tools.javac.code.Symbol
            com.sun.tools.javac.code.Type
            com.sun.tools.javac.code.Scope
            com.sun.tools.javac.jvm.Items
            com.sun.tools.javac.jvm.Code
            */
	    delegateCompiler.close();
	    elapsed_msec = delegateCompiler.elapsed_msec;
        } catch (Abort ex) { //��ȫ�޶�����:com.sun.tools.javac.util.Abort
            if (devVerbose)
                ex.printStackTrace();
        } 
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"compile(3)");
    	}
    }