    /**
     * Parses a list of files.
     */
   public List<JCCompilationUnit> parseFiles(List<JavaFileObject> fileObjects) throws IOException {
       try {//�Ҽ��ϵ�
       DEBUG.P(this,"parseFiles(1) (�﷨����......)");
       
       if (errorCount() > 0)
       	   return List.nil();

        //parse all files
        ListBuffer<JCCompilationUnit> trees = lb();
        //lb()����һ��Ԫ������ΪJCCompilationUnit�Ŀ�ListBuffer
        //��com.sun.tools.javac.util.ListBuffer���ж���;
        for (JavaFileObject fileObject : fileObjects)
            trees.append(parse(fileObject));
        return trees.toList();
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(2,this,"parseFiles(1)");
    	}
    }