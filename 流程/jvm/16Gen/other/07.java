    /** Visitor method: generate code for an expression, catching and reporting
     *  any completion failures.
     *  @param tree    The expression to be visited.
     *  @param pt      The expression's expected type (proto-type).
     */
    public Item genExpr(JCTree tree, Type pt) {
    DEBUG.P(this,"genExpr(JCTree tree, Type pt)");
    DEBUG.P("pt="+pt+" tree.type.constValue()="+tree.type.constValue());
	Type prevPt = this.pt;
	
	Item myItemResult=null;//�Ҽ��ϵ�
	try {
	    if (tree.type.constValue() != null) {
		// Short circuit any expressions which are constants
		checkStringConstant(tree.pos(), tree.type.constValue());
		result = items.makeImmediateItem(tree.type, tree.type.constValue());
	    } else {
		this.pt = pt;
		tree.accept(this);
	    }
	    
	    myItemResult=result.coerce(pt);//�Ҽ��ϵ�
	    return myItemResult;//�Ҽ��ϵ�
	    //coerce(Type targettype),coerce(int targetcode)��Items.Item�ж���,
	    //ֻ��Items.ImmediateItem������coerce(int targetcode)
	    //return result.coerce(pt);
	} catch (CompletionFailure ex) {
	    chk.completionError(tree.pos(), ex);
            code.state.stacksize = 1;
	    return items.makeStackItem(pt);
	} finally {
	    this.pt = prevPt;
	    DEBUG.P("result="+result);
	    DEBUG.P("myItemResult="+myItemResult);
	    DEBUG.P(0,this,"genExpr(JCTree tree, Type pt)");
	}
    }