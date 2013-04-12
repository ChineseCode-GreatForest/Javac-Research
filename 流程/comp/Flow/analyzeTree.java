/**************************************************************************
 * main method
 *************************************************************************/

    /** Perform definite assignment/unassignment analysis on a tree.
     */
    public void analyzeTree(JCTree tree, TreeMaker make) {
		DEBUG.P(5);
		DEBUG.P(this,"analyzeTree(2) ��ʽ��ʼ����������......");
		try {
			this.make = make;
			inits = new Bits();
			uninits = new Bits();
			uninitsTry = new Bits();
			initsWhenTrue = initsWhenFalse =
			uninitsWhenTrue = uninitsWhenFalse = null;
			if (vars == null)
				vars = new VarSymbol[32];
			else
				for (int i=0; i<vars.length; i++)
					vars[i] = null;
			firstadr = 0;
			nextadr = 0;
			pendingExits = new ListBuffer<PendingExit>();
			alive = true;
			this.thrown = this.caught = null;
			this.classDef = null;
			scan(tree);//����com.sun.tools.javac.tree.TreeScanner�ķ���
		} finally {
			// note that recursive invocations of this method fail hard
			inits = uninits = uninitsTry = null;
			initsWhenTrue = initsWhenFalse =
			uninitsWhenTrue = uninitsWhenFalse = null;
			if (vars != null)
				for (int i=0; i<vars.length; i++)
					vars[i] = null;
			firstadr = 0;
			nextadr = 0;
			pendingExits = null;
			this.make = null;
			this.thrown = this.caught = null;
			this.classDef = null;
			
			DEBUG.P(5,this,"analyzeTree(2)");
		}
    }