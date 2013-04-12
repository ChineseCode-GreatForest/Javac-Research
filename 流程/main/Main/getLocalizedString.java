    /* ************************************************************************
     * Internationalization
     *************************************************************************/

    /** Find a localized string in the resource bundle.
     *  @param key     The key for the localized string.
     */
    public static String getLocalizedString(String key, Object... args) { // FIXME sb private
        try {
            if (messages == null)
                messages = new Messages(javacBundleName);
            return messages.getLocalizedString("javac." + key, args);
        }
        catch (MissingResourceException e) {
            throw new Error("Fatal Error: Resource for javac is missing", e);
        }
    }

    public static void useRawMessages(boolean enable) {
        if (enable) {
            messages = new Messages(javacBundleName) {
                    public String getLocalizedString(String key, Object... args) {
                        return key;
                    }
                };
        } else {
            messages = new Messages(javacBundleName);
        }
    }
    
    //��Դ�����Ƶ��ַ���ͨ����ȷ���ļ����������ļ���֮ǰ
    //���޶�����(�������"com.sun.tools.javac.resources")��
    //�����������·����ĳһĿ¼��
    private static final String javacBundleName =
        "com.sun.tools.javac.resources.javac";
        
    //��ȫ�޶�����:com.sun.tools.javac.util.Messages
    private static Messages messages;
}