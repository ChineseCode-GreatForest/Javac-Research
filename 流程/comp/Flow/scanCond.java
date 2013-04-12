    /** Analyze a condition. Make sure to set (un)initsWhenTrue(WhenFalse)
     *	rather than (un)inits on exit.
     */
    void scanCond(JCTree tree) {
		DEBUG.P(this,"scanCond(1)");
		DEBUG.P("tree.type="+tree.type);
		DEBUG.P("tree.type.isFalse()="+tree.type.isFalse());
		DEBUG.P("tree.type.isTrue()="+tree.type.isTrue());
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);

		DEBUG.P("");
		DEBUG.P("inits   ="+inits);
		DEBUG.P("uninits ="+uninits);

		//Bits initsPrev = inits.dup();//�Ҽ��ϵ�
		//Bits uninitsPrev = uninits.dup();//�Ҽ��ϵ�

		if (tree.type.isFalse()) {//��if(false)���������ʽ��ֵ�ڱ���׶���֪�����
			if (inits == null) merge();
			initsWhenTrue = inits.dup();
			//��Ϊ�����if(false)����ôthen��䲿�ݾͲ���ִ�У�
			//���ԾͰ�initsWhenTrue�д�firstadr��nextadr(������)��λ����1,
			//����then������漰�ı������ٶ����Ƕ�����ʼ������
			initsWhenTrue.inclRange(firstadr, nextadr);
			uninitsWhenTrue = uninits.dup();
			//ͬ��
			uninitsWhenTrue.inclRange(firstadr, nextadr);
			initsWhenFalse = inits;
			uninitsWhenFalse = uninits;
		} else if (tree.type.isTrue()) {//��if(true)���������ʽ��ֵ�ڱ���׶���֪�����
			if (inits == null) merge();
			initsWhenFalse = inits.dup();
			//��Ϊ�����if(true)����ôelse��䲿�ݾͲ���ִ�У�
			//���ԾͰ�initsWhenFalse�д�firstadr��nextadr(������)��λ����1,
			//����else������漰�ı������ٶ����Ƕ�����ʼ������
			initsWhenFalse.inclRange(firstadr, nextadr);
			uninitsWhenFalse = uninits.dup();
			//ͬ��
			uninitsWhenFalse.inclRange(firstadr, nextadr);
			initsWhenTrue = inits;
			uninitsWhenTrue = uninits;
		} else {//��if(i>0)���������ʽ�������������ֵ�ڱ���׶�δ֪�����
			scan(tree);
			if (inits != null) split();//��Ҫ���
		}
		inits = uninits = null;

		DEBUG.P("");
		//DEBUG.P("initsǰ         ="+initsPrev+"     inits��="+inits);
		//DEBUG.P("initsWhenFalse  ="+initsWhenFalse);
		//DEBUG.P("initsWhenTrue   ="+initsWhenTrue);
		//DEBUG.P("");
		//DEBUG.P("uninitsǰ       ="+uninitsPrev+"     uninits��="+uninits);

		DEBUG.P("initsWhenFalse   ="+initsWhenFalse);
		DEBUG.P("uninitsWhenFalse ="+uninitsWhenFalse);
		DEBUG.P("");
		DEBUG.P("initsWhenTrue    ="+initsWhenTrue);
		DEBUG.P("uninitsWhenTrue  ="+uninitsWhenTrue);

		//myUninitVars(initsPrev,uninitsPrev);

		myUninitVars(initsWhenFalse.andSet(initsWhenTrue),
			uninitsWhenFalse.andSet(uninitsWhenTrue));
		DEBUG.P(0,this,"scanCond(1)");
    }