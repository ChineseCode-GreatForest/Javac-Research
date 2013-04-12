    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args) {
    	DEBUG.P(this,"compile(1)");
    	
        Context context = new Context();
        JavacFileManager.preRegister(context); // can't create it until Log has been set up
        int result = compile(args, context);
        if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
        
        DEBUG.P(0,this,"compile(1)");
        return result;
    }

    public int compile(String[] args, Context context) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"compile(2)");
		
		//��ȫ�޶�����:com.sun.tools.javac.util.List
		//��ȫ�޶�����:javax.tools.JavaFileObject
    	//List.<JavaFileObject>nil()��ʾ����һ����Ԫ��ΪJavaFileObject��
    	//�͵Ŀ�List(����null������ָsize=0)
        return compile(args, context, List.<JavaFileObject>nil(), null);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"compile(2)");
		}
    }