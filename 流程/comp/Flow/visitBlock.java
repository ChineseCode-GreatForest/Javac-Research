	public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(JCBlock tree)");

		//��scan��JCBlock��,nextadr���ǻ�ԭΪԭ����nextadr
		//��һ��ֵ��ע�⣬��ΪJCBlock���Կ�����һ�����壬
		//���JCBlock���漰�ı������������ģ���JCBlock��scanֻ�ǹ�������
		int nextadrPrev = nextadr;
		scanStats(tree.stats);
		
		DEBUG.P("nextadr��ǰ="+nextadr+" nextadr��ԭ��="+nextadrPrev);
		nextadr = nextadrPrev;
		
		DEBUG.P(0,this,"visitBlock(JCBlock tree)");
    }