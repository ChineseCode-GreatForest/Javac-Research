    /** Read name.
     */
    Name readName(int i) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"readName(1)");
		
        return (Name) (readPool(i));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"readName(1)");
		}
    }