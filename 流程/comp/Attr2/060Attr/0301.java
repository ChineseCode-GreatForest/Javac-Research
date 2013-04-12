    /** Check the annotations of a symbol.
     */
    public void validateAnnotations(List<JCAnnotation> annotations, Symbol s) {
    try {//�Ҽ��ϵ�
	DEBUG.P(this,"validateAnnotations(2)");
	//DEBUG.P("��ʱ����ע�ͣ������");
	
	DEBUG.P("annotations="+annotations);
	DEBUG.P("s="+s);
	DEBUG.P("skipAnnotations="+skipAnnotations);
	
	
	if (skipAnnotations) return;
	for (JCAnnotation a : annotations)
	    validateAnnotation(a, s);
	   
	}finally{//�Ҽ��ϵ�
	DEBUG.P(2,this,"validateAnnotations(2)");
	}
	
    }

    /** Check an annotation of a symbol.
     */
    public void validateAnnotation(JCAnnotation a, Symbol s) {
    DEBUG.P(this,"validateAnnotation(2)");
    DEBUG.P("a="+a);
	DEBUG.P("s="+s);
	
	validateAnnotation(a);
	/*
	if (!annotationApplicable(a, s))
	    log.error(a.pos(), "annotation.type.not.applicable");
	if (a.annotationType.type.tsym == syms.overrideType.tsym) {
	    if (!isOverrider(s))
		log.error(a.pos(), "method.does.not.override.superclass");
	}
	*/
	//��������log.error()��λ�ö���a.pos()�����Ե�����ͬʱ����ʱ��ֻ����һ������
	boolean annotationApplicableFlag=annotationApplicable(a, s);
	DEBUG.P("annotationApplicableFlag="+annotationApplicableFlag);
	if (!annotationApplicableFlag)
	    log.error(a.pos(), "annotation.type.not.applicable");

	DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
	DEBUG.P("syms.overrideType.tsym="+syms.overrideType.tsym);
	if (a.annotationType.type.tsym == syms.overrideType.tsym) {
		boolean isOverriderFlag=isOverrider(s);
		DEBUG.P("isOverriderFlag="+isOverriderFlag);
	    if (!isOverriderFlag)
		log.error(a.pos(), "method.does.not.override.superclass");
	}
	
	DEBUG.P(1,this,"validateAnnotation(2)");
    }
