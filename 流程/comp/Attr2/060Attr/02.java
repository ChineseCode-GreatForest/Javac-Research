	/** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     */
    void checkNonCyclic(DiagnosticPosition pos, Type t) {
    DEBUG.P(this,"checkNonCyclic(2)");	
	checkNonCyclicInternal(pos, t);
	DEBUG.P(1,this,"checkNonCyclic(2)");
    }

    /** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     *  @returns        True if the check completed on all attributed classes
     */
    private boolean checkNonCyclicInternal(DiagnosticPosition pos, Type t) {
	boolean complete = true; // was the check complete?
	//- System.err.println("checkNonCyclicInternal("+t+");");//DEBUG
	Symbol c = t.tsym;
	
	try {//�Ҽ��ϵ�
	DEBUG.P(this,"checkNonCyclicInternal(2)");
    DEBUG.P("Symbol c="+c);
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
	DEBUG.P("c.type.isErroneous()="+c.type.isErroneous());
	DEBUG.P("c.completer="+c.completer);
	
	//flags_field��һ�����ϱ�־λ,���ǳ�����������(��&����0����!=�Ƚ�)
	//���������ж�flags_field�Ƿ������Ҫ�Ƚϵı�־λ,������Ϊtrue,����Ϊfalse
	//��:���c.flags_field=public unattributed,��ôif ((c.flags_field & ACYCLIC) != 0)=false
	if ((c.flags_field & ACYCLIC) != 0) {
		DEBUG.P(c+" ��ȷ�ϲ�����ѭ�������Բ��ټ�⣬ֱ�ӷ��ء�");
		return true;
	}
	//��ͬһ��Symbol��flags_field��ǰһ���ù�LOCKEDʱ,�ڶ���checkNonCyclicInternalʱ
	//����ͬһ��Symbol,˵���϶�����ѭ���̳�
	if ((c.flags_field & LOCKED) != 0) {
	    noteCyclic(pos, (ClassSymbol)c);
	} else if (!c.type.isErroneous()) {
	    try {
		c.flags_field |= LOCKED;//����
		if (c.type.tag == CLASS) {
		    ClassType clazz = (ClassType)c.type;
		    //�������ʵ�ֵĽӿ�
		    DEBUG.P("��� "+clazz+" �����нӿ�: "+clazz.interfaces_field);
		    if (clazz.interfaces_field != null)
			for (List<Type> l=clazz.interfaces_field; l.nonEmpty(); l=l.tail)
			    complete &= checkNonCyclicInternal(pos, l.head);
			    
			//��鳬��
			DEBUG.P("��� "+clazz+" �ĳ���: "+clazz.supertype_field);
		    if (clazz.supertype_field != null) {
			Type st = clazz.supertype_field;
			if (st != null && st.tag == CLASS)
			    complete &= checkNonCyclicInternal(pos, st);
		    }
		    
		    //����ⲿ��(ͨ������Symbol cΪһ���ڲ���ʱ��c.owner.kind == TYP)
		    DEBUG.P("��� "+clazz+" ��owner: "+c.owner.type);
		    DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
		    if (c.owner.kind == TYP)
			complete &= checkNonCyclicInternal(pos, c.owner.type);
		}
	    } finally {
		c.flags_field &= ~LOCKED;//����
	    }
	}
	if (complete)
	//((c.flags_field & UNATTRIBUTED) == 0)��flags_field������UNATTRIBUTEDʱΪtrue
	    complete = ((c.flags_field & UNATTRIBUTED) == 0) && c.completer == null;
	if (complete) c.flags_field |= ACYCLIC;

	return complete;
	
	
	}finally{//�Ҽ��ϵ�
	DEBUG.P("");
	DEBUG.P("complete="+complete);
	DEBUG.P(c+".flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"checkNonCyclicInternal(2)");
	}
    }

    /** Note that we found an inheritance cycle. */
    private void noteCyclic(DiagnosticPosition pos, ClassSymbol c) {
    DEBUG.P(this,"noteCyclic(2)");
    DEBUG.P("ClassSymbol c="+c);
    
	log.error(pos, "cyclic.inheritance", c);
	for (List<Type> l=types.interfaces(c.type); l.nonEmpty(); l=l.tail)
	    l.head = new ErrorType((ClassSymbol)l.head.tsym);
	Type st = types.supertype(c.type);
	if (st.tag == CLASS)
	    ((ClassType)c.type).supertype_field = new ErrorType((ClassSymbol)st.tsym);
	c.type = new ErrorType(c);
	c.flags_field |= ACYCLIC;
	
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"noteCyclic(2)");
    }