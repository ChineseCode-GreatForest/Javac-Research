    /** Do any of the structures aborted by a non-local exit have
     *  finalizers that require an empty stack?
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    boolean hasFinally(JCTree target, Env<GenContext> env) {
	boolean hasFinally=true;//�Ҽ��ϵ�
	try {//�Ҽ��ϵ�
	DEBUG.P(this,"hasFinally(2)");

	while (env.tree != target) {
	    if (env.tree.tag == JCTree.TRY && env.info.finalize.hasFinalizer())
		return true;
	    env = env.next;
	}

	hasFinally=false;//�Ҽ��ϵ�

	return false;

	}finally{//�Ҽ��ϵ�
	DEBUG.P("hasFinally="+hasFinally);
	DEBUG.P(0,this,"hasFinally(2)");
	}
    }