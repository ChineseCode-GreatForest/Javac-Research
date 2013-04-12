    // process the non-static imports and the static imports of types.
    public void visitImport(JCImport tree) {
        // <editor-fold defaultstate="collapsed">
        
    	DEBUG.P(this,"visitImport(1)");
    	DEBUG.P("tree.qualid="+tree.qualid);
        DEBUG.P("tree.staticImport="+tree.staticImport);
    	
        JCTree imp = tree.qualid;
        Name name = TreeInfo.name(imp);//ȡ���һ��Ident(��java.util.* �򷵻�*; ��java.util.Map �򷵻�Map)
        TypeSymbol p;
        
        // Create a local environment pointing to this tree to disable
        // effects of other imports in Resolve.findGlobalType
        Env<AttrContext> localEnv = env.dup(tree);//outerΪnull
        //localEnv = env.dup(tree)�൱���Ȱ�env����һ�֣����õ�ǰtree�滻ԭ����tree,
        //�µ�env(localEnv)��nextָ��ԭ����env

        // Attribute qualifying package or class.
        JCFieldAccess s = (JCFieldAccess) imp;
        
        
        /*
        ��Ϊ���еĵ���(import)��䶼����һ��JCFieldAccess��
        ��ʾ��(�μ�Parser.importDeclaration())��
        JCFieldAccess��Ҳ����JCIdent(���һ��selector)��
        ��MemberEnter�׶ε�visitImport(1)�����л���
        ��JCFieldAccess��JCIdent��Symbol sym�ֶ�
        */
        //��û��attribTree()ǰsym����null
        DEBUG.P(2);DEBUG.P("************attribTree()ǰ************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()ǰ************");DEBUG.P(2);

        
        //attribTree()�����е㷱���������Ŀ�
        p = attr.
            attribTree(s.selected,
                       localEnv,
                       tree.staticImport ? TYP : (TYP | PCK),
                       Type.noType).tsym;
        
        
        
        //��attribTree()��ֻ�е�һ��JCFieldAccess��sym��null
        DEBUG.P(2);DEBUG.P("************attribTree()��************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()��************");DEBUG.P(2);  
        
	// </editor-fold>
        
        DEBUG.P("p="+p);
        DEBUG.P("name="+name);    
        //DEBUG.P("visitImport stop",true);          
        if (name == names.asterisk) {
            // Import on demand.
            chk.checkCanonical(s.selected);
            if (tree.staticImport)
                importStaticAll(tree.pos, p, env);
            else
                importAll(tree.pos, p, env);
        } else {
            // Named type import.
            if (tree.staticImport) {
                importNamedStatic(tree.pos(), p, name, localEnv);
                chk.checkCanonical(s.selected);
            } else {
                TypeSymbol c = attribImportType(imp, localEnv).tsym;
                DEBUG.P("TypeSymbol c="+c); 
                chk.checkCanonical(imp);
                importNamed(tree.pos(), c, env);
            }
        }
        
        DEBUG.P(0,this,"visitImport(1)");
    }