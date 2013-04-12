    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
        Iterable<? extends File> files)
    {
    	DEBUG.P(this,"getJavaFileObjectsFromFiles(1)");
    	DEBUG.P("files.getClass().getName()="+files.getClass().getName());
    	DEBUG.P("(files instanceof Collection)="+(files instanceof Collection));
    	if (files instanceof Collection)
    		DEBUG.P("(((Collection)files).size())="+(((Collection)files).size()));
    		
        ArrayList<RegularFileObject> result;
        
        //��com.sun.tools.javac.main.Main===>compile(4)������
        //��List<File> filenames����files��
        //com.sun.tools.javac.util.List<T>��
        //�̳���java.util.AbstractCollection<E>�࣬
        //��java.util.AbstractCollection<E>����
        //ʵ����java.util.Collection<E>�ӿ�
        if (files instanceof Collection)
        	//����һ��ArrayList�����ArrayList�ĳ�ʼ��С������size()��Ԫ��
        	//��������Ҳ���ǵ���Ч�����⣬���files��size()������֪�Ļ���
        	//������Ԥ�����size()ָ����С�Ŀռ䣬�������Ժ���ArrayList�����
        	//��Ԫ��ʱ�Ͳ���ÿ�ζ������¿ռ��ˡ�
            result = new ArrayList<RegularFileObject>(((Collection)files).size());
        else
        	//��ʼ��С������10��Ԫ��
        	//(��java.util.ArrayList��ArrayList()������ԭ��)
            result = new ArrayList<RegularFileObject>();
        //ע��ArrayList���size()���ص���ʵ���Ѽ����Ԫ�ظ���
        //����ָ��ʼ��С������С
        //Ҳ����˵�����ʼ��С������С��20��������ArrayList���add����
        //������5��Ԫ��ʱ��size()���ص���5������20
        DEBUG.P("result.size()="+result.size());
        
        for (File f: files)
            result.add(new RegularFileObject(nullCheck(f)));
        
        for (File f: files) DEBUG.P("fileName="+f);
        DEBUG.P("result.size()="+result.size());
        DEBUG.P(0,this,"getJavaFileObjectsFromFiles(1)");
        return result;
    }