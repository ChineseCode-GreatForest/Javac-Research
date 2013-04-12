    /** Is the given method definition an access method
     *  resulting from a qualified super? This is signified by an odd
     *  access code.
     */
    private boolean isAccessSuper(JCMethodDecl enclMethod) {
		return
			(enclMethod.mods.flags & SYNTHETIC) != 0 &&
			isOddAccessName(enclMethod.name);
    }

    /** Does given name start with "access$" and end in an odd digit?
     */
    private boolean isOddAccessName(Name name) {
        //name�����һ��byte��1���С���λ�롱������������1����һ������
		return
			name.startsWith(accessDollar) &&
			(name.byteAt(name.len - 1) & 1) == 1;
    }
