/******************************************************************
 * Writing Attributes
 ******************************************************************/

    /** Write header for an attribute to data buffer and return
     *  position past attribute length index.
     */
    int writeAttr(Name attrName) {
    	DEBUG.P(this,"writeAttr(Name attrName)");
		DEBUG.P("attrName="+attrName);
		
        databuf.appendChar(pool.put(attrName));
        //ָattribute_length��ռ4�ֽ�
        databuf.appendInt(0);//�ȳ�ʼΪ0���Ժ��ٻ���
        
		DEBUG.P("alenIdx="+databuf.length);//���Գ�������
		DEBUG.P(0,this,"writeAttr(Name attrName)");

        return databuf.length;
    }