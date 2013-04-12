    /** An item representing a static variable or method.
     */
    class StaticItem extends Item {
		/** The represented symbol.
		 */
		Symbol member;

		StaticItem(Symbol member) {
			super(Code.typecode(member.erasure(types)));
			this.member = member;
		}

		Item load() {
			//pool.put(member)�ķ���ֵΪint����
			code.emitop2(getstatic, pool.put(member));
			return stackItem[typecode];
		}

		void store() {
			code.emitop2(putstatic, pool.put(member));
		}

		Item invoke() {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"invoke()");
			
			MethodType mtype = (MethodType)member.erasure(types);
			int argsize = Code.width(mtype.argtypes);//û���ô�
			int rescode = Code.typecode(mtype.restype);
			int sdiff = Code.width(rescode) - argsize;//û���ô�
			code.emitInvokestatic(pool.put(member), mtype);
			return stackItem[rescode];

			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"invoke()");
			}
		}

		public String toString() {
			return "static(" + member + ")";
		}
    }