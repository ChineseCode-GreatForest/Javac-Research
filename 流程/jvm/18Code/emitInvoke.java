    /** Emit an invokeinterface instruction.
     */
    public void emitInvokeinterface(int meth, Type mtype) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitInvokeinterface(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokeinterface);
        if (!alive) return;
		emit2(meth);//�޷���16λ����������
		emit1(argsize + 1);//����(����this)�ֳ�����
		emit1(0);//0��invokeinterfaceָ���ռλ�����̶�����
		state.pop(argsize + 1);//�����1�����治ͬ����������ΪҪ�����������ö���1
		
		//<<����JAVA�����>>��404-409ҳ���������ﻹҪpush����ֵ,�����ϵĶ�ջ�ǿյ�
		state.push(mtype.getReturnType());
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitInvokeinterface(int meth, Type mtype)");
		}
    }

    /** Emit an invokespecial instruction.
     */
    //invokespecialָ���ʽ�ǡ�invokespecial 16λ������������
    public void emitInvokespecial(int meth, Type mtype) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitInvokespecial(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokespecial);//��Ӧinvokespecial�ֽ���
        if (!alive) return;
		emit2(meth);//��Ӧ16λ�����������ֽ���
		Symbol sym = (Symbol)pool.pool[meth];
		state.pop(argsize);
		if (sym.isConstructor())
			state.markInitialized((UninitializedType)state.peek());
		state.pop(1);
		state.push(mtype.getReturnType());
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitInvokespecial(int meth, Type mtype)");
		}
    }

    /** Emit an invokestatic instruction.
     */
    public void emitInvokestatic(int meth, Type mtype) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitInvokestatic(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokestatic);
        if (!alive) return;
		emit2(meth);
		state.pop(argsize);
		state.push(mtype.getReturnType());
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitInvokestatic(int meth, Type mtype)");
		}
    }

    /** Emit an invokevirtual instruction.
     */
    public void emitInvokevirtual(int meth, Type mtype) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitInvokevirtual(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokevirtual);
		if (!alive) return;
		emit2(meth);
		state.pop(argsize + 1);
		state.push(mtype.getReturnType());
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitInvokevirtual(int meth, Type mtype)");
		}
    }