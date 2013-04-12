    /** A class for package symbols
     */
    public static class PackageSymbol extends TypeSymbol
        implements PackageElement {

        public Scope members_field;
        public Name fullname;
        
        //��Ӧpackage-info.java�����
        public ClassSymbol package_info; // see bug 6443073

        public PackageSymbol(Name name, Type type, Symbol owner) {
        	//�����0����flags_field,��Ϊ��һ��PackageSymbol,����û�����η�(modifier)��,
        	//������0��ʾ(ע:����Flags��û�ж���ֵΪ0��flag)
        	//DEBUG.P("flag=0 modifier=("+Flags.toString(0)+")");
        	
            super(0, name, type, owner);
            //������TypeSymbol�Ĺ��췽��ʱ,kindĬ��ȡֵΪTYP,���Ե�����������ΪPCK
            this.kind = PCK;
            this.members_field = null;
            this.fullname = formFullName(name, owner);//��TypeSymbol�ж���
        }

        public PackageSymbol(Name name, Symbol owner) {
            this(name, null, owner);
            this.type = new PackageType(this);
        }

        public String toString() {
            return fullname.toString();
        }

        public Name getQualifiedName() {
            return fullname;
        }

		public boolean isUnnamed() {
		    return name.isEmpty() && owner != null;
		}

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }

        public long flags() {
            if (completer != null) complete();
            return flags_field;
        }

        public List<Attribute.Compound> getAnnotationMirrors() {
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
        }

        /** A package "exists" if a type or package that exists has
         *  been seen within it.
         */
        public boolean exists() {
            return (flags_field & EXISTS) != 0;
        }

        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        public Symbol getEnclosingElement() {
            return null;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitPackage(this, p);
        }
    }