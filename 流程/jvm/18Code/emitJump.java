    /** Emit a jump instruction.
     *  Return code pointer of instruction to be patched.
     */
    public int emitJump(int opcode) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitJump(1)");
		DEBUG.P("opcode="+mnem(opcode));
		DEBUG.P("fatcode="+fatcode);
		
		if (fatcode) {
			if (opcode == goto_ || opcode == jsr) {
				//goto_ת����goto_w��jsrת����jsr_w������4���ֽڵ�ƫ����
				emitop4(opcode + goto_w - goto_, 0);
			} else {
				emitop2(negate(opcode), 8);
				emitop4(goto_w, 0);
				alive = true;
				pendingStackMap = needStackMap;
			}
			return cp - 5;
		} else {
			emitop2(opcode, 0);//����0��֮�����resolve(2)�����л���
			//����ָ��λ��(��Ϊemitop2(opcode, 0)��code�����з���3���ֽ�
			//��cp�������1������cp-3�൱�ڻ��˵����ָ���������λ��)
			return cp - 3;
		}
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitJump(1)");
		}
    }