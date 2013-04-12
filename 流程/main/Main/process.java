	/** Process the option (with arg). Return true if error detected.
	 */
	public boolean process(Options options, String option, String arg) {
		//options�൱��һ��Map<K,V>�����Ժ�ĳ�������о����õ���
		//���Ȱ�keyȡֵ��Ȼ��ȡ����ֵ�Ƿ�Ϊnull�����boolean������ֵ
            if (options != null)
                options.put(option, arg);
	    return false;
	}

	/** Process the option (without arg). Return true if error detected.
	 */
	public boolean process(Options options, String option) {
	    if (hasSuffix)
		return process(options, name.optionName, option.substring(name.optionName.length()));
	    else
		return process(options, option, option);
	}
        
        public OptionKind getKind() { return OptionKind.NORMAL; }
        
        public OptionName getName() { return name; }
    };