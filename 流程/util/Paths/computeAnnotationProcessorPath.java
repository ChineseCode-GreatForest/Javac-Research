    //�˷����п��ܷ���null
    private Path computeAnnotationProcessorPath() {
        try {
        //-processorpath <·��>        ָ������ע�ʹ�������λ��
        DEBUG.P(this,"computeAnnotationProcessorPath()");
        DEBUG.P(PROCESSORPATH+"="+options.get(PROCESSORPATH));
    
		String processorPathArg = options.get(PROCESSORPATH);
		if (processorPathArg == null)
			return null;

		return new Path().addFiles(processorPathArg);
		
		}finally{
		DEBUG.P(0,this,"computeAnnotationProcessorPath()");
		}
    }