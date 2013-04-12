    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
	/*
	����javac�����������á�-Xlint:dep-ann��ѡ��ʱ��
	���javadoc�ĵ�����@deprecated��
	����û�мӡ�@Deprecated �����ע�ͱ��ʱ���������ͻᷢ������
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

	    void attribBounds(List<JCTypeParameter> typarams) {
    	DEBUG.P(this,"attribBounds(1)");
    	DEBUG.P("typarams="+typarams);
        for (JCTypeParameter typaram : typarams) {
            Type bound = typaram.type.getUpperBound();
            DEBUG.P("typaram: "+typaram+" bound: "+bound);
            if (bound != null && bound.tsym instanceof ClassSymbol) {
                ClassSymbol c = (ClassSymbol)bound.tsym;
                DEBUG.P("bound.tsym.flags_field="+Flags.toString(c.flags_field));
                if ((c.flags_field & COMPOUND) != 0) {
                    assert (c.flags_field & UNATTRIBUTED) != 0 : c;
                    attribClass(typaram.pos(), c);
                }
            }
        }
        DEBUG.P(0,this,"attribBounds(1)");
    }