    /** Merge the jumps in of two chains into one.
     */
    public static Chain mergeChains(Chain chain1, Chain chain2) {
		try {//�Ҽ��ϵ�
		DEBUG.P(Code.class,"mergeChains(2)");
		DEBUG.P("chain1="+chain1);
		DEBUG.P("chain2="+chain2);

		// recursive merge sort
        if (chain2 == null) return chain1;
        if (chain1 == null) return chain2;
		assert
			chain1.state.stacksize == chain2.state.stacksize &&
			chain1.state.nlocks == chain2.state.nlocks;
	    
	    //��ָ����ƫ����(pc)�Ӵ�С��˳��ϲ�������
        if (chain1.pc < chain2.pc)
            return new Chain(
                chain2.pc,
                mergeChains(chain1, chain2.next),
                chain2.state);
        return new Chain(
                chain1.pc,
                mergeChains(chain1.next, chain2),
                chain1.state);

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,Code.class,"mergeChains(2)");
		}
    }