    /** Fully check membership: hierarchy, protection, and hiding.
     *  Does not exclude methods not inherited due to overriding.
     */
    public boolean isMemberOf(TypeSymbol clazz, Types types) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"isMemberOf(2)");
		DEBUG.P("this.name="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("(owner == clazz)="+(owner == clazz));

    	//��owner == clazzʱ��˵����ǰsymbol��clazz�ĳ�Ա��ֱ�ӷ���true
    	//��clazz.isSubClass(owner, types)����trueʱ����֪clazz��owner
    	//������,����������isInheritedIn(clazz, types)���жϵ�
    	//ǰsymbol(owner�ĳ�Ա,���ֶ�,������)�Ƿ��ܱ�����clazz�̳�������
        /*return
            owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);*/

		boolean isMemberOf=
			owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);
        
		DEBUG.P("");
		DEBUG.P("isMemberOf="+isMemberOf);	
		return isMemberOf;
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"isMemberOf(2)");
		}
    }

    /** Is this symbol the same as or enclosed by the given class? */
    public boolean isEnclosedBy(ClassSymbol clazz) {
    	//���clazz�뵱ǰsmybol��ͬ�����뵱ǰsmybol��(ֱ�ӵĻ��ӵ�)owner��ͬ���򷵻�true
		/*
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner)
            if (sym == clazz) return true;
        return false;
		*/
		
		//�Ҽ��ϵ�
		DEBUG.P(this,"isEnclosedBy(ClassSymbol clazz)");
		DEBUG.P("clazz="+clazz);
        boolean result=false;
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner) {
			DEBUG.P("sym="+sym);
            if (sym == clazz) {
				result=true;
				break;
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"isEnclosedBy(ClassSymbol clazz)");
		return result;
    }

    /** Check for hiding.  Note that this doesn't handle multiple
     *  (interface) inheritance. */
    private boolean hiddenIn(ClassSymbol clazz, Types types) {
		boolean hiddenIn=false;
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"hiddenIn(2)");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("this.kind="+Kinds.toString(kind));
		DEBUG.P("this.flags_field="+Flags.toString(flags_field));
		
    	//����ķ�STATIC�������ܱ�����hidden��ֱ�ӷ���false
        if (kind == MTH && (flags() & STATIC) == 0) return false;
        
        while (true) {
            if (owner == clazz) return false;
            Scope.Entry e = clazz.members().lookup(name);
            while (e.scope != null) {
                if (e.sym == this) return false;
                
                //�����볬��ĳ�Ա�������ͬkind��name�ĳ�Ա��
                //��ô���಻��̳г���ͬkind��name�ĳ�Ա
                if (e.sym.kind == kind &&
                    (kind != MTH ||
                     (e.sym.flags() & STATIC) != 0 &&
                     types.isSubSignature(e.sym.type, type))) {
					hiddenIn=true;
                    return true;
					}
                e = e.next();
            }
            Type superType = types.supertype(clazz.type);
            if (superType.tag != TypeTags.CLASS) return false;
            clazz = (ClassSymbol)superType.tsym;
        }

		}finally{//�Ҽ��ϵ�
		DEBUG.P("");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("hiddenIn="+hiddenIn);	
		DEBUG.P(0,this,"hiddenIn(2)");
		}
    }

    /** Is this symbol inherited into a given class?
     *  PRE: If symbol's owner is a interface,
     *       it is already assumed that the interface is a superinterface
     *       of given class.
     *  @param clazz  The class for which we want to establish membership.
     *                This must be a subclass of the member's owner.
     */
    //�ο������isMemberOf���ڴ��Լٶ�clazz��symbol's owner������
    //�˷����Ĺ������жϵ�ǰsymbol�ܷ�clazz�̳�
    public boolean isInheritedIn(Symbol clazz, Types types) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"isInheritedIn(2)");
		DEBUG.P("this.name="+this.name+" clazz="+clazz);
		DEBUG.P("flags_field="+Flags.toString(flags_field));
		DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));
		

        switch ((int)(flags_field & Flags.AccessFlags)) {
        default: // error recovery
        case PUBLIC:
            return true;
        case PRIVATE:
            return this.owner == clazz;
        case PROTECTED:
            // we model interfaces as extending Object
            return (clazz.flags() & INTERFACE) == 0;
            //�ܱ����ĳ�Ա��ֻ�з�INTERFACE��Symbol������ܼ̳�
            //ע��:����ֻ�ǰ������߼�����⣬ʵ�ʲ�������һ�����������һ���ӿڵ����
            
        case 0:
        //���ʱ�־ȱʡ�ĳ�Ա��ֻ��ͬ���ķ�INTERFACE��Symbol������ܼ̳�
            PackageSymbol thisPackage = this.packge();
            DEBUG.P("");DEBUG.P("case 0");
            DEBUG.P("thisPackage="+thisPackage);
			for (Symbol sup = clazz;
                 sup != null && sup != this.owner;
                 sup = types.supertype(sup.type).tsym) {
                DEBUG.P("sup != null && sup != this.owner="+(sup != null && sup != this.owner));
            	DEBUG.P("sup.type="+sup.type);
            	DEBUG.P("sup.type.isErroneous()="+sup.type.isErroneous());
                if (sup.type.isErroneous())
                    return true; // error recovery
                if ((sup.flags() & COMPOUND) != 0)
                    continue;
                DEBUG.P("(sup.packge() != thisPackage)="+(sup.packge() != thisPackage));
				/*
				//clazz���ڵ�ֱ��this.ownerΪ���ļ̳���(��clazz)�ϵ����������ڵİ����붼��thisPackage
				//ֻҪ��һ������thisPackage������false

				����:
				clazz����ClassC��this����Class1��
				ͨ��"import static my.test.ClassC.*;"���ת���˷���

				package my.test;
				public class ClassA {
					static class Class1{}
				}

				package my;
				public class ClassB extends my.test.ClassA {}

				package my.test;
				public class ClassC extends my.ClassB {}
				*/
                if (sup.packge() != thisPackage)
                    return false;
            }
            return (clazz.flags() & INTERFACE) == 0;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"isInheritedIn(2)");
		}
    }

    /** The (variable or method) symbol seen as a member of given
     *  class type`site' (this might change the symbol's type).
     *  This is used exclusively for producing diagnostics.
     */
    public Symbol asMemberOf(Type site, Types types) {
        throw new AssertionError();
    }

    /** Does this method symbol override `other' symbol, when both are seen as
     *  members of class `origin'?  It is assumed that _other is a member
     *  of origin.
     *
     *  It is assumed that both symbols have the same name.  The static
     *  modifier is ignored for this test.
     *
     *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
     */
    public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        return false;
    }

    /** Complete the elaboration of this symbol's definition.
     */
    public void complete() throws CompletionFailure {
    	DEBUG.P(this,"complete()");
    	DEBUG.P("name="+name+"   completer="+completer);
        if (completer != null) {
            Completer c = completer;
            completer = null;
            //DEBUG.P("c.getClass().getName()="+c.getClass().getName(),true);
            //�����:com.sun.tools.javac.jvm.ClassReader
            //����Ҳ��ע��com.sun.tools.javac.comp.MemberEnter
            c.complete(this);
        }
        DEBUG.P(0,this,"complete()");
    }

    /** True if the symbol represents an entity that exists.
     */
    //ֻ������PackageSymbol�����˴˷�������������û�и��ǡ�
    //��com.sun.tools.javac.comp.Resolve���жԴ˷����д������ã�һ�㶼����true
    public boolean exists() {
        return true;
    }

    public Type asType() {
        return type;
    }

    public Symbol getEnclosingElement() {
        return owner;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;       // most unkind
    }

    public Set<Modifier> getModifiers() {
        return Flags.asModifierSet(flags());
    }

    public Name getSimpleName() {
        return name;
    }

    /**
     * @deprecated this method should never be used by javac internally.
     */
    @Deprecated
    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) {
        return JavacElements.getAnnotation(this, annoType);
    }

    // TODO: getEnclosedElements should return a javac List, fix in FilteredMemberList
    public java.util.List<Symbol> getEnclosedElements() {
        return List.nil();
    }

    public List<TypeSymbol> getTypeParameters() {
        ListBuffer<TypeSymbol> l = ListBuffer.lb();
        for (Type t : type.getTypeArguments()) {
            l.append(t.tsym);
        }
        return l.toList();
    }







































