    //�û��������·������˳������(ǰһ�������ڲ���������)��
    //javac -classpath==>OS��������CLASSPATH==>application.home(�����֪��������?)==>
    //java -classpath ==>��ǰĿ¼(.)
    //������·�����jar��zip�ļ���Ҫչ��
    //�˷���һ�����᷵��null
    private Path computeUserClassPath() {
		DEBUG.P(CLASSPATH+"="+options.get(CLASSPATH));
		DEBUG.P("env.class.path="+System.getProperty("env.class.path"));
		DEBUG.P("application.home="+System.getProperty("application.home"));
		DEBUG.P("java.class.path="+System.getProperty("java.class.path"));
		
		String cp = options.get(CLASSPATH);
		// CLASSPATH environment variable when run from `javac'.
		if (cp == null) cp = System.getProperty("env.class.path");

		// If invoked via a java VM (not the javac launcher), use the
		// platform class path
		if (cp == null && System.getProperty("application.home") == null)
			cp = System.getProperty("java.class.path");

		// Default to current working directory.
		if (cp == null) cp = ".";

		//��-classpath��ָ����jar�ļ�Ҫչ��
        return new Path()
	    .expandJarClassPaths(true) // Only search user jars for Class-Paths
	    .emptyPathDefault(".")     // Empty path elt ==> current directory
	    .addFiles(cp);
    }