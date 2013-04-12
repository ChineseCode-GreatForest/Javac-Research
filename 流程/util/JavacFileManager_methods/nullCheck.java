    private static <T> T nullCheck(T o) {
    	//���oΪnull����������ʱ�׳�java.lang.NullPointerException
        o.getClass(); // null check
        return o;
    }

    private static <T> Iterable<T> nullCheck(Iterable<T> it) {
        for (T t : it)
            t.getClass(); // null check
        return it;
    }