    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors)
    {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"compile(4)");
    	DEBUG.P("options="+options);
    	
        if (options == null)
            options = Options.instance(context); // creates a new one
            
        //������ʵ���ֶε�ֵ�ڵ���processArgs()����ʱ��
        //����ͨ��RecognizedOptions.HiddenOption(SOURCEFILE)��process()�õ���.
        filenames = new ListBuffer<File>();//�����ʵ������
        classnames = new ListBuffer<String>();
        
        //��ȫ�޶�����:com.sun.tools.javac.main.JavaCompiler
        JavaCompiler comp = null;
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
        	//��javac�����û���κ�ѡ�����ʱ��ʾ������Ϣ
            if (args.length == 0 && fileObjects.isEmpty()) {
                help();
                return EXIT_CMDERR;
            }

            List<File> filenames;//����Ǳ��ر�����ע�����滹�и�ͬ����ʵ������
            try {
                filenames = processArgs(CommandLine.parse(args));
                //��ѡ������ѡ���������ʱprocessArgs()�ķ���ֵ��Ϊnull
                if (filenames == null) {
                    // null signals an error in options, abort
                    return EXIT_CMDERR;
                } else if (filenames.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    if (options.get("-help") != null
                        || options.get("-X") != null
                        || options.get("-version") != null
                        || options.get("-fullversion") != null)
                        return EXIT_OK;
                    error("err.no.source.files");
                    return EXIT_CMDERR;
                }
            } catch (java.io.FileNotFoundException e) {
            	DEBUG.P("java.io.FileNotFoundException");
            	//������쳣��֪�������׳�,
            	//��RecognizedOptions��new HiddenOption(SOURCEFILE)
            	//��process()����helper.error("err.file.not.found", f);
            	//���Դ�ļ�(.java)�����ڵĻ��������ﶼ�д�����ʾ��
            	//����ʹ�ļ������ڣ�Ҳ���׳�FileNotFoundException�쳣
            	
            	//2007-06-01�����ѽ���������:
	            //javac�������п��Դ���windowsƽ̨�ϵ�������
	            //�ļ���Ĳ������磺javac @myfile.bat
	            //���myfile.bat�ļ��Ҳ�������ʾ������Ϣ��:
	            //��javac: �Ҳ����ļ��� myfile.bat (ϵͳ�Ҳ���ָ�����ļ���)��
	            //ͬʱ�����׳�FileNotFoundException�쳣��
	            //֮���˳�CommandLine.parse������processArgs����Ҳ����ִ��
	            //�쳣�����ﱻ����
                Log.printLines(out, ownName + ": " +
                               getLocalizedString("err.file.not.found",
                                                  e.getMessage()));
                return EXIT_SYSERR;
            }
            
            //��֪��"-Xstdout"�������"stdout"��ʲô����
            //�����������в�����ʹ��"stdout"
            //(�����ڳ����ڲ�����options��,������������Դ���룬Ҳû�ҵ����������)
            //2007-05-31�����ѽ���������:
            //����ͨ����-XDstdout=...(����ʵ�������д)��ѡ������
            //��RecognizedOptions��ġ�new HiddenOption(XD)����һ�δ���
            boolean forceStdOut = options.get("stdout") != null;
			DEBUG.P("forceStdOut="+forceStdOut);

            if (forceStdOut) {
                out.flush();
                out = new PrintWriter(System.out, true);
            }
            
            DEBUG.P("����һ��JavacFileManager�����ʵ��...��ʼ");
            
            //����������䲻�ܵ����Ⱥ���򣬷������,
            //����ο�JavacFileManager.preRegister()�е�ע��
            context.put(Log.outKey, out);
            fileManager = context.get(JavaFileManager.class);
            
            DEBUG.P("����һ��JavacFileManager�����ʵ��...����");
            
            
            DEBUG.P(3);
            DEBUG.P("����һ��JavaCompiler�����ʵ��...��ʼ");
            //�ڵõ�JavaCompiler��ʵ���Ĺ���������˺ܶ��ʼ������
            comp = JavaCompiler.instance(context);
            DEBUG.P("����һ��JavaCompiler�����ʵ��...����");
            DEBUG.P(3);
            if (comp == null) return EXIT_SYSERR;

            if (!filenames.isEmpty()) {
                // add filenames to fileObjects
                comp = JavaCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                //��JavacFileManager.getJavaFileObjectsFromFiles()�������
                //ÿһ��Ҫ�����Դ�ļ�������װ����һ��RegularFileObjectʵ����
                //RegularFileObject����JavacFileManager���ڲ��࣬ͬʱʵ����
                //JavaFileObject�ӿڣ�ͨ������getCharContent()��������һ��
                //java.nio.CharBufferʵ�������þͿ��Զ�Դ�ļ����ݽ��н����ˡ�
                //��com.sun.tools.javac.main.JavaCompiler���readSource()��
                //������������Ӧ��
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(filenames))
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            comp.compile(fileObjects,
                         classnames.toList(),
                         processors);

            if (comp.errorCount() != 0 ||
                options.get("-Werror") != null && comp.warningCount() != 0)
                return EXIT_ERROR;
        } catch (IOException ex) {
            ioMessage(ex);
            return EXIT_SYSERR;
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (FatalError ex) {
            feMessage(ex);
            return EXIT_SYSERR;
        } catch(AnnotationProcessingError ex) {
            apMessage(ex);
            return EXIT_SYSERR;
        } catch (ClientCodeException ex) {
            // as specified by javax.tools.JavaCompiler#getTask
            // and javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.get("dev") != null)
                bugMessage(ex);
            return EXIT_ABNORMAL;
        } finally {
            if (comp != null) comp.close();
            filenames = null;
            options = null;
        }
        return EXIT_OK;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"compile(4)");
		}
    }