        public List<Type> getInterfaces() {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"getInterfaces()");

            complete();
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.interfaces_field == null) // FIXME: shouldn't be null
                    t.interfaces_field = List.nil();
                return t.interfaces_field;
            } else {
                return List.nil();
            }

			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"getInterfaces()");
			}
        }