    /** This class represents an option recognized by the main program
     */
    static class Option implements JavacOption {

	/** Option string.
	 */
	OptionName name;

	/** Documentation key for arguments.
	 */
	String argsNameKey;

	/** Documentation key for description.
	 */
	String descrKey;

	/** Suffix option (-foo=bar or -foo:bar)
	 */
	boolean hasSuffix; //ѡ���������һ���ַ���'=' �� ':'
	
	/*
	argsNameKey��descrKey��Documentation������������ļ���:
	com\sun\tools\javac\resources\javac.properties(�ֹ��ʻ��汾)
	
	��:-classpath <·��> ָ�������û����ļ���ע�ʹ�������λ��
	OptionName name    ��ӦCLASSPATH     (-classpath);
	String argsNameKey ��Ӧopt.arg.path  (<·��>);
	String descrKey    ��Ӧopt.classpath (ָ�������û����ļ���ע�ʹ�������λ��); 
	
	
	��RecognizedOptions���getAll()�����ﰴ�ո���
	��������������Option(������������:XOption��HiddenOption)
	*/
	Option(OptionName name, String argsNameKey, String descrKey) {
	    this.name = name;
	    this.argsNameKey = argsNameKey;
	    this.descrKey = descrKey;
	    char lastChar = name.optionName.charAt(name.optionName.length()-1);
	    hasSuffix = lastChar == ':' || lastChar == '=';
	}
	Option(OptionName name, String descrKey) {
	    this(name, null, descrKey);
	}