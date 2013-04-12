
    /** Check that class c does not implement directly or indirectly
     *  the same parameterized interface with two different argument lists.
     *  @param pos          Position to be used for error reporting.
     *  @param type         The type whose interfaces are checked.
     */
    void checkClassBounds(DiagnosticPosition pos, Type type) {
		DEBUG.P(this,"checkClassBounds(2)");
		DEBUG.P("type="+type);
		checkClassBounds(pos, new HashMap<TypeSymbol,Type>(), type);
		DEBUG.P(0,this,"checkClassBounds(2)");
    }
//where
        /** Enter all interfaces of type `type' into the hash table `seensofar'
	 *  with their class symbol as key and their type as value. Make
	 *  sure no class is entered with two different types.
	 */
	void checkClassBounds(DiagnosticPosition pos,
			      Map<TypeSymbol,Type> seensofar,
			      Type type) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"checkClassBounds(3)");
		DEBUG.P("seensofar="+seensofar);
		DEBUG.P("type="+type);

	    if (type.isErroneous()) return;
	    for (List<Type> l = types.interfaces(type); l.nonEmpty(); l = l.tail) {
			Type it = l.head;
			Type oldit = seensofar.put(it.tsym, it);
			
			DEBUG.P("Type it="+it);
			DEBUG.P("Type oldit="+oldit);
			
			if (oldit != null) {
				/*��������:
				bin\mysrc\my\test\Test.java:7: �ӿ��ظ�
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest.MyInnerClassStatic implements InterfaceTest<ExtendsTest,M
				yInterfaceA>, InterfaceTest<ExtendsTest,Test> {
				
				
										   ^
				bin\mysrc\my\test\Test.java:7: �޷�ʹ�����²�ͬ�Ĳ����̳� my.InterfaceTest��<my.
				ExtendsTest,my.test.MyInterfaceA> �� <my.ExtendsTest,my.test.Test>
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest.MyInnerClassStatic implements InterfaceTest<ExtendsTest,M
				yInterfaceA>, InterfaceTest<ExtendsTest,Test> {
					   ^
				2 ����
				
				��ӡ���:
				Type it=my.InterfaceTest<my.ExtendsTest,my.test.Test>
				Type oldit=my.InterfaceTest<my.ExtendsTest,my.test.MyInterfaceA>
				oldparams=my.ExtendsTest,my.test.MyInterfaceA
				newparams=my.ExtendsTest,my.test.Test
				*/
				List<Type> oldparams = oldit.allparams();
				List<Type> newparams = it.allparams();
				DEBUG.P("oldparams="+oldparams);
				DEBUG.P("newparams="+newparams);
				if (!types.containsTypeEquivalent(oldparams, newparams))
				log.error(pos, "cant.inherit.diff.arg",
					  it.tsym, Type.toString(oldparams),
					  Type.toString(newparams));
			}
			checkClassBounds(pos, seensofar, it);
	    }
	    Type st = types.supertype(type);
	    DEBUG.P("st="+st);
	    if (st != null) checkClassBounds(pos, seensofar, st);
	    
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"checkClassBounds(3)");
		}	
	}
