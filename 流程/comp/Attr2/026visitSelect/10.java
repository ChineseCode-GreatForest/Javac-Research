    /** Check kind and type of given tree against protokind and prototype.
     *  If check succeeds, store type in tree and return it.
     *  If check fails, store errType in tree and return it.
     *  No checks are performed if the prototype is a method type.
     *  Its not necessary in this case since we know that kind and type
     *  are correct.
     *
     *  @param tree     The tree whose kind and type is checked
     *  @param owntype  The computed type of the tree
     *  @param ownkind  The computed kind of the tree
     *  @param pkind    The expected kind (or: protokind) of the tree
     *  @param pt       The expected type (or: prototype) of the tree
     */
	//���pkind��PCK��TYP���ͱ�ʾ��ǰsymbol��kind(Ҳ����ownkind)
	//Ҫô��PCK,Ҫô��TYP����������Ǿͱ����ڴ���kind(PCK��TYP)û�ҵ�
	//����tree.type��ֵ
    Type check(JCTree tree, Type owntype, int ownkind, int pkind, Type pt) {
    	DEBUG.P(this,"check(5)");
    	DEBUG.P("tree.type="+tree.type);
    	DEBUG.P("ownkind="+Kinds.toString(ownkind));
    	DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
    	DEBUG.P("pkind="+Kinds.toString(pkind));
    	DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));

        if (owntype.tag != ERROR && pt.tag != METHOD && pt.tag != FORALL) {
        	//���ownkind�������Kinds��pkind��û�У��򱨴�
        	/*���磺���ownkind��VAR,��pkind��PCK��TYP
        	bin\mysrc\my\test\Test.java:3: ���������
			��Ҫ�� �ࡢ�����
			�ҵ��� ����
			*/
            if ((ownkind & ~pkind) == 0) {
                owntype = chk.checkType(tree.pos(), owntype, pt);
            } else {
                log.error(tree.pos(), "unexpected.type",
                          Resolve.kindNames(pkind),
                          Resolve.kindName(ownkind));
                owntype = syms.errType;
            }
        }
        tree.type = owntype;
        DEBUG.P(0,this,"check(5)");
        return owntype;
    }

	    /** Check that a given type is assignable to a given proto-type.
     *  If it is, return the type, otherwise return errType.
     *  @param pos        Position to be used for error reporting.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type checkType(DiagnosticPosition pos, Type found, Type req) {
    try {//�Ҽ��ϵ�
	DEBUG.P(this,"checkType(3)");
	DEBUG.P("found.tag="+TypeTags.toString(found.tag));
	DEBUG.P("req.tag="+TypeTags.toString(req.tag));

	if (req.tag == ERROR)
	    return req;
	if (found.tag == FORALL)
	    return instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
	if (req.tag == NONE)
	    return found;
	if (types.isAssignable(found, req, convertWarner(pos, found, req)))
	    return found;
	if (found.tag <= DOUBLE && req.tag <= DOUBLE)
	    return typeError(pos, JCDiagnostic.fragment("possible.loss.of.precision"), found, req);
	if (found.isSuperBound()) {
	    log.error(pos, "assignment.from.super-bound", found);
	    return syms.errType;
	}
	if (req.isExtendsBound()) {
	    log.error(pos, "assignment.to.extends-bound", req);
	    return syms.errType;
	}
	return typeError(pos, JCDiagnostic.fragment("incompatible.types"), found, req);
	
	
	}finally{//�Ҽ��ϵ�
	DEBUG.P(0,this,"checkType(3)");
	}
    }