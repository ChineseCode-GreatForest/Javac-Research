    JCModifiers optFinal(long flags) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//���������еĲ���ֻ����final��deprecated(��JAVADOC)��ָ��
		//ParserTest(/** @deprecated */ final int i){}
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }