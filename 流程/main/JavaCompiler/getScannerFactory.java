    protected Scanner.Factory getScannerFactory() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"getScannerFactory()");
    	
        return Scanner.Factory.instance(context);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"getScannerFactory()");
		}
    }







































