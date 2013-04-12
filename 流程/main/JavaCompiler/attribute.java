    /**
     * Attribute a list of parse trees, such as found on the "todo" list.
     * Note that attributing classes may cause additional files to be
     * parsed and entered via the SourceCompleter.
     * Attribution of the entries in the list does not stop if any errors occur.
     * @returns a list of environments for attributd classes.
     */
    public List<Env<AttrContext>> attribute(ListBuffer<Env<AttrContext>> envs) {
        ListBuffer<Env<AttrContext>> results = lb();
        while (envs.nonEmpty())
            results.append(attribute(envs.next()));
        return results.toList();
    }

    /**
     * Attribute a parse tree.
     * @returns the attributed parse tree
     */
    public Env<AttrContext> attribute(Env<AttrContext> env) {
    	DEBUG.P(this,"attribute(Env<AttrContext> env)");
    	DEBUG.P("attribute(ǰ) env="+env);
    	//verboseCompilePolicy=true; verbose=true;//�Ҽ��ϵģ�������;
    	
    	
        if (verboseCompilePolicy)
            log.printLines(log.noticeWriter, "[attribute " + env.enclClass.sym + "]");
        if (verbose)
            printVerbose("checking.attribution", env.enclClass.sym);

        if (taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.ANALYZE, env.toplevel, env.enclClass.sym);
            taskListener.started(e);
        }

        JavaFileObject prev = log.useSource(
                                  env.enclClass.sym.sourcefile != null ?
                                  env.enclClass.sym.sourcefile :
                                  env.toplevel.sourcefile);
        try {
            attr.attribClass(env.tree.pos(), env.enclClass.sym);
        }
        finally {
            log.useSource(prev);
        }

        //���е������û��ʼ�ֽ��뷭��
        //DEBUG.P("JCTree.JCCompilationUnit toplevel(���Է�����):"+env.toplevel);
        DEBUG.P("attribute(��) env="+env);
        DEBUG.P(3,this,"attribute(Env<AttrContext> env)");
        return env;
    }