    /*Ϊʲô����������ֻ��һ�δ���?
			int iii;
			if(iii>5) iii++;
			else iii--;
			
			bin\mysrc\my\test\Test.java:91: ������δ��ʼ������ iii
					if(iii>5) iii++;
					   ^
		��Ϊ��scanCond(tree.cond)��scan��iii>5ʱ����ת��checkInit(2),
		��ʱ����inits��û��iii���ͱ���:������δ��ʼ������ iii,�����
		����ʾ��Ϣ���ٰ�iii����inits�У�
		�����ٰ�inits����initsWhenTrue��initsWhenFalse
		
		��	int i=10;
		����int iii;
			if(i>5) iii++;
			else iii--;
		�������δ�:	
		bin\mysrc\my\test\Test.java:91: ������δ��ʼ������ iii
					if(i>5) iii++;
							^
		bin\mysrc\my\test\Test.java:92: ������δ��ʼ������ iii
					else iii--;
						 ^
		����Ϊ:if������������(then��else)�ֱ��
		Ӧ����initsWhenTrue��initsWhenFalse��
		�������if(iii>5)ֻ������Ӧinits����������checkInit(2)��
		����inits�ĵ�ǰֵ����initsWhenTrue��initsWhenFalse������ʱ
		������ֵ���Ѱ����˱���iii��
		
		���Ƕ���if(i>5)��˵���ڵ�����scanCond(tree.cond)��inits��
		û�а�������iii��Ȼ���ֱ�Ӹ���initsWhenTrue��initsWhenFalse��
		������scanStat(tree.thenpart)��scanStat(tree.elsepart)֮ǰ��
		�ְ�initsWhenTrue��initsWhenFalse�ֱ𸳸�inits��������ִ��
		��checkInit(2)ʱ��inits��û�а�������iii���Ӷ������δ���
		������ܺ���
	*/
    public void visitIf(JCIf tree) {
		DEBUG.P(this,"visitIf(1)");
		scanCond(tree.cond);
		Bits initsBeforeElse = initsWhenFalse;
		Bits uninitsBeforeElse = uninitsWhenFalse;
		inits = initsWhenTrue;
		uninits = uninitsWhenTrue;
		DEBUG.P("scanStat(tree.thenpart)��ʼ");
		scanStat(tree.thenpart);
		DEBUG.P("scanStat(tree.thenpart)����");
		if (tree.elsepart != null) {
			DEBUG.P(2);
			DEBUG.P("scanStat(tree.elsepart)��ʼ");
			boolean aliveAfterThen = alive;
			alive = true;
			Bits initsAfterThen = inits.dup();
			Bits uninitsAfterThen = uninits.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			
			scanStat(tree.elsepart);
			inits.andSet(initsAfterThen);
			uninits.andSet(uninitsAfterThen);
			alive = alive | aliveAfterThen;
			DEBUG.P("scanStat(tree.elsepart)����");
		} else {
			inits.andSet(initsBeforeElse);
			uninits.andSet(uninitsBeforeElse);
			alive = true;
		}
		DEBUG.P("alive="+alive);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		myUninitVars(inits,uninits);
		DEBUG.P(0,this,"visitIf(1)");
    }