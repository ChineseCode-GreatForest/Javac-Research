    //�˷����п��ܷ���null
    private Path computeSourcePath() {
		//-sourcepath <·��>           ָ����������Դ�ļ���λ��
		DEBUG.P(SOURCEPATH+"="+options.get(SOURCEPATH));
		
		String sourcePathArg = options.get(SOURCEPATH);
		if (sourcePathArg == null)
			return null;

		return new Path().addFiles(sourcePathArg);
    }