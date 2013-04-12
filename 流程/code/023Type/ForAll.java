    public static class ForAll extends DelegatedType
            implements Cloneable, ExecutableType {
        public List<Type> tvars;//һ����TypeParameters
        //qtypeһ����MethodType
        public ForAll(List<Type> tvars, Type qtype) {
            super(FORALL, qtype);
            this.tvars = tvars;
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitForAll(this, s);
        }

        public String toString() {
            return "<" + tvars + ">" + qtype;
        }

        //Ϊ������getTypeArguments()��getParameterTypes()�ĸ������ͱ����ĸ���
        //����������ֻҪ���ַ�����Type������ǰ�����ں���ǰ�������ͱ�������
        //����Ƿ�������
        public List<Type> getTypeArguments()   { return tvars; }

        public void setThrown(List<Type> t) {
            qtype.setThrown(t);
        }

        public Object clone() {
            ForAll result = (ForAll)super.clone();
            result.qtype = (Type)result.qtype.clone();
            return result;
        }

        public boolean isErroneous()  {
            return qtype.isErroneous();
        }

        public Type map(Mapping f) {
            return f.apply(qtype);
        }

        public boolean contains(Type elem) {
            return qtype.contains(elem);
        }

        public MethodType asMethodType() {
            return qtype.asMethodType();
        }

        public void complete() {
            for (List<Type> l = tvars; l.nonEmpty(); l = l.tail) {
                ((TypeVar)l.head).bound.complete();
            }
            qtype.complete();
        }

        public List<TypeVar> getTypeVariables() {
            return List.convert(TypeVar.class, getTypeArguments());
        }

        public TypeKind getKind() {
            return TypeKind.EXECUTABLE;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }
    }