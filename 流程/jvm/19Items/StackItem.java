    /** An item representing a value on stack.
     */
    class StackItem extends Item {

		StackItem(int typecode) {
			super(typecode);
		}

		Item load() {
			return this;
		}

		void duplicate() {
			DEBUG.P(this,"duplicate()");
			code.emitop0(width() == 2 ? dup2 : dup);
			DEBUG.P(0,this,"duplicate()");
		}

		void drop() {
			DEBUG.P(this,"drop()");
			code.emitop0(width() == 2 ? pop2 : pop);
			DEBUG.P(0,this,"drop()");
		}

		void stash(int toscode) {
			/*��Ӧ�����ָ��֮һ(�ο�<<����java�����>>P375--P377:
			dup_x1		= 90,//����1��������2(2=1+1)��
			dup_x2		= 91,//����1��������3(3=1+2)��

			dup2_x1		= 93,//����2��������3(3=2+1)��
			dup2_x2		= 94,//����2��������4(4=2+2)��
			
			//(���䷽ʽ:
			//�Ӻ���ߵ���1��ʾdup��2��ʾdup2��
			//�Ӻ��ұߵ�������ָ������x��ĸ�Աߵ�����)
			*/
			code.emitop0(//toscode������VOIDcode
			(width() == 2 ? dup_x2 : dup_x1) + 3 * (Code.width(toscode) - 1));
		}

		int width() {
			//LONGcode��DOUBLEcodeռ�����ֳ�,VOIDcode��ռ�ֳ�������Ϊ1���ֳ���
			//ע���ֳ�������ڶ�ջ���Եģ���java�Ļ���������ռ��bitλ�����޹ء�
			//�����һ����ջ������һ��Ԫ������ΪObject������Ļ���һ���ֳ�����
			//��������е�һ��Ԫ�ء�
			return Code.width(typecode);
		}

		public String toString() {
			return "stack(" + typecodeNames[typecode] + ")";
		}
    }
