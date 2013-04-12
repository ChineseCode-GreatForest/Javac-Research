    private static final boolean fileSystemIsCaseSensitive =
        File.separatorChar == '/';

    /** Hack to make Windows case sensitive. Test whether given path
     *  ends in a string of characters with the same case as given name.
     *  Ignore file separators in both path and name.
     */
    private boolean caseMapCheck(File f, String name) {
		try {
            DEBUG.P(this,"caseMapCheck(2)");
            DEBUG.P("File f="+f);
			DEBUG.P("f.exists()="+f.exists());
			DEBUG.P("name="+name);
			DEBUG.P("fileSystemIsCaseSensitive="+fileSystemIsCaseSensitive);

        if (fileSystemIsCaseSensitive) return true;
        // Note that getCanonicalPath() returns the case-sensitive
        // spelled file name.
        String path;
        try {
			//��f������ʱ��getCanonicalPath()���������IOException
            path = f.getCanonicalPath();
			DEBUG.P("path="+path);
        } catch (IOException ex) {
			DEBUG.P("IOException ex="+ex);
            return false;
        }
        char[] pcs = path.toCharArray();
        char[] ncs = name.toCharArray();
        int i = pcs.length - 1;
        int j = ncs.length - 1; //��������unnamed packageʱ,j=-1������ture
        //�ж�File f����Ŀ¼�Ƿ���name��β(windowsϵͳ������Ŀ¼��Сд)
		//������Ӧ��Ŀ¼��������ȫһ������Ȼwindowsƽ̨��Ŀ¼�����ִ�Сд
		//���ǵ�������my.testʱ�����windowsƽ̨�Ķ�ӦĿ¼��my\Test��
		//��������ǲ����my\Test��Ѱ���ļ��ģ�ͨ���ᱨ���Ҳ������š�֮��Ĵ���
        while (i >= 0 && j >= 0) {
            while (i >= 0 && pcs[i] == File.separatorChar) i--;
            while (j >= 0 && ncs[j] == File.separatorChar) j--;
            if (i >= 0 && j >= 0) {
                if (pcs[i] != ncs[j]) return false;
                i--;
                j--;
            }
        }
        return j < 0;

		} finally {
			DEBUG.P(0,this,"caseMapCheck(2)");
        }
    }