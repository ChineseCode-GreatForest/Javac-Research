    /** Parse contents of file.
     *  @param filename     The name of the file to be parsed.
     */
    public JCTree.JCCompilationUnit parse(JavaFileObject filename) {
    	DEBUG.P(this,"parse(1)");
    	
    	//��log�ڲ��������ļ��л�����ǰ��������ļ�filename
        JavaFileObject prev = log.useSource(filename);
        try {
            JCTree.JCCompilationUnit t = parse(filename, readSource(filename));
            if (t.endPositions != null)
                log.setEndPosTable(filename, t.endPositions);
            return t;
        } finally {
            log.useSource(prev);//��log�ڲ��������ļ��л���ԭ�����ļ�
            DEBUG.P(0,this,"parse(1)");
        }
    }

    /** Try to open input stream with given name.
     *  Report an error if this fails.
     *  @param filename   The file name of the input stream to be opened.
     */
    //��ȫ�޶�����:java.lang.CharSequence
    public CharSequence readSource(JavaFileObject filename) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"readSource(1)");
    	
        try {
            inputFiles.add(filename);
            //������ʵ���ѿ�ʼ��ȡԴ�ļ���������
            //�ο�com.sun.tools.javac.main.Main��compile()�����е�ע��
            return filename.getCharContent(false);
        } catch (IOException e) {
            log.error("error.reading.file", filename, e.getLocalizedMessage());
            return null;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"readSource(1)");
		}
    }