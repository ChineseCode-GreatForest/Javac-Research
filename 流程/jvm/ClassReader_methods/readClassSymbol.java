    /** Read class entry.
     */
    ClassSymbol readClassSymbol(int i) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"readClassSymbol(int i)");
		DEBUG.P("i="+i);

        return (ClassSymbol) (readPool(i));

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"readClassSymbol(int i)");
		}
    }