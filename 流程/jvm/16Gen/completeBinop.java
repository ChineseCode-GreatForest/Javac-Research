     /** Complete generating code for operation, with left operand
	 *  already on stack.
	 *  @param lhs       The tree representing the left operand.
	 *  @param rhs       The tree representing the right operand.
	 *  @param operator  The operator symbol.
	 */
	Item completeBinop(JCTree lhs, JCTree rhs, OperatorSymbol operator) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"completeBinop(3)");
		DEBUG.P("lhs="+lhs);
		DEBUG.P("rhs="+rhs);
		DEBUG.P("operator="+operator);
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));

	    MethodType optype = (MethodType)operator.type;
	    int opcode = operator.opcode;

	    if (opcode >= if_icmpeq && opcode <= if_icmple &&
		rhs.type.constValue() instanceof Number &&
		((Number) rhs.type.constValue()).intValue() == 0) {
			//�����ϵ������ұߵĲ�������0����if_icmpeq��if_icmple��6��ָ��
			//ת����ifeq��ifle��6��ָ������Ͳ��ý��ұߵĲ�����0ѹ���ջ��
			opcode = opcode + (ifeq - if_icmpeq);
	    } else if (opcode >= if_acmpeq && opcode <= if_acmpne &&
				   TreeInfo.isNull(rhs)) {
			//�����ϵ������ұߵĲ�������null����if_acmpeqת����if_acmp_null��
			//��if_acmpneת����if_acmp_nonnull��
			opcode = opcode + (if_acmp_null - if_acmpeq);
	    } else {
			// The expected type of the right operand is
			// the second parameter type of the operator, except for
			// shifts with long shiftcount, where we convert the opcode
			// to a short shift and the expected type to int.
			Type rtype = operator.erasure(types).getParameterTypes().tail.head;

			DEBUG.P("");
			DEBUG.P("operator.type="+operator.type);
			DEBUG.P("operator.erasure(types).getParameterTypes()="+operator.erasure(types).getParameterTypes());
			DEBUG.P("rtype="+rtype);
			if (opcode >= ishll && opcode <= lushrl) {
				//��ishll��lushrl��6���Ǳ�׼ָ��ת����ishl��lushr��6��ָ�
				opcode = opcode + (ishl - ishll);
				rtype = syms.intType;
			}

			DEBUG.P("opcode="+code.mnem(opcode));
			// Generate code for right operand and load.
			genExpr(rhs, rtype).load();
			// If there are two consecutive opcode instructions,
			// emit the first now.
			if (opcode >= (1 << preShift)) { //�ο�Symtab���enterBinop����
				code.emitop0(opcode >> preShift);
				opcode = opcode & 0xFF;
			}
	    }
	    
	    DEBUG.P("opcode="+code.mnem(opcode));
	    if (opcode >= ifeq && opcode <= if_acmpne ||
			opcode == if_acmp_null || opcode == if_acmp_nonnull) {
			return items.makeCondItem(opcode);
	    } else {
			code.emitop0(opcode);
			return items.makeStackItem(optype.restype);
	    }
	    
	    }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"completeBinop(3)");
		}
	}