    /** Visitor method: enter all classes in given tree, catching any
     *	completion failure exceptions. Return the tree's type.
     *
     *	@param tree    The tree to be visited.
     *	@param env     The environment visitor argument.
     */
    Type classEnter(JCTree tree, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(JCTree tree, Env<AttrContext> env)");
		//Enter��ֻ��JCCompilationUnit��JCClassDecl��JCTypeParameter��������������visitXXX()����
		//�����������ֻ��һ��Ĭ�ϵ�visitTree(��д�˳���JCTree.Visitor��visitTree)
		DEBUG.P("tree.tag="+tree.myTreeTag());
		Env<AttrContext> prevEnv = this.env;
		DEBUG.P("��ǰEnv="+prevEnv);
		DEBUG.P("��ǰEnv="+env);
		try {
			this.env = env;
			//����JCTree�������accept(Visitor v),�����е�Visitor��Enter���,
			//��JCTree�������accept(Visitor v)�ڲ��ص�Enter�ж�Ӧ��visitXXX()
			tree.accept(this);
			return result;
		}  catch (CompletionFailure ex) {//��ȫ�޶�����:com.sun.tools.javac.code.Symbol.CompletionFailure
			return chk.completionError(tree.pos(), ex);
		} finally {
			DEBUG.P(1,this,"classEnter(JCTree tree, Env<AttrContext> env)");
			this.env = prevEnv;
		}
    }

    /** Visitor method: enter classes of a list of trees, returning a list of types.
     */
    <T extends JCTree> List<Type> classEnter(List<T> trees, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(2)");
		DEBUG.P("List<T> trees.size()="+trees.size());
		ListBuffer<Type> ts = new ListBuffer<Type>();
		for (List<T> l = trees; l.nonEmpty(); l = l.tail)
			ts.append(classEnter(l.head, env));
		DEBUG.P(2,this,"classEnter(2)");
		return ts.toList();
    }