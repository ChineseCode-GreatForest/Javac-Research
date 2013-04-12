    /** Is exc an exception symbol that need not be declared?
     */
	//ƽ����˵��δ����쳣:
	//����java.lang.Error��java.lang.RuntimeException�������ߵ�����
	//��ν��δ��顱����˵������������Դ�����м����Щ�ط�ʹ�õ���������˵��
	//�쳣����ʹ���ڷ�������throws��throw�׳���������˵���쳣��
	//��ǰ�������ķ����õ��������쳣Ҳ����Ҫ��try/catch����������׳�
	//����������˵���쳣֮����쳣���ǡ��Ѽ���쳣����
	//ֻҪ��������throws��throw�׳��ˡ��Ѽ���쳣����
	//��ô��ǰ�������ķ����õ��������쳣�ͱ�����try/catch����������׳�
    boolean isUnchecked(ClassSymbol exc) {
		return
			exc.kind == ERR ||
			exc.isSubClass(syms.errorType.tsym, types) ||
			exc.isSubClass(syms.runtimeExceptionType.tsym, types);
    }

    /** Is exc an exception type that need not be declared?
     */
    boolean isUnchecked(Type exc) {
		return
			(exc.tag == TYPEVAR) ? isUnchecked(types.supertype(exc)) :
			(exc.tag == CLASS) ? isUnchecked((ClassSymbol)exc.tsym) :
			exc.tag == BOT;
    }

    /** Same, but handling completion failures.
     */
    boolean isUnchecked(DiagnosticPosition pos, Type exc) {
		try {
			return isUnchecked(exc);
		} catch (CompletionFailure ex) {
			completionError(pos, ex);
			return true;
		}
    }