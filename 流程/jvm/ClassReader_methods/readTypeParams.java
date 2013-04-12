    /** Read signature and convert to type parameters.
     */
    List<Type> readTypeParams(int i) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"readTypeParams(1)");
		DEBUG.P("i="+i);
	//i�ǳ������������Ҷ�Ӧtag��CONSTANT_Utf8����	
        int index = poolIdx[i];
        
        DEBUG.P("index="+index);
        //getChar(index + 1)���ֽڳ���
        //index+3��ʾsignature�Ŀ�ʼλ��
        return sigToTypeParams(buf, index + 3, getChar(index + 1));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"readTypeParams(1)");
		}
    }

	/** Convert signature to type parameters, where signature is a name.
     */
    List<Type> sigToTypeParams(Name name) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"sigToTypeParams(1)");
		DEBUG.P("name="+name);

        return sigToTypeParams(name.table.names, name.index, name.len);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"sigToTypeParams(1)");
		}
    }

    /** Convert signature to type parameters, where signature is a byte
     *  array segment.
     */
    List<Type> sigToTypeParams(byte[] sig, int offset, int len) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"sigToTypeParams(3)");
		DEBUG.P("offset="+offset);
		DEBUG.P("len="+len);
		
        signature = sig;
        sigp = offset;
        siglimit = offset + len;
        return sigToTypeParams();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"sigToTypeParams(3)");
		}
    }

    /** Convert signature to type parameters, where signature is implicit.
     */
    List<Type> sigToTypeParams() {
    	DEBUG.P(this,"sigToTypeParams()");
		DEBUG.P("signature[sigp]="+(char)signature[sigp]);
    	
        List<Type> tvars = List.nil();
        if (signature[sigp] == '<') {
            sigp++;
            int start = sigp;
            sigEnterPhase = true;
            while (signature[sigp] != '>')
                tvars = tvars.prepend(sigToTypeParam());
            sigEnterPhase = false;
            sigp = start;

			DEBUG.P("signature[sigp]="+(char)signature[sigp]);
            while (signature[sigp] != '>')
                sigToTypeParam();
            sigp++;
        }
        
        DEBUG.P("tvars.reverse()="+tvars.reverse());
        DEBUG.P(0,this,"sigToTypeParams()");
        return tvars.reverse();
    }
