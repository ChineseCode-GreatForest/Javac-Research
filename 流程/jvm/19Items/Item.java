    /** The base class of all items, which implements default behavior.
     */
    abstract class Item {
        /** The type code of values represented by this item.
		 */
		int typecode;
		
		Item(int typecode) {
			this.typecode = typecode;
		}

		/** Generate code to load this item onto stack.
		 */
		Item load() {
			throw new AssertionError();
		}

		/** Generate code to store top of stack into this item.
		 */
		void store() {
			throw new AssertionError("store unsupported: " + this);
		}

		/** Generate code to invoke method represented by this item.
		 */
		Item invoke() {
			throw new AssertionError(this);
		}

		/** Generate code to use this item twice.
		 */
		void duplicate() {
			DEBUG.P(this,"duplicate()");
			DEBUG.P("Item.duplicate() do nothing");
			DEBUG.P(0,this,"duplicate()");
		}

		/** Generate code to avoid having to use this item.
		 */
		void drop() {
			DEBUG.P(this,"drop()");
			DEBUG.P("Item.drop() do nothing");
			DEBUG.P(0,this,"drop()");
		}

		/** Generate code to stash a copy of top of stack - of typecode toscode -
		 *  under this item.
		 */
		void stash(int toscode) {
			stackItem[toscode].duplicate();
		}

		/** Generate code to turn item into a testable condition.
		 */
		//����itemѹ���ջ(stack),����һ����ʾifne(���ջ��������0����ת)��CondItem
		//ֻ������CondItem��ImmediateItem���������������
		CondItem mkCond() {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"mkCond()");
			
			load();
			return makeCondItem(ifne); //ifne��ByteCodes����

			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"mkCond()");
			}
		}
		
		/** Generate code to coerce item to given type code.
		 *  @param targetcode    The type code to coerce to.
		 */
		Item coerce(int targetcode) {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"coerce(int targetcode)");
			DEBUG.P("typecode="+typecode+" targetcode="+targetcode);
			
			if (typecode == targetcode)
				return this;
			else {
				load();

				int typecode1 = Code.truncate(typecode);
				int targetcode1 = Code.truncate(targetcode);
				if (typecode1 != targetcode1) {
					int offset = targetcode1 > typecode1 ? targetcode1 - 1
					: targetcode1;
					// <editor-fold defaultstate="collapsed">
					/*��Ӧ�����ָ��֮һ:
					i2l		= 133,
					i2f		= 134,
					i2d		= 135,
					l2i		= 136,
					l2f		= 137,
					l2d		= 138,
					f2i		= 139,
					f2l		= 140,
					f2d		= 141,
					d2i		= 142,
					d2l		= 143,
					d2f		= 144,
					*/
					/*
					ע�������ָ������3��Ϊһ���,���������type code���Ӧ
					int INTcode 	= 0,
					LONGcode 	= 1,
					FLOATcode 	= 2,
					DOUBLEcode 	= 3,
					
					����:��longת��float(Ҳ����l2f = 137����ָ�������еĹ���)
					��Ӧ�������ֵΪ:
					typecode=LONGcode=1,
					targetcode=FLOATcode=2
					�����жϵó�typecode��targetcode�����,
					��int typecode1 = Code.truncate(typecode) =LONGcode=1;
					  int targetcode1 = Code.truncate(targetcode)=FLOATcode=2;
					
					��Ϊtargetcode1>typecode1 
					����int offset=targetcode1 - 1=2-1=LONGcode=1;
					
					���i2l + typecode1 * 3 + offset = 133 + 1 * 3 + 1=137=l2f
					
					���ؼ�����:
					INTcode,LONGcode,FLOATcode,DOUBLEcode��ֵ��1������
					�������ֻ�������֮����໥ת������3��ָ�
					ָ����(ֵ)Ҳ��INT,LONG,FLOAT,DOUBLE��˳��������
					�����ͺ��й����ˡ�
					*/
					// </editor-fold>
					code.emitop0(i2l + typecode1 * 3 + offset);
				}
				/*
				��targetcode��BYTEcode��SHORTcode��CHARcodeʱ,
				targetcode1����Code.truncate(targetcode)���ΪINTcode,
				if (targetcode != targetcode1)��Ϊtrue
				*/
				if (targetcode != targetcode1) {
					/*��Ӧ�����ָ��֮һ:
					int2byte	= 145,
					int2char	= 146,
					int2short	= 147,
					*/
					code.emitop0(int2byte + targetcode - BYTEcode);
				}
				return stackItem[targetcode];
			}
			
			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"coerce(int targetcode)");
			}
		}

		/** Generate code to coerce item to given type.
		 *  @param targettype    The type to coerce to.
		 */
		Item coerce(Type targettype) {
			return coerce(Code.typecode(targettype));
		}

		/** Return the width of this item on stack as a number of words.
		 */
		int width() {
			return 0;
		}

		public abstract String toString();
    }