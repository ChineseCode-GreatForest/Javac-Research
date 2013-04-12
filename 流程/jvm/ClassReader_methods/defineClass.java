/************************************************************************
 * Loading Classes
 ***********************************************************************/

    /** Define a new class given its name and owner.
     */
    public ClassSymbol defineClass(Name name, Symbol owner) {
    	//DEBUG.P("defineClass(Name name="+name+", Symbol owner="+owner+")");
        ClassSymbol c = new ClassSymbol(0, name, owner);
        
        //��ClassSymbol(0, name, owner)�ڲ��Ѱ�name��owner��flatname��ֵ
        if (owner.kind == PCK)
            assert classes.get(c.flatname) == null : c;//ͬһ���²�����ͬ��������(����)��
        c.completer = this;
        DEBUG.P("����ClassSymbol(name="+name+", owner="+owner+", flags=0, completer=ClassReader)");
        return c;
    }