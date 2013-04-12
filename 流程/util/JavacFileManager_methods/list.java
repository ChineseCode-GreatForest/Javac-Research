    public Iterable<JavaFileObject> list(Location location,
                                         String packageName,
                                         Set<JavaFileObject.Kind> kinds,
                                         boolean recurse)
        throws IOException
    {
    	DEBUG.P(this,"list(4)");
        // validatePackageName(packageName);
        nullCheck(packageName);
        nullCheck(kinds);

        //com.sun.tools.javac.util.Paths.Path extends LinkedHashSet<File>��LinkedHashSet<File>��ʵ����Iterable<File>�ӿ� 
        Iterable<? extends File> path = getLocation(location);
        if (path == null)
            return List.nil();
        //for (File f: path) DEBUG.P("file="+f);
        
        //�Ѱ����е�"."�滻��Ŀ¼�ָ���
        String subdirectory = externalizeFileName(packageName);
        ListBuffer<JavaFileObject> results = new ListBuffer<JavaFileObject>();

        //for (File directory : path)
        //    listDirectory(directory, subdirectory, kinds, recurse, results);
        
        for (File directory : path) {
        	DEBUG.P(this,"listDirectory(4)");
            listDirectory(directory, subdirectory, kinds, recurse, results);
            DEBUG.P("ListBuffer<JavaFileObject> results.size="+results.size());
            DEBUG.P(1,this,"listDirectory(4)");
        }
        
        DEBUG.P(2,this,"list(4)");
        return results.toList();
    }