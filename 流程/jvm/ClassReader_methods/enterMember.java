    /** Add member to class unless it is synthetic.
     */
    private void enterMember(ClassSymbol c, Symbol sym) {
    	//ֻ��flags_field��������SYNTHETICʱ��Ϊfalse��
    	//�������(����ͬʱ����SYNTHETIC��BRIDGE)��Ϊtrue
        if ((sym.flags_field & (SYNTHETIC|BRIDGE)) != SYNTHETIC)
            c.members_field.enter(sym);
    }