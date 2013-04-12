    /** Import statics types of a given name.  Non-types are handled in Attr.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class from which the name is imported.
     *  @param name          The (simple) name being imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    private void importNamedStatic(final DiagnosticPosition pos,
                                   final TypeSymbol tsym,
                                   final Name name,
                                   final Env<AttrContext> env) {
        try {//�Ҽ��ϵ�                         	
        DEBUG.P(this,"importNamedStatic(4)");
        DEBUG.P("name="+name+" tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));   
        DEBUG.P("env="+env);
        
        if (tsym.kind != TYP) {
            /*����:
            src/my/test/EnterTest.java:18: ������ͽӿھ�̬����
            import static my.MyProcessor;
            ^
            */

            log.error(pos, "static.imp.only.classes.and.interfaces");
            return;
        }

        final Scope toScope = env.toplevel.namedImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;
        
        DEBUG.P("namedImportScopeǰ="+env.toplevel.namedImportScope);
        
        // enter imported types immediately
        new Object() {
            Set<Symbol> processed = new HashSet<Symbol>();
            void importFrom(TypeSymbol tsym) {
                try {//�Ҽ��ϵ�                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");
                
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

				DEBUG.P("tsym.members()="+tsym.members());
                for (Scope.Entry e = tsym.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    Symbol sym = e.sym;
                    
                    DEBUG.P("sym.name="+sym.name);
                    DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
                    DEBUG.P("sym.completer="+sym.completer);
                    DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
                    
                    if (sym.isStatic() &&
                        sym.kind == TYP &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types) &&
                        chk.checkUniqueStaticImport(pos, sym, toScope))
                        toScope.enter(sym, sym.owner.members(), origin.members());
                }
                
                }finally{//�Ҽ��ϵ�
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
        }.importFrom(tsym);
        
        DEBUG.P("namedImportScope��="+env.toplevel.namedImportScope);

        // enter non-types before annotations that might use them
        annotate.earlier(new Annotate.Annotator() {
            Set<Symbol> processed = new HashSet<Symbol>();
            boolean found = false;

            public String toString() {
                return "import static " + tsym + "." + name;
            }
            void importFrom(TypeSymbol tsym) {
				try {//�Ҽ��ϵ�                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");

                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                for (Scope.Entry e = tsym.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    Symbol sym = e.sym;
                    if (sym.isStatic() &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types)) {
                        found = true;
                        if (sym.kind == MTH ||
                            sym.kind != TYP && chk.checkUniqueStaticImport(pos, sym, toScope))
                            toScope.enter(sym, sym.owner.members(), origin.members());
                    }
                }

				}finally{//�Ҽ��ϵ�
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
            public void enterAnnotation() {
				DEBUG.P(this,"enterAnnotation()");
                JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
                try {
                    importFrom(tsym);
					//�������Ĳ���һ����̬��(�����������)�򱨴�
					DEBUG.P("found="+found);
                    if (!found) {
                        log.error(pos, "cant.resolve.location",
                                  JCDiagnostic.fragment("kindname.static"),
                                  name, "", "", Resolve.typeKindName(tsym.type),
                                  tsym.type);
                    }
                } finally {
                    log.useSource(prev);
					DEBUG.P(0,this,"enterAnnotation()");
                }
            }
        });
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"importNamedStatic(4)");
		}
    }