        /** The implementation of this (abstract) symbol in class origin;
         *  null if none exists. Synthetic methods are not considered
         *  as possible implementations.
         */
        public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
        	//��ǰ��MethodSymbol����һ�����󷽷������origin�༰�������Ƿ�ʵ���˸÷���
        	try {//�Ҽ��ϵ�
			DEBUG.P(this,"implementation(3)");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
			
            for (Type t = origin.type; t.tag == CLASS; t = types.supertype(t)) {
                TypeSymbol c = t.tsym;
                DEBUG.P("��һ��for:");
                DEBUG.P("TypeSymbol c="+c);
                DEBUG.P("c.members()="+c.members());
                DEBUG.P("lookup(name)="+name);
                DEBUG.P("t.tag="+TypeTags.toString(t.tag));
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    DEBUG.P("�ڶ���for:");
                    DEBUG.P("e.sym="+e.sym);
                    DEBUG.P("e.scope="+e.scope);
                    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
                    if (e.sym.kind == MTH) {
                        MethodSymbol m = (MethodSymbol) e.sym;
                        
						//m�п�����ԭʼʵ����(origin)���߳����еķ�����this�Ǳ�ʵ�ֵĳ��󷽷�
                        boolean overrides=m.overrides(this, origin, types, checkResult);
						//�����abstract���к���abstract������m��this��ָ�������abstract����
						//��ͬһ���������ڵ���overrides����ʱ��
						//��һ����if (this == _other) return true;������䣬
						//Ҳ����˵��ֱ�Ӿ���Ϊ�����໥���ǡ�
                        DEBUG.P("overrides="+overrides);
                        if(overrides) {
                        	if((m.flags() & SYNTHETIC) == 0) {
                        		DEBUG.P(m+".flags() û��SYNTHETIC");
                        		return m;
                        	}
                        }
                        /*		
                        if (m.overrides(this, origin, types, checkResult) &&
                            (m.flags() & SYNTHETIC) == 0)
                            return m;
                            */
                    }
                }
            }
            DEBUG.P("������һ��for");
            DEBUG.P("origin.type="+origin.type);
            // if origin is derived from a raw type, we might have missed
            // an implementation because we do not know enough about instantiations.
            // in this case continue with the supertype as origin.
            if (types.isDerivedRaw(origin.type))
                return implementation(types.supertype(origin.type).tsym, types, checkResult);
            else
                return null;
                
            }finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"implementation(3)");
			}
        }