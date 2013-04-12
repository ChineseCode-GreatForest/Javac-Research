    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
		/*
		����javac�����������á�-Xlint:dep-ann��ѡ��ʱ��
		���javadoc�ĵ�����@deprecated��
		����û�мӡ�@Deprecated �����ע�ͱ��ʱ���������ͻᷢ������

		ע����:��-Xlint:dep-ann��ѡ�������-Xlint:deprecation
		*/
		DEBUG.P(this,"checkDeprecatedAnnotation(2)");
		if (allowAnnotations &&
			lint.isEnabled(Lint.LintCategory.DEP_ANN) &&
			(s.flags() & DEPRECATED) != 0 &&
			!syms.deprecatedType.isErroneous() &&
			s.attribute(syms.deprecatedType.tsym) == null) {
			log.warning(pos, "missing.deprecated.annotation");
		}
		DEBUG.P(0,this,"checkDeprecatedAnnotation(2)");
    }