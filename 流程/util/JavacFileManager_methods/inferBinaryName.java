    //���Ȼ�ø�����file��ȫ��(��:F:\javac\bin\classes\my\test\Test4.class)
    //Ȼ��location����ָ������·��(������·����F:\javac\bin\classes��ͷ),
    //���file��ȫ��һһ����·���е�Ŀ¼�Ƚ�,ֻҪfile��ȫ���п�ʼ������
    //��·���е�ĳһĿ¼��ͬ,������Ƚ�,����ȡfile��ȫ����ʣ�ಿ�֣���Ŀ¼�ָ�
    //���滻��".",ȥ����չ�����õ�һ����ȫ����
    //��F:\javac\bin\classes\my\test\Test4.class��󽫷���my.test.Test4
    public String inferBinaryName(Location location, JavaFileObject file) {
    	try {
    	DEBUG.P(this,"inferBinaryName(2)");
    	
        file.getClass(); // null check
        location.getClass(); // null check
        // Need to match the path semantics of list(location, ...)
        Iterable<? extends File> path = getLocation(location);
        if (path == null) {
            //System.err.println("Path for " + location + " is null");
            return null;
        }
        //System.err.println("Path for " + location + " is " + path);

        if (file instanceof RegularFileObject) {
            RegularFileObject r = (RegularFileObject) file;
            String rPath = r.getPath();
            //DEBUG.P("RegularFileObject " + file + " " +r.getPath());
            //System.err.println("RegularFileObject " + file + " " +r.getPath());
            for (File dir: path) {
                //System.err.println("dir: " + dir);
                String dPath = dir.getPath();
                //DEBUG.P("dir=" + dir);
                //DEBUG.P("dPath=" + dPath);
                if (!dPath.endsWith(File.separator))
                    dPath += File.separator;
                //DEBUG.P("dPath2=" + dPath);
                if (rPath.regionMatches(true, 0, dPath, 0, dPath.length())
                    && new File(rPath.substring(0, dPath.length())).equals(new File(dPath))) {
                    String relativeName = rPath.substring(dPath.length());
                    return removeExtension(relativeName).replace(File.separatorChar, '.');
                }
            }
        } else if (file instanceof ZipFileObject) {
            ZipFileObject z = (ZipFileObject) file;
            String entryName = z.getZipEntryName();
            
            //DEBUG.P("ZipFileObject " + file);
            //DEBUG.P("entryName=" + entryName);
            
            if (entryName.startsWith(symbolFilePrefix))
                entryName = entryName.substring(symbolFilePrefix.length());
            return removeExtension(entryName).replace('/', '.');
        } else
            throw new IllegalArgumentException(file.getClass().getName());
        // System.err.println("inferBinaryName failed for " + file);
        return null;
        
        
    	} finally {
    		DEBUG.P(0,this,"inferBinaryName(2)");
    	}
    }
    // where
        private static String removeExtension(String fileName) {
            int lastDot = fileName.lastIndexOf(".");
            return (lastDot == -1 ? fileName : fileName.substring(0, lastDot));
        }