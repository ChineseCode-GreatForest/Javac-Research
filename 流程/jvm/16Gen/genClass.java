/* ************************************************************************
 * main method
 *************************************************************************/

    /** Generate code for a class definition.
     *  @param env   The attribution environment that belongs to the
     *               outermost class containing this class definition.
     *               We need this for resolving some additional symbols.
     *  @param cdef  The tree representing the class definition.
     *  @return      True if code is generated with no errors.
     */
    public boolean genClass(Env<AttrContext> env, JCClassDecl cdef) {
		DEBUG.P(this,"genClass(2) ���������ֽ���......");
		DEBUG.P("cdef="+cdef);
		DEBUG.P("env="+env);
		try {
			attrEnv = env;
			ClassSymbol c = cdef.sym;
			this.toplevel = env.toplevel;
			this.endPositions = toplevel.endPositions;
			
			DEBUG.P("generateIproxies="+generateIproxies);
			DEBUG.P("allowGenerics="+allowGenerics);
			DEBUG.P("c="+c);
			DEBUG.P("c.flags()="+Flags.toString(c.flags()));

			// If this is a class definition requiring Miranda methods,
			// add them.
			if (generateIproxies && //jdk1.1��jdk1.0����Ҫ
			(c.flags() & (INTERFACE|ABSTRACT)) == ABSTRACT
			&& !allowGenerics // no Miranda methods available with generics
			)
			implementInterfaceMethods(c);
			
			cdef.defs = normalizeDefs(cdef.defs, c);
			//����normalizeDefs(cdef.defs, c)������(defs)��ֻ��������(���췽���ͷǹ��췽��)
			//�ڲ�����ڲ��ӿ�Ҳ������������(defs)��
			DEBUG.P("cdef.defs(�淶���������)="+cdef.defs);
			c.pool = pool;
			pool.reset();
			Env<GenContext> localEnv =
			new Env<GenContext>(cdef, new GenContext());
			localEnv.toplevel = env.toplevel;
			localEnv.enclClass = cdef;
			
			int myMethodCount=1;
			DEBUG.P(2);DEBUG.P("��ʼΪÿһ�����������ֽ���...(�����ܸ���: "+cdef.defs.size()+")");
			for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
			DEBUG.P("�� "+myMethodCount+" ��������ʼ...");
			genDef(l.head, localEnv);
			DEBUG.P("�� "+myMethodCount+" ����������...");
			myMethodCount++;DEBUG.P(2);
			}
			
			if (pool.numEntries() > Pool.MAX_ENTRIES) {
			log.error(cdef.pos(), "limit.pool");
			nerrs++;
			}
			if (nerrs != 0) {
			// if errors, discard code
			for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.METHODDEF)
				((JCMethodDecl) l.head).sym.code = null;
			}
			}
				cdef.defs = List.nil(); // discard trees
			return nerrs == 0;
		} finally {
			// note: this method does NOT support recursion.
			attrEnv = null;
			this.env = null;
			toplevel = null;
			endPositions = null;
			nerrs = 0;
			DEBUG.P(2,this,"genClass(2)");
		}
    }