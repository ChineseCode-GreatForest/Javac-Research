public interface JavacOption {
	
	OptionKind getKind();

    /** Does this option take a (separate) operand? */
    boolean hasArg();

    /** Does argument string match option pattern?
     *  @param arg        The command line argument string.
     */
    boolean matches(String arg);

    /** Process the option (with arg). Return true if error detected.
     */
    boolean process(Options options, String option, String arg);

    /** Process the option (without arg). Return true if error detected.
     */
    boolean process(Options options, String option);
    
    OptionName getName();

    enum OptionKind {
        NORMAL,  //��׼ѡ��
        EXTENDED,//�Ǳ�׼ѡ��(Ҳ����չѡ��,�ñ�׼ѡ�-X�����鿴������չѡ��)
        HIDDEN,  //����ѡ��(�ڲ�ʹ�ã�������ʾ)
    }