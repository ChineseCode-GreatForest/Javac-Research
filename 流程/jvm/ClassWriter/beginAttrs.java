    /** Leave space for attribute count and return index for
     *  number of attributes field.
     */
    int beginAttrs() {
    	//���Ը����ȳ�ʼΪ0��������databuf�е����������ҳ��������Ժ�
    	//�ٸ��������޸ĳ�ʵ�ʵ����Ը���
        databuf.appendChar(0);

		//���Ը���(arrtibutes_count)ʵ��ռ���ֽ�
		//�ڻ���ʱ������λ�õ�����һ2(��endAttrs����)
        return databuf.length;
    }