    /** Attribute the arguments in a method call, returning a list of types.
     */
    List<Type> attribArgs(List<JCExpression> trees, Env<AttrContext> env) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"attribArgs(2)");
		DEBUG.P("trees="+trees);
		DEBUG.P("env="+env);
		
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkNonVoid(
                l.head.pos(), types.upperBound(attribTree(l.head, env, VAL, Infer.anyPoly))));
        return argtypes.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"attribArgs(2)");
		}
    }