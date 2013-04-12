    /** Analyze a statement. Check that statement is reachable.
     */
    void scanStat(JCTree tree) {
		DEBUG.P(this,"scanStat(1)");
		DEBUG.P("alive="+alive+"  (tree != null)="+(tree != null));

		if (!alive && tree != null) {
			/*�������:
				if (dd>0) {
					continue;
					;
					ddd++;
				}
			������ʾ:
			bin\mysrc\my\test\Test.java:105: �޷����ʵ����
									;
									^
			bin\mysrc\my\test\Test.java:106: �޷����ʵ����
									ddd++;
									^
			��Ϊ���������˵���continue�����ʱ������visitContinue(1)-->
			recordExit(1)-->markDead()����markDead()�а�alive��Ϊfalse
			*/
			log.error(tree.pos(), "unreachable.stmt");
			if (tree.tag != JCTree.SKIP) alive = true;
		}
		scan(tree);

		DEBUG.P(1,this,"scanStat(1)");
    }

    /** Analyze list of statements.
     */
    void scanStats(List<? extends JCStatement> trees) {
		DEBUG.P(this,"scanStats(1)");
		if (trees == null) DEBUG.P("trees is null");
		else DEBUG.P("trees.size="+trees.size());
		
		if (trees != null)
			for (List<? extends JCStatement> l = trees; l.nonEmpty(); l = l.tail)
				scanStat(l.head);
		DEBUG.P(0,this,"scanStats(1)");	
    }