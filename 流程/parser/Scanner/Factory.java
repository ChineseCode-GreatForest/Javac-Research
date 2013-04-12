    /** A factory for creating scanners. */
    public static class Factory {
	/** The context key for the scanner factory. */
	public static final Context.Key<Scanner.Factory> scannerFactoryKey =
	    new Context.Key<Scanner.Factory>();

	/** Get the Factory instance for this context. */
	public static Factory instance(Context context) {
	    Factory instance = context.get(scannerFactoryKey);
	    if (instance == null)
		instance = new Factory(context);
	    return instance;
	}

	final Log log;
	final Name.Table names;
	final Source source;
	final Keywords keywords;

	/** Create a new scanner factory. */
	protected Factory(Context context) {
		DEBUG.P(this,"Factory(1)");
	    context.put(scannerFactoryKey, this);
	    this.log = Log.instance(context);
	    this.names = Name.Table.instance(context);
	    this.source = Source.instance(context);
	    this.keywords = Keywords.instance(context);
	    DEBUG.P(0,this,"Factory(1)");
	}

        public Scanner newScanner(CharSequence input) {
        	try {//�Ҽ��ϵ�
        	DEBUG.P(this,"newScanner(1)");
        	//DEBUG.P("input instanceof CharBuffer="+(input instanceof CharBuffer));
        	/*
        	ΪʲôҪ(input instanceof CharBuffer)�أ�
        	��Ϊÿ��Ҫ�����Դ�ļ���������װ����һ
        	��JavacFileManager.RegularFileObject���ʵ�� ,
        	RegularFileObject��ʵ����JavaFileObject�ӿ�,JavaFileObject�ӿڵ�
        	�����ӿ���FileObject����FileObject�ӿ�����һ������(���ڶ�ȡ�ļ�����):
        	java.lang.CharSequence getCharContent(boolean ignoreEncodingErrors)
                                      throws java.io.IOException
                                      
            ��JavacFileManager.RegularFileObject���Ӧ��ʵ�ַ���Ϊ:
            public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors)
                                   throws java.io.IOException
                                   
            �Ƚ����������ķ���ֵ���������ܾ����е�֣���ʵ���ǺϷ��ģ�
            ��Ϊjava.nio.CharBuffer��ʵ����java.lang.CharSequence�ӿ�                   
        	*/
            if (input instanceof CharBuffer) {
                return new Scanner(this, (CharBuffer)input);
            } else {
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
            }
            
            }finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"newScanner(1)");
			}
        }

        public Scanner newScanner(char[] input, int inputLength) {
            return new Scanner(this, input, inputLength);
        }
    }