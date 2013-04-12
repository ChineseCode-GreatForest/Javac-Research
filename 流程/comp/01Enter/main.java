    /** Main method: enter all classes in a list of toplevel trees.
     *	@param trees	  The list of trees to be processed.
     */
    public void main(List<JCCompilationUnit> trees) {
		DEBUG.P(this,"main(1)");
		complete(trees, null);
		DEBUG.P(0,this,"main(1)");
    }

    /** Main method: enter one class from a list of toplevel trees and
     *  place the rest on uncompleted for later processing.
     *  @param trees      The list of trees to be processed.
     *  @param c          The class symbol to be processed.
     */
     
    //�ڴ�MemberEnter�׶ν��е�Resolve.loadClass(Env<AttrContext> env, Name name)ʱ��
    //���һ����ĳ��໹û�б��룬���ȴ�ͷ��ʼ���볬�࣬�ֻ��JavaCompiler.complete(ClassSymbol c)
    //ת�������ʱ ClassSymbol c�Ͳ�Ϊnull��
    public void complete(List<JCCompilationUnit> trees, ClassSymbol c) {
    	DEBUG.P(this,"complete(2)");
    	//DEBUG.P("���EnterǰList<JCCompilationUnit> trees������: trees.size="+trees.size());
    	//DEBUG.P("------------------------------------------------------------------------------");
    	//DEBUG.P(""+trees);
    	//DEBUG.P("------------------------------------------------------------------------------");
		/*
    	if(typeEnvs!=null) {
            DEBUG.P("");
            DEBUG.P("Env����: "+typeEnvs.size());
            DEBUG.P("--------------------------");
            for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
                    DEBUG.P(""+myMapEntry);
            DEBUG.P("");	
        }
        DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
		*/
    	
       
        annotate.enterStart();
        ListBuffer<ClassSymbol> prevUncompleted = uncompleted;
        if (memberEnter.completionEnabled) uncompleted = new ListBuffer<ClassSymbol>();

        DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//0

        try {
            // enter all classes, and construct uncompleted list
            classEnter(trees, null);


            DEBUG.P(5);
            DEBUG.P("***����ڶ��׶�MemberEnter***");
            DEBUG.P("-----------------------------------------------");

            //uncompleted�в���������
            DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
            //DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//!=0

            // complete all uncompleted classes in memberEnter
            if (memberEnter.completionEnabled) {
                if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
                else DEBUG.P("uncompleted=null");
                
                // <editor-fold defaultstate="collapsed">

                while (uncompleted.nonEmpty()) {
                    ClassSymbol clazz = uncompleted.next();
                    DEBUG.P("Uncompleted SymbolName="+clazz);
                    DEBUG.P("clazz.completer="+clazz.completer);
                    DEBUG.P("(c == null)="+(c == null));
                    DEBUG.P("(c == clazz)="+(c == clazz));
                    DEBUG.P("(prevUncompleted == null)="+(prevUncompleted == null));
                    /*
                    if(c!=null) DEBUG.P("c.name="+c.name+" c.kind="+c.kind);
                    else DEBUG.P("c.name=null c.kind=null");
                    if(clazz!=null) DEBUG.P("clazz.name="+clazz.name+" clazz.kind="+clazz.kind);
                    else DEBUG.P("clazz.name=null clazz.kind=null");
                    */

                    //����MemberEnter�׶ν��е�����ʱ��c!=null��c��uncompleted�У�
                    //����c == clazz��������һ�Σ����Զ�c����complete()��
                    //�������c���ڲ��࣬��Ϊc!=null��c != clazz(�ڲ���)��
                    //prevUncompleted != null(���һ�ν���MemberEnter�׶�ʱuncompleted!=null)
                    //����c�������ڲ�����ʱ������complete()���ȷ���prevUncompleted�У������������
                    if (c == null || c == clazz || prevUncompleted == null)
                        clazz.complete();
                    else
                        // defer
                        prevUncompleted.append(clazz);

                    DEBUG.P("");
                }
                // </editor-fold>

				DEBUG.P("trees="+trees);

                // if there remain any unimported toplevels (these must have
                // no classes at all), process their import statements as well.
                for (JCCompilationUnit tree : trees) {
                    DEBUG.P(2);
                    DEBUG.P("tree.starImportScope="+tree.starImportScope);
                    DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
					DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
                    if (tree.starImportScope.elems == null) {
                        JavaFileObject prev = log.useSource(tree.sourcefile);
                        //�е��typeEnvs =new HashMap<TypeSymbol,Env<AttrContext>>();
                        //��tree��JCCompilationUnit����ôget???????????

						//ͬʱ����package-info.javaʱ�ͻ�����������
                        Env<AttrContext> env = typeEnvs.get(tree);
						DEBUG.P("env="+env);
                        if (env == null)
                            env = topLevelEnv(tree);
                        memberEnter.memberEnter(tree, env);
                        log.useSource(prev);
                    }
                }

				DEBUG.P("Enter����:for (JCCompilationUnit tree : trees)");
				DEBUG.P(3);
            }
        } finally {
            uncompleted = prevUncompleted;
            annotate.enterDone();

            if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
            else DEBUG.P("uncompleted=null");

            //DEBUG.P(2);
            //DEBUG.P("���Enter��List<JCCompilationUnit> trees������: trees.size="+trees.size());
            //DEBUG.P("------------------------------------------------------------------------------");
            //DEBUG.P(""+trees);
            //DEBUG.P("------------------------------------------------------------------------------");
            DEBUG.P(2,this,"complete(2)");
        }
    }