    /** Resolve all continues of this statement. */
    boolean resolveContinues(JCTree tree) {
		DEBUG.P(this,"resolveContinues(1)");
		
		boolean result = false;
		List<PendingExit> exits = pendingExits.toList();
		pendingExits = new ListBuffer<PendingExit>();
		DEBUG.P("exits.size="+exits.size());
		for (; exits.nonEmpty(); exits = exits.tail) {
			PendingExit exit = exits.head;
			DEBUG.P("exit.tree.tag="+exit.tree.myTreeTag());
			if (exit.tree.tag == JCTree.CONTINUE &&
			((JCContinue) exit.tree).target == tree) {

				DEBUG.P("exit.inits  ="+exit.inits);
				DEBUG.P("exit.uninits="+exit.uninits);

				DEBUG.P("inits  ǰ   ="+inits);
				DEBUG.P("uninitsǰ   ="+uninits);
				
				//��continue���֮ǰ���б����ĸ�ֵ�����continue���֮��
				//���б����ĸ�ֵ�������λ������(and)
				inits.andSet(exit.inits);
				uninits.andSet(exit.uninits);

				DEBUG.P("inits  ��   ="+inits);
				DEBUG.P("uninits��   ="+uninits);
				result = true;
			} else {
				pendingExits.append(exit);
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"resolveContinues(1)");
		return result;
    }