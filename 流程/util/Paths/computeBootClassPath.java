    /**
     * ��ָ����-Xbootclasspath/p:<·��>��ʱ���ֿ���<·��>���е�Ŀ¼���ļ��ӽ�Path��
     * ��ȡ��-endorseddirs <Ŀ¼>��ָ����Ŀ¼(��������Ŀ¼)�е�����jar��zip�ļ��ӽ�Path
     * ��ָ����-bootclasspath <·��>��ʱ���ֿ���<·��>���е�Ŀ¼���ļ��ӽ�Path��
     * ��ָ����-Xbootclasspath/a:<·��>��ʱ���ֿ���<·��>���е�Ŀ¼���ļ��ӽ�Path��
     * ��ȡ��-extdirs <Ŀ¼> ��ָ����Ŀ¼(��������Ŀ¼)�е�����jar��zip�ļ��ӽ�Path
     */
    //�˷���һ�����᷵��null
    private Path computeBootClassPath() {
        DEBUG.P(this,"computeBootClassPath()");

        bootClassPathRtJar = null;
		String optionValue;
		Path path = new Path();
		
		DEBUG.P(XBOOTCLASSPATH_PREPEND+"="+options.get(XBOOTCLASSPATH_PREPEND));
		path.addFiles(options.get(XBOOTCLASSPATH_PREPEND));
		
		
		DEBUG.P(ENDORSEDDIRS+"="+options.get(ENDORSEDDIRS));
		
		//-endorseddirs <Ŀ¼> ����ǩ���ı�׼·����λ��
		if ((optionValue = options.get(ENDORSEDDIRS)) != null)
			path.addDirectories(optionValue);
		else {
            DEBUG.P("java.endorsed.dirs="+System.getProperty("java.endorsed.dirs"));
            //���:D:\Java\jre1.6.0\lib\endorsed(��Ŀ¼һ�㲻����)
			path.addDirectories(System.getProperty("java.endorsed.dirs"), false);
		}
	    
		//-bootclasspath <·��>        �����������ļ���λ��
		DEBUG.P(BOOTCLASSPATH+"="+options.get(BOOTCLASSPATH));
        if ((optionValue = options.get(BOOTCLASSPATH)) != null) {
            path.addFiles(optionValue);
        } else {
            DEBUG.P("sun.boot.class.path="+System.getProperty("sun.boot.class.path"));
            //���:sun.boot.class.path=D:\Java\jre1.6.0\lib\resources.jar;D:\Java\jre1.6.0\lib\rt.jar;D:\Java\jre1.6.0\lib\sunrsasign.jar;D:\Java\jre1.6.0\lib\jsse.jar;D:\Java\jre1.6.0\lib\jce.jar;D:\Java\jre1.6.0\lib\charsets.jar;D:\Java\jre1.6.0\classes
            
            //��Ubuntu��������������ʱ
            //java -Xbootclasspath/p:src:classes -Xbootclasspath/a:src:classes -classpath src:classes com.sun.tools.javac.Main
            //���:sun.boot.class.path=src:classes:/home/zhh/java/jdk1.6.0_04/jre/lib/resources.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/rt.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/sunrsasign.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/jsse.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/jce.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/charsets.jar:/home/zhh/java/jdk1.6.0_04/jre/classes:src:classes
            
            // Standard system classes for this compiler's release.
            String files = System.getProperty("sun.boot.class.path");
            path.addFiles(files, false);
            File rt_jar = new File("rt.jar");
            
            DEBUG.P("rt_jar="+rt_jar);
            DEBUG.P("bootClassPathRtJar="+bootClassPathRtJar);
            
            for (String file : new PathIterator(files, null)) {
                File f = new File(file);
                if (new File(f.getName()).equals(rt_jar))
                    bootClassPathRtJar = f;
            }
            DEBUG.P("bootClassPathRtJar="+bootClassPathRtJar);
        }

        DEBUG.P(XBOOTCLASSPATH_APPEND+"="+options.get(XBOOTCLASSPATH_APPEND));
		path.addFiles(options.get(XBOOTCLASSPATH_APPEND));
	
	
		DEBUG.P(EXTDIRS+"="+options.get(EXTDIRS));

		// Strictly speaking, standard extensions are not bootstrap
		// classes, but we treat them identically, so we'll pretend
		// that they are.
		if ((optionValue = options.get(EXTDIRS)) != null)
			path.addDirectories(optionValue);
		else {
            DEBUG.P("java.ext.dirs="+System.getProperty("java.ext.dirs"));
			path.addDirectories(System.getProperty("java.ext.dirs"), false);
		}
	
		DEBUG.P(2,this,"computeBootClassPath()");
		return path;
    }