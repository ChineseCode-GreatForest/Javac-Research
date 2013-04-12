    /** Check that symbol is unique in given scope.
     *	@param pos	     Position for error reporting.
     *	@param sym	     The symbol.
     *	@param s	     The scope.
     */
    boolean checkUnique(DiagnosticPosition pos, Symbol sym, Scope s) {
    try {//�Ҽ��ϵ�
	DEBUG.P(this,"checkUnique(3)");
	DEBUG.P("Scope s="+s);
	DEBUG.P("sym.name="+sym.name);
	DEBUG.P("sym.type.isErroneous()="+sym.type.isErroneous());
	
	if (sym.type.isErroneous())
	    return true;
	DEBUG.P("sym.owner.name="+sym.owner.name);    
	if (sym.owner.name == names.any) return false;//errSymbol��Symtab��
		/*
		ע������for�Ľ�������������e.scope != null��������MemberEnter===>methodEnv(2)��
		������Ӧ��scope��nextָ�����scope��������ж������뷽����ͬ���Ƶ����ͱ���
		��:
		class VisitMethodDefTest<T> {
			<T> void m1(int i1,int i2) throws T{}
		}
		�ͻ���ִ���:
		test\memberEnter\VisitMethodDefTest.java:13: ���� test.memberEnter.VisitMethodDefTest �ж��� T
		������Ϊs.lookup(sym.name)����������е�scope����
		*/
		//for (Scope.Entry e = s.lookup(sym.name); e.scope != null; e = e.next()) {
	for (Scope.Entry e = s.lookup(sym.name); e.scope == s; e = e.next()) {
		DEBUG.P("e.scope="+e.scope);
		DEBUG.P("e.sym="+e.sym);
	    if (sym != e.sym &&
		sym.kind == e.sym.kind &&
		sym.name != names.error &&
		/*
		//���������������ǲ��Ƿ��ͷ�����Ҳ�������������ķ���ֵ�Ƿ�һ����
		//ֻҪ������һ������������һ��������Ϊ�Ǵ����
		����:
		void m2(int[] i1) {}
		<T> void m2(int... i1) {}
		��
		void m2(int[] i1) {}
		<T> int m2(int... i1) {}

		����:
		test\memberEnter\VisitMethodDefTest.java:22: �޷��� test.memberEnter.VisitMethod
		DefTest ��ͬʱ���� <T {bound=Object}>m2(int...) �� m2(int[])
				<T> int m2(int... i1) {}
						^
		1 ����
		*/

		(sym.kind != MTH || types.overrideEquivalent(sym.type, e.sym.type))) {
		if ((sym.flags() & VARARGS) != (e.sym.flags() & VARARGS))
		    varargsDuplicateError(pos, sym, e.sym);
		else 
		    duplicateError(pos, e.sym);
		return false;
	    }
	}
	return true;
    
    
    }finally{//�Ҽ��ϵ�
	DEBUG.P(0,this,"checkUnique(3)");
	}
	
    }