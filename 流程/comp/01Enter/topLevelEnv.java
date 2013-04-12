    /** Create a fresh environment for toplevels.
     *	@param tree	The toplevel tree.
     */
    Env<AttrContext> topLevelEnv(JCCompilationUnit tree) {
		Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
		localEnv.toplevel = tree;
		localEnv.enclClass = predefClassDef;
		tree.namedImportScope = new Scope.ImportScope(tree.packge);
		tree.starImportScope = new Scope.ImportScope(tree.packge);
		localEnv.info.scope = tree.namedImportScope;//ע������
		
		//����Scope[]
		//DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
		//DEBUG.P("tree.starImportScope="+tree.starImportScope);
		//DEBUG.P("localEnv.info.scope="+localEnv.info.scope);
			
		localEnv.info.lint = lint;
		return localEnv;
    } 