    /** An item representing a local variable.
     */
    class LocalItem extends Item {

		/** The variable's register.
		 */
		int reg;

		/** The variable's type.
		 */
		Type type;

		LocalItem(Type type, int reg) {
			super(Code.typecode(type));
			assert reg >= 0;
			this.type = type;
			this.reg = reg;
		}

		Item load() {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"load()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);

			//reg�Ǿֲ�������λ�ã�JVMָ������ֱ��ȡ�ֲ�����λ��0��3��ָ��
			if (reg <= 3)//��Ӧָ��iload_0��aload_3֮һ(ÿһ������������ָ����Գ���4)
				code.emitop0(iload_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(iload + Code.truncate(typecode), reg);
			return stackItem[typecode];

			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"load()");
			}
		}

		void store() {
			DEBUG.P(this,"store()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);
			if (reg <= 3)//��Ӧָ��istore_0��astore_3֮һ
				code.emitop0(istore_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(istore + Code.truncate(typecode), reg);
			code.setDefined(reg);
			DEBUG.P(0,this,"store()");
		}

		void incr(int x) {
			DEBUG.P(this,"incr(int x)");
			DEBUG.P("x="+x+" typecode="+ByteCodes.typecodeNames[typecode]);

			//typecode��xͬΪINTcodeʱ��ֱ��iinc
			if (typecode == INTcode && x >= -32768 && x <= 32767) {
				//�ѳ���ֵx�ӵ�����Ϊreg�ľֲ�����������ֲ�������int����
				code.emitop1w(iinc, reg, x);
			} else {
				//��LocalItemѹ���ջ����ImmediateItem(����x)ѹ���ջ��
				//��ӻ�����󣬽������ת����LocalItem����󱣴浽LocalItem
				
				load();//��LocalItemѹ���ջ
				if (x >= 0) {
					makeImmediateItem(syms.intType, x).load();//��ImmediateItem(����x)ѹ���ջ
					code.emitop0(iadd);//���
				} else {
					makeImmediateItem(syms.intType, -x).load();//��ImmediateItem(����-x)ѹ���ջ
					code.emitop0(isub);//���
				}		
				makeStackItem(syms.intType).coerce(typecode);//�������ת����LocalItem
				store();//���浽LocalItem
			}

			DEBUG.P(0,this,"incr(int x)");
		}

		public String toString() {
			return "localItem(type=" + type + "; reg=" + reg + ")";
		}
    }