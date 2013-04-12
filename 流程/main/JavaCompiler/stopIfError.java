    /** The number of errors reported so far.
     */
    public int errorCount() {
        if (delegateCompiler != null && delegateCompiler != this)
            return delegateCompiler.errorCount();
        else
            return log.nerrors;
    }
    
    //�ڱ����ÿ���׶��ﶼ�п����ҵ��������ĳһ�׶��ҵ��˴�����
    //�������Ľ׶������޷����У��ͻ��ȵ���stopIfError()�������������
    //��Ϊ0���ͼ�����һ�׶ε����񣬷�����벻����������
    protected final <T> List<T> stopIfError(ListBuffer<T> listBuffer) {
        if (errorCount() == 0)
            return listBuffer.toList();
        else
            return List.nil();
    }

    protected final <T> List<T> stopIfError(List<T> list) {
        if (errorCount() == 0)
            return list;
        else
            return List.nil();
    }