    /**
     * Set to true to enable skeleton annotation processing code.
     * Currently, we assume this variable will be replaced more
     * advanced logic to figure out if annotation processing is
     * needed.
     */
    boolean processAnnotations = false;

    /**
     * Object to handle annotation processing.
     */
    JavacProcessingEnvironment procEnvImpl = null;


	/**
     * Check if we should process annotations.
     * If so, and if no scanner is yet registered, then set up the DocCommentScanner
     * to catch doc comments, and set keepComments so the parser records them in
     * the compilation unit.
     *
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void initProcessAnnotations(Iterable<? extends Processor> processors) {
    	DEBUG.P(this,"initProcessAnnotations(1)");
        // Process annotations if processing is not disabled and there
        // is at least one Processor available.
        Options options = Options.instance(context);
        DEBUG.P("options.get(\"-proc:none\")="+options.get("-proc:none"));
        DEBUG.P("JavacProcessingEnvironment procEnvImpl="+procEnvImpl);
        if (options.get("-proc:none") != null) {
            processAnnotations = false;
        } else if (procEnvImpl == null) {
        	/*
        	����javac�������м���"-proc:none"ѡ��ʱ��
        	�ͱ�ʾ��ִ��ע�ʹ����/����룬processAnnotationsΪfalse��
        	
        	
        	����"-proc:none"ѡ��ʱ��������һ��JavacProcessingEnvironment��
        	��ʵ����������ʵ���Ĺ����У��鿴�Ƿ�����������ָ��������ѡ�
        	
        	-processor <class1>[,<class2>,<class3>...]Ҫ���е�ע�ʹ����������ƣ��ƹ�Ĭ�ϵ���������
        	-processorpath <·��>        ָ������ע�ʹ�������λ��
        	
        	���ѡ�-processor��ûָ�����Ͳ���Ĭ�ϵ�ע�ʹ������
        	(ע:Ĭ�ϵ�ע�ʹ��������sun.misc.Service���ṩ��
        	    sun.misc.Service�ಢ��������javac1.7��Դ�����У�
        	    ������rt.jar�ļ��У���û�п�Դ)
        	    
        	���ѡ�-processorpath��ûָ��������-classpathΪ׼��
        	Ȼ��������ָ����·��������ע�ʹ����������ƣ��ҵ�����
        	һ��ע�ʹ������Ļ�����processAnnotationsΪture������
        	��processAnnotationsΪfalse���Ժ󵱵���processAnnotations()����
        	ʱ�����processAnnotations��ȡֵ�����Ƿ��Դ�����е�����ע��
        	���д����/����롣
        	
        	��󻹵�ע��һ��ϸ��:
        	������һ��JavacProcessingEnvironment���ʵ��ʱ���Ѽ�ӵĵ�
        	����com.sun.tools.javac.util.Paths���lazy()�������ڴ˷���
        	�л��PLATFORM_CLASS_PATH,CLASS_PATH,SOURCE_PATH������·��
        	��ֵ���������
        	*/
            procEnvImpl = new JavacProcessingEnvironment(context, processors);
            processAnnotations = procEnvImpl.atLeastOneProcessor();
            
            DEBUG.P("processAnnotations="+processAnnotations);
            if (processAnnotations) {
                if (context.get(Scanner.Factory.scannerFactoryKey) == null)
                    DocCommentScanner.Factory.preRegister(context);
                    
                options.put("save-parameter-names", "save-parameter-names");
                reader.saveParameterNames = true;
				keepComments = true;
				
				if (taskListener != null)
				    taskListener.started(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING));
            } else { // free resources
                procEnvImpl.close();
	    	}
        }
        DEBUG.P(0,this,"initProcessAnnotations(1)");
    }