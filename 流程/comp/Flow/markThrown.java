    /** Record that exception is potentially thrown and check that it
     *	is caught.
     */
    void markThrown(JCTree tree, Type exc) {
		DEBUG.P(this,"markThrown(2)");
		DEBUG.P("exc="+exc);
		DEBUG.P("exc.isUnchecked="+chk.isUnchecked(tree.pos(), exc));
		//DEBUG.P("exc.tag="+TypeTags.toString(exc.tag));
		DEBUG.P("caught="+caught);
		DEBUG.P("thrown="+thrown);
		
		//�����õ�ĳһ�������׳����쳣����
		//java.lang.RuntimeException��java.lang.Error��������ʱ��
		//�ҵ�������û�в����쳣ʱ��
		//���쳣����pendingExits(����μ�Check�е�ע��)
		if (!chk.isUnchecked(tree.pos(), exc)) {
			DEBUG.P("exc.isHandled="+chk.isHandled(exc, caught));
			if (!chk.isHandled(exc, caught))
				pendingExits.append(new PendingExit(tree, exc));
			thrown = chk.incl(exc, thrown);
		}
		DEBUG.P("thrown="+thrown);
		DEBUG.P(0,this,"markThrown(2)");
    }