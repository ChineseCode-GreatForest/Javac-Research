    /**
     * Check if a list of annotations contains a reference to
     * java.lang.Deprecated.
     **/
    private boolean hasDeprecatedAnnotation(List<JCAnnotation> annotations) {
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
			//��ΪMemberEnter�׶��ǽ�����Parser�׶�֮��ģ�����Parser�׶����
			//@Deprecated���в���(��:@Deprecated("str"))����ȷ�ģ�������ʹ����
			//a.args.isEmpty()��Ϊ����ǰ���һ���Ƿ���ȷʹ����@Deprecated���Ա�
			//Ϊ��ǰClassSymbol��flags_field����DEPRECATED(��complete)
            if (a.annotationType.type == syms.deprecatedType && a.args.isEmpty())
                return true;
        }
        return false;
    }