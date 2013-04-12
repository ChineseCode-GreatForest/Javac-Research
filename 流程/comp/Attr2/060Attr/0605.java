    /** Fully check membership: hierarchy, protection, and hiding.
     *  Does not exclude methods not inherited due to overriding.
     */
    public boolean isMemberOf(TypeSymbol clazz, Types types) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"isMemberOf(2)");
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("(owner == clazz)="+(owner == clazz));

    	//��owner == clazzʱ��˵����ǰsymbol��clazz�ĳ�Ա��ֱ�ӷ���true
    	//��clazz.isSubClass(owner, types)����trueʱ����֪clazz��owner
    	//������,����������isInheritedIn(clazz, types)���жϵ�
    	//ǰsymbol(owner�ĳ�Ա,���ֶ�,������)�Ƿ��ܱ�����clazz�̳�������
        /*return
            owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);*/

		boolean isMemberOf=
			owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);
        
		DEBUG.P("");
		DEBUG.P("isMemberOf="+isMemberOf);	
		return isMemberOf;
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"isMemberOf(2)");
		}
    }