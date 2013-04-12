    /** Import all classes of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    //tsym������һ����Ҳ������һ���࣬
    //�����һ�������Ͱ�������е������ർ��env.toplevel.starImportScope
    //�����һ���࣬�Ͱ���������ж�������г�Ա�ർ��env.toplevel.starImportScope
    private void importAll(int pos,
                           final TypeSymbol tsym,
                           Env<AttrContext> env) {
        DEBUG.P(this,"importAll(3)");
        DEBUG.P("tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));
        
        //��tsym.kind == PCKʱ˵��tsym��PackageSymbol��ʵ�����ã���ִ��
        //tsym.members()ʱ�����ClassReader���complete()����tsym����ʾ�İ��е�������
        // Check that packages imported from exist (JLS ???).
        if (tsym.kind == PCK && tsym.members().elems == null && !tsym.exists()) {
        	//EXISTS��־��com.sun.tools.javac.jvm.ClassReader.includeClassFile(2)������
        	
            // If we can't find java.lang, exit immediately.
            if (((PackageSymbol)tsym).fullname.equals(names.java_lang)) {
                JCDiagnostic msg = JCDiagnostic.fragment("fatal.err.no.java.lang");
                //��ȫ�޶�����:com.sun.tools.javac.util.FatalError
                throw new FatalError(msg);
            } else {
                //��:import test2.*;(����test2������)
                log.error(pos, "doesnt.exist", tsym);
            }
        }
        final Scope fromScope = tsym.members();
        //java.lang���е���������Ĭ������²���import
        final Scope toScope = env.toplevel.starImportScope;
        
        DEBUG.P("fromScope="+fromScope);
        DEBUG.P("toScope(forǰ)="+toScope);

        for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
        	//����Symbol.ClassSymbol.getKind()�ᴥ��complete()
        	//���Ե���ʱ��ñ���
        	//DEBUG.P("Entry e.sym="+e.sym+" (kind="+e.sym.getKind()+")");
        	//DEBUG.P("e.sym="+e.sym);
        	//DEBUG.P("toScope.nelems="+toScope.nelems);
            if (e.sym.kind == TYP && !toScope.includes(e.sym))
                toScope.enter(e.sym, fromScope);//ע������,��ImportEntry
            else //if (e.sym.kind == TYP && toScope.includes(e.sym))
            	DEBUG.P("e.sym="+e.sym+"  �Ѵ���");
            //DEBUG.P("toScope.nelems="+toScope.nelems);
        }
        
        DEBUG.P("toScope(for��)="+toScope);
        DEBUG.P(1,this,"importAll(3)");    
    }
