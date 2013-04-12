    /** Import all static members of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    private void importStaticAll(int pos,
                                 final TypeSymbol tsym,
                                 Env<AttrContext> env) {
        try {//�Ҽ��ϵ�                         	
        DEBUG.P(this,"importStaticAll(3)");
        DEBUG.P("tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));   
        DEBUG.P("env="+env);
                              	
        final JavaFileObject sourcefile = env.toplevel.sourcefile;
        final Scope toScope = env.toplevel.starImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;
        
        DEBUG.P("starImportScopeǰ="+env.toplevel.starImportScope);

        // enter imported types immediately
        new Object() {
            Set<Symbol> processed = new HashSet<Symbol>();
            void importFrom(TypeSymbol tsym) {
            	try {//�Ҽ��ϵ�                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");
		//���processed.add(tsym)����true���ʹ���tsym֮ǰû��Set��
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                final Scope fromScope = tsym.members();
                DEBUG.P("fromScope="+fromScope);
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    
                    DEBUG.P("sym.name="+sym.name);
                    DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
                    DEBUG.P("sym.completer="+sym.completer);
                    DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
                    
					/*
                    if (sym.kind == TYP &&
                        (sym.flags() & STATIC) != 0 &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types) &&
                        !toScope.includes(sym))
                        
                        //fromScope.owner������origin.members().owner
                        //������origin.members().owner�����г���������ʵ�ֵĽӿ�
                        toScope.enter(sym, fromScope, origin.members());
					*/

					///*
					boolean flag1,flag2,flag3,flag4,flag5;
					flag1=flag2=flag3=flag4=flag5=false;
					flag1=sym.kind == TYP;
					if(flag1) flag2=(sym.flags() & STATIC) != 0;
					if(flag1 && flag2) flag3=staticImportAccessible(sym, packge);
					if(flag1 && flag2 && flag3) flag4=sym.isMemberOf(origin, types);
					if(flag1 && flag2 && flag3 && flag4) flag5=!toScope.includes(sym);

					if(flag1 && flag2 && flag3 && flag4 && flag5)
						toScope.enter(sym, fromScope, origin.members());

					DEBUG.P("kind == TYP     ="+flag1);
					DEBUG.P("flags == STATIC ="+flag2);
					DEBUG.P("accessible      ="+flag3);
					DEBUG.P("isMemberOf      ="+flag4);
					DEBUG.P("not includes    ="+flag5);
					//*/

                    DEBUG.P("");
                }
                
                }finally{//�Ҽ��ϵ�
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
        }.importFrom(tsym);
        
        DEBUG.P("starImportScope��="+env.toplevel.starImportScope);
        
        // enter non-types before annotations that might use them
        annotate.earlier(new Annotate.Annotator() {
            Set<Symbol> processed = new HashSet<Symbol>();

            public String toString() {
                return "import static " + tsym + ".*" + " in " + sourcefile;
            }
            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                final Scope fromScope = tsym.members();
				DEBUG.P("toScopeǰ="+toScope);
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    if (sym.isStatic() && sym.kind != TYP &&
                        staticImportAccessible(sym, packge) &&
                        !toScope.includes(sym) &&
                        sym.isMemberOf(origin, types)) {
                        toScope.enter(sym, fromScope, origin.members());
                    }
                }
				DEBUG.P("toScope��="+toScope);
            }
            public void enterAnnotation() {
				DEBUG.P(this,"enterAnnotation()");
				DEBUG.P("tsym="+tsym);

                importFrom(tsym);
				DEBUG.P(0,this,"enterAnnotation()");
            }
        });
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"importStaticAll(3)");
		}
    }

    // is the sym accessible everywhere in packge?
    boolean staticImportAccessible(Symbol sym, PackageSymbol packge) {
    	try {//�Ҽ��ϵ�                         	
        DEBUG.P(this,"staticImportAccessible(2)");
        DEBUG.P("sym="+sym);   
        DEBUG.P("sym.packge()="+sym.packge()+" packge="+packge);
        DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
        DEBUG.P("(sym.flags() & AccessFlags)="+Flags.toString((sym.flags() & AccessFlags)));

        int flags = (int)(sym.flags() & AccessFlags);
        switch (flags) {
        default:
        case PUBLIC:
            return true;
        case PRIVATE:
            return false;
        case 0:
        case PROTECTED:
            return sym.packge() == packge;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"staticImportAccessible(2)");
		}
    }