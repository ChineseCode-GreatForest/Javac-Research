        public Type withTypeVar(Type t) {
        	try {//�Ҽ��ϵ�
			DEBUG.P(this,"withTypeVar(Type t)");
			DEBUG.P("bound="+bound);
			DEBUG.P("t    ="+t);
			
            //-System.err.println(this+".withTypeVar("+t+");");//DEBUG
            if (bound == t)
                return this;
            bound = (TypeVar)t;
            return this;
            
            }finally{//�Ҽ��ϵ�
            DEBUG.P("");
            DEBUG.P("�������βΣ�"+bound);
            DEBUG.P("������ʵ�Σ�"+this);
			DEBUG.P(1,this,"withTypeVar(Type t)");
			}
        }