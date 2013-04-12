    public void visitMethodDef(JCMethodDecl tree) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
		DEBUG.P("tree="+tree);

		if (tree.body == null) return;

		List<Type> caughtPrev = caught;
		List<Type> mthrown = tree.sym.type.getThrownTypes();
		Bits initsPrev = inits.dup();
		Bits uninitsPrev = uninits.dup();
		int nextadrPrev = nextadr;
		int firstadrPrev = firstadr;
		Lint lintPrev = lint;

		lint = lint.augment(tree.sym.attributes_field);

		assert pendingExits.isEmpty();

		try {
			boolean isInitialConstructor =
			TreeInfo.isInitialConstructor(tree);

			DEBUG.P("isInitialConstructor="+isInitialConstructor);
			DEBUG.P("firstadr="+firstadr);
			DEBUG.P("nextadr="+nextadr);

			if (!isInitialConstructor)
				firstadr = nextadr;

			DEBUG.P("");DEBUG.P("for tree.params......");
			for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
				JCVariableDecl def = l.head;
				DEBUG.P("def="+def);
				scan(def);
				//������������俴����
				//ֻ��Ϊ����newVar(VarSymbol sym)��def.sym.adr��ֵ�����޸�nextadr
				inits.incl(def.sym.adr);
				uninits.excl(def.sym.adr);
				
				DEBUG.P("inits  ="+inits);
				DEBUG.P("uninits="+uninits);DEBUG.P("");
			}

			DEBUG.P(2);DEBUG.P("for tree.params......����");
			DEBUG.P("caught1="+caught);
			
			DEBUG.P("����:"+tree.name+" isInitialConstructor="+isInitialConstructor);
			DEBUG.P("����:"+tree.name+" mthrown="+mthrown);
			//DEBUG.P("mthrown="+mthrown);

			if (isInitialConstructor) //��һ����䲻��this(...)���õĹ��캯��
				caught = chk.union(caught, mthrown);
			//������̬��ʼ���������?��������BLOCK�����
			else if ((tree.sym.flags() & (BLOCK | STATIC)) != BLOCK)
				caught = mthrown;
			// else we are in an instance initializer block;
			// leave caught unchanged.

			DEBUG.P("caught2="+caught);

			alive = true;
			scanStat(tree.body);
			DEBUG.P("������scan����");
			DEBUG.P("alive="+alive);
			DEBUG.P("ree.sym.type.getReturnType()="+tree.sym.type.getReturnType());
			if (alive && tree.sym.type.getReturnType().tag != VOID)
				log.error(TreeInfo.diagEndPos(tree.body), "missing.ret.stmt");

			/*
			������������������һ����һ����䲻��this()���õĹ��췽��ʱ,
			�ڷ�����˹��췽���ķ�����ʱ���������finalʵ���ֶλ��г�ʼ
			�����Ϳ���ֱ�ӱ����ˣ��������������췽���ڲ��Ƿ������ʼ����
			*/
			if (isInitialConstructor) {
				DEBUG.P("firstadr="+firstadr);
				DEBUG.P("nextadr="+nextadr);
				for (int i = firstadr; i < nextadr; i++)
					if (vars[i].owner == classDef.sym)
						checkInit(TreeInfo.diagEndPos(tree.body), vars[i]);
			}


			List<PendingExit> exits = pendingExits.toList();
			pendingExits = new ListBuffer<PendingExit>();
			while (exits.nonEmpty()) {
				PendingExit exit = exits.head;
				exits = exits.tail;
				if (exit.thrown == null) {
					assert exit.tree.tag == JCTree.RETURN;
					if (isInitialConstructor) {
						inits = exit.inits;
						for (int i = firstadr; i < nextadr; i++)
							checkInit(exit.tree.pos(), vars[i]);
					}
				} else {
					// uncaught throws will be reported later
					pendingExits.append(exit);
				}
			}
		} finally {
			inits = initsPrev;
			uninits = uninitsPrev;
			nextadr = nextadrPrev;
			firstadr = firstadrPrev;
			caught = caughtPrev;
			lint = lintPrev;
		}

		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"visitMethodDef(JCMethodDecl tree)");
		}
    }