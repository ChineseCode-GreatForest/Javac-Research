    /**
     * Returns the result of combining the values in this object with 
     * the given annotations and flags.
     */
    public Lint augment(List<Attribute.Compound> attrs, long flags) {
    DEBUG.P(this,"augment(2)");
    DEBUG.P("attrs="+attrs);
    DEBUG.P("flags="+Flags.toString(flags));
    
	Lint l = augmentor.augment(this, attrs);
	
	//�����ǰ����(�緽�������)�Ѽ��ˡ�@Deprecated�����ע�ͱ�ǣ�
	//��ô�����µĳ��������ʹ�õ����������ˡ�@Deprecated���Ķ���
	//��ʱ���پ��棬��Ϊ��ǰ�������Ѳ��޳�ʹ�á�
	if ((flags & DEPRECATED) != 0) {//flags��DEPRECATED�����
	    if (l == this)
		l = new Lint(this);
	    l.values.remove(LintCategory.DEPRECATION);
	    l.suppressedValues.add(LintCategory.DEPRECATION);
	}
	
	DEBUG.P("return lint="+l);
	DEBUG.P(0,this,"augment(2)");
	return l;
    }

	Lint augment(Lint parent, List<Attribute.Compound> attrs) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"augment(2)");
		DEBUG.P("attrs="+attrs);
		DEBUG.P("lint  ="+lint);
		DEBUG.P("parent="+parent);

	    initSyms();
	    this.parent = parent;
	    lint = null;
	    for (Attribute.Compound a: attrs) {
		a.accept(this);
	    }
	    return (lint == null ? parent : lint);
	    
	    }finally{//�Ҽ��ϵ�
	    DEBUG.P("");
		DEBUG.P("lint  ="+lint);
		DEBUG.P("parent="+parent);
		DEBUG.P(0,this,"augment(2)");
		}
	}

	// If we find a @SuppressWarnings annotation, then we continue
	// walking the tree, in order to suppress the individual warnings
	// specified in the @SuppressWarnings annotation.
	public void visitCompound(Attribute.Compound compound) {
		DEBUG.P(this,"visitCompound(1)");
		DEBUG.P("compound="+compound);
		DEBUG.P("compound.type.tsym="+compound.type.tsym);
		DEBUG.P("syms.suppressWarningsType.tsym="+syms.suppressWarningsType.tsym);
		
	    if (compound.type.tsym == syms.suppressWarningsType.tsym) {
		for (List<Pair<MethodSymbol,Attribute>> v = compound.values;
		     v.nonEmpty(); v = v.tail) {
		    Pair<MethodSymbol,Attribute> value = v.head;
		    if (value.fst.name.toString().equals("value")) 
			value.snd.accept(this);
		}
		
	    }
	    
	    DEBUG.P(0,this,"visitCompound(1)");
	}

	public void visitArray(Attribute.Array array) {
		DEBUG.P(this,"visitArray(1)");
		
	    for (Attribute value : array.values) 
		value.accept(this);
		
		DEBUG.P(0,this,"visitArray(1)");
	}
	public void visitConstant(Attribute.Constant value) {
		DEBUG.P(this,"visitConstant(1)");
		DEBUG.P("value="+value);
	    if (value.type.tsym == syms.stringType.tsym) {
		LintCategory lc = LintCategory.get((String) (value.value));
		if (lc != null) 
		    suppress(lc);
	    }
	    DEBUG.P(0,this,"visitConstant(1)");
	}

	private void suppress(LintCategory lc) {
		DEBUG.P(this,"suppress(1)");
		DEBUG.P("lc="+lc);
		DEBUG.P("lint="+lint);
		
	    if (lint == null) 
		lint = new Lint(parent);
	    lint.suppressedValues.add(lc);
	    lint.values.remove(lc);
	    
	    DEBUG.P("");
	    DEBUG.P("lint="+lint);
	    DEBUG.P(0,this,"suppress(1)");
	}
