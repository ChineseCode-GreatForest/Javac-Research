    /** Emit an opcode with a one-byte operand field.
     */
    public void emitop1(int op, int od) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitop1(int op, int od)");
		DEBUG.P("op="+op+"  od="+od);

		emitop(op);
		if (!alive) return;
		emit1(od);
		switch (op) {
			case bipush://��ʱ��od�ǳ���(8λ)
				state.push(syms.intType);
				break;
			case ldc1://��ʱ��od�ǳ���������
				state.push(typeForPool(pool.pool[od]));
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitop1(int op, int od)");
		}
    }