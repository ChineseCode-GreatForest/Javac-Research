    /** Emit a multinewarray instruction.
     */
    public void emitMultianewarray(int ndims, int type, Type arrayType) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitMultianewarray(3)");
		DEBUG.P("ndims="+ndims);
		DEBUG.P("type="+type);
		DEBUG.P("arrayType="+arrayType);

		emitop(multianewarray);
        if (!alive) return;
		emit2(type);//�޷���16λ����������(int type����������������˷ѽ⣬Ҳ������type��ʾ�������д�ŵ�����Ԫ������)
		emit1(ndims);//����ά��
		state.pop(ndims);//�Ӷ�ջ����ndims���ֳ���ÿ���ֳ���ֵ��������ÿһά�Ŀ��
		state.push(arrayType);//��arrayTypeѹ���ջ

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitMultianewarray(3)");
		}
    }

    /** Emit newarray.
     */
    public void emitNewarray(int elemcode, Type arrayType) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitNewarray(2)");
		DEBUG.P("elemcode="+elemcode);
		DEBUG.P("arrayType="+arrayType);

		emitop(newarray);
		if (!alive) return;
		emit1(elemcode);//����Ԫ������(��Ӧarraycode�����ķ���ֵ)
		state.pop(1); // count ����Ԫ�ظ���
		state.push(arrayType);

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitNewarray(2)");
		}
    }

    /** Emit anewarray.
     */
    //����һ������Ԫ������Ϊ�������͵�����
    public void emitAnewarray(int od, Type arrayType) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"emitAnewarray(2)");
		DEBUG.P("od="+od);
		DEBUG.P("arrayType="+arrayType);

        emitop(anewarray);
		if (!alive) return;
		emit2(od);//�޷���16λ����������
		state.pop(1);
		state.push(arrayType);

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"emitAnewarray(2)");
		}
    }