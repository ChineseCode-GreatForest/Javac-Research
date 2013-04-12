    /** Enter an inner class into the `innerClasses' set/queue.
     */
    void enterInner(ClassSymbol c) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"enterInner(1)");
		DEBUG.P("c="+c);
		DEBUG.P("innerClassesǰ="+innerClasses);
		DEBUG.P("innerClassesQueueǰ="+innerClassesQueue);

        assert !c.type.isCompound();
        try {
            c.complete();
        } catch (CompletionFailure ex) {
            System.err.println("error: " + c + ": " + ex.getMessage());
            throw ex;
        }
		DEBUG.P("");
		DEBUG.P("c.type="+c.type+"  c.type.tag="+TypeTags.toString(c.type.tag));
		DEBUG.P("pool="+pool);
		if(pool != null) DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        if (c.type.tag != CLASS) return; // arrays
        if (pool != null && // pool might be null if called from xClassName
            c.owner.kind != PCK &&
            (innerClasses == null || !innerClasses.contains(c))) {
				DEBUG.P("�����ڲ���");
//          log.errWriter.println("enter inner " + c);//DEBUG
            if (c.owner.kind == TYP) enterInner((ClassSymbol)c.owner);
            pool.put(c);
            pool.put(c.name);
            if (innerClasses == null) {
                innerClasses = new HashSet<ClassSymbol>();
                innerClassesQueue = new ListBuffer<ClassSymbol>();
                pool.put(names.InnerClasses);
            }
            innerClasses.add(c);
            innerClassesQueue.append(c);
        }

		}finally{//�Ҽ��ϵ�
		DEBUG.P("innerClasses��="+innerClasses);
		DEBUG.P("innerClassesQueue��="+innerClassesQueue);
		DEBUG.P(0,this,"enterInner(1)");
		}
    }