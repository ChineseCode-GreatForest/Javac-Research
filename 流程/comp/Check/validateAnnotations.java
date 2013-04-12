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
    /** Check an annotation value.
     */
    public void validateAnnotation(JCAnnotation a) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"validateAnnotation(1)");
		DEBUG.P("a="+a);
		DEBUG.P("a.type="+a.type);
		DEBUG.P("a.type.isErroneous()="+a.type.isErroneous());

        if (a.type.isErroneous()) return;

		DEBUG.P("");
		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("a.annotationType.type.tsym.members()="+a.annotationType.type.tsym.members());
		// collect an inventory of the members
		Set<MethodSymbol> members = new HashSet<MethodSymbol>();
		for (Scope.Entry e = a.annotationType.type.tsym.members().elems;
			 e != null;
			 e = e.sibling)
			if (e.sym.kind == MTH)
					members.add((MethodSymbol) e.sym);
		DEBUG.P("members="+members);

		DEBUG.P("");
		DEBUG.P("a.args="+a.args);
		DEBUG.P("for...............��ʼ");
		// count them off as they're annotated
		for (JCTree arg : a.args) {
			DEBUG.P("arg.tag="+arg.myTreeTag());

			if (arg.tag != JCTree.ASSIGN) continue; // recovery
			JCAssign assign = (JCAssign) arg;
			Symbol m = TreeInfo.symbol(assign.lhs);

			DEBUG.P("m="+m);

			if (m == null || m.type.isErroneous()) continue;
			/*
			���ע�ͳ�Աֵ�Ƿ����ظ������ظ���
			��������ᱨһ���ؼ���Ϊ��duplicate.annotation.member.value���Ĵ���
			
			����Դ����:
			--------------------------------------------------------------------
			package my.error;
			@interface MyAnnotation {
				String value();
			}
			@MyAnnotation(value="testA",value="testB")
			public class duplicate_annotation_member_value  {}
			--------------------------------------------------------------------
			
			���������ʾ��Ϣ����:
			--------------------------------------------------------------------
			bin\mysrc\my\error\duplicate_annotation_member_value.java:5: my.error.MyAnnotation �е�ע�ͳ�Աֵ value �ظ�
			@MyAnnotation(value="testA",value="testB")
											  ^
			1 ����
			--------------------------------------------------------------------
			
			��Ϊmembers=[value()]��a.argsȴ������value��
			���Եڶ���members.remove(m)ʱ������false
			(Ҳ����value()�ڵ�һ��forѭ��ʱ��ɾ�����ڵڶ���forѭ��ʱ�Ѳ�����)
			*/
			if (!members.remove(m))
			log.error(arg.pos(), "duplicate.annotation.member.value",
				  m.name, a.type);

			DEBUG.P("assign.rhs.tag="+assign.rhs.myTreeTag());

			if (assign.rhs.tag == ANNOTATION)
			validateAnnotation((JCAnnotation)assign.rhs);
		}
		DEBUG.P("for...............����");

		DEBUG.P("");
		DEBUG.P("members="+members);

		// all the remaining ones better have default values
		for (MethodSymbol m : members)
			if (m.defaultValue == null && !m.type.isErroneous())
			log.error(a.pos(), "annotation.missing.default.value", 
							  a.type, m.name);

		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("syms.annotationTargetType.tsym="+syms.annotationTargetType.tsym);
		// special case: java.lang.annotation.Target must not have
		// repeated values in its value member
		if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
			a.args.tail == null)
			return;
			
		DEBUG.P("a.args.head.tag="+a.args.head.myTreeTag());
		
			if (a.args.head.tag != JCTree.ASSIGN) return; // error recovery
		JCAssign assign = (JCAssign) a.args.head;
		Symbol m = TreeInfo.symbol(assign.lhs);
		
		DEBUG.P("m.name="+m.name);
		
		if (m.name != names.value) return;
		JCTree rhs = assign.rhs;
		
		DEBUG.P("rhs.tag="+rhs.myTreeTag());
		
		if (rhs.tag != JCTree.NEWARRAY) return;
		JCNewArray na = (JCNewArray) rhs;
		Set<Symbol> targets = new HashSet<Symbol>();
		for (JCTree elem : na.elems) {
			if (!targets.add(TreeInfo.symbol(elem))) {
			log.error(elem.pos(), "repeated.annotation.target");
			}
		}
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"validateAnnotation(1)");
		}
    }