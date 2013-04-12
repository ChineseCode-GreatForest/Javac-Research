/* *************************************************************************
 * Check annotations
 **************************************************************************/

    /** Annotation types are restricted to primitives, String, an
     *  enum, an annotation, Class, Class<?>, Class<? extends
     *  Anything>, arrays of the preceding.
     */
    void validateAnnotationType(JCTree restype) {
        // restype may be null if an error occurred, so don't bother validating it
        if (restype != null) {
            validateAnnotationType(restype.pos(), restype.type);
        }
    }

    void validateAnnotationType(DiagnosticPosition pos, Type type) {
		if (type.isPrimitive()) return;
		if (types.isSameType(type, syms.stringType)) return;
		if ((type.tsym.flags() & Flags.ENUM) != 0) return;
		if ((type.tsym.flags() & Flags.ANNOTATION) != 0) return;
		if (types.lowerBound(type).tsym == syms.classType.tsym) return;
		if (types.isArray(type) && !types.isArray(types.elemtype(type))) {
			validateAnnotationType(pos, types.elemtype(type));
			return;
		}
		log.error(pos, "invalid.annotation.member.type");
    }

    /**
     * "It is also a compile-time error if any method declared in an
     * annotation type has a signature that is override-equivalent to
     * that of any public or protected method declared in class Object
     * or in the interface annotation.Annotation."
     *
     * @jls3 9.6 Annotation Types
     */
    void validateAnnotationMethod(DiagnosticPosition pos, MethodSymbol m) {
        for (Type sup = syms.annotationType; sup.tag == CLASS; sup = types.supertype(sup)) {
            Scope s = sup.tsym.members();
            for (Scope.Entry e = s.lookup(m.name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH &&
                    (e.sym.flags() & (PUBLIC | PROTECTED)) != 0 &&
                    types.overrideEquivalent(m.type, e.sym.type))
                    log.error(pos, "intf.annotation.member.clash", e.sym, sup);
            }
        }
    }

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

    /** Is s a method symbol that overrides a method in a superclass? */
    boolean isOverrider(Symbol s) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"isOverrider(Symbol s)");
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
        if (s.kind != MTH || s.isStatic()) //��̬������Զ���Ḳ�ǳ����еľ�̬����
            return false;
        MethodSymbol m = (MethodSymbol)s;
        TypeSymbol owner = (TypeSymbol)m.owner;
        
        DEBUG.P("m="+m);
        DEBUG.P("owner="+owner);
        
        for (Type sup : types.closure(owner.type)) {
            if (sup == owner.type)
                continue; // skip "this"
            Scope scope = sup.tsym.members();
            DEBUG.P("scope="+scope);
            for (Scope.Entry e = scope.lookup(m.name); e.scope != null; e = e.next()) {
                if (!e.sym.isStatic() && m.overrides(e.sym, owner, types, true))
                    return true;
            }
        }
        return false;
        
		}finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"isOverrider(Symbol s)");
		}  
    }

    /** Is the annotation applicable to the symbol? */
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"annotationApplicable(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
		Attribute.Compound atTarget =
			a.annotationType.type.tsym.attribute(syms.annotationTargetType.tsym);
		
		DEBUG.P("atTarget="+atTarget);
		if (atTarget == null) return true;
		Attribute atValue = atTarget.member(names.value);
		DEBUG.P("atValue="+atValue);
		DEBUG.P("(!(atValue instanceof Attribute.Array))="+(!(atValue instanceof Attribute.Array)));
		if (!(atValue instanceof Attribute.Array)) return true; // error recovery
		Attribute.Array arr = (Attribute.Array) atValue;
		for (Attribute app : arr.values) {
			DEBUG.P("(!(app instanceof Attribute.Enum))="+(!(app instanceof Attribute.Enum)));
			if (!(app instanceof Attribute.Enum)) return true; // recovery
			Attribute.Enum e = (Attribute.Enum) app;
			
			DEBUG.P("s.kind="+Kinds.toString(s.kind));
			DEBUG.P("s.owner.kind="+Kinds.toString(s.owner.kind));
			DEBUG.P("s.flags()="+Flags.toString(s.flags()));
			DEBUG.P("e.value.name="+e.value.name);
			if (e.value.name == names.TYPE)
			{ if (s.kind == TYP) return true; }
			else if (e.value.name == names.FIELD)
			{ if (s.kind == VAR && s.owner.kind != MTH) return true; }
			else if (e.value.name == names.METHOD)
			{ if (s.kind == MTH && !s.isConstructor()) return true; }
			else if (e.value.name == names.PARAMETER)
			{	
				if (s.kind == VAR &&
				  s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) != 0)
				return true;
			}
			else if (e.value.name == names.CONSTRUCTOR)
			{ if (s.kind == MTH && s.isConstructor()) return true; }
			else if (e.value.name == names.LOCAL_VARIABLE)
			{ if (s.kind == VAR && s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) == 0)
				return true;
			}
			else if (e.value.name == names.ANNOTATION_TYPE)
			{ if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
				return true;
			}
			else if (e.value.name == names.PACKAGE)
			{ if (s.kind == PCK) return true; }
			else
			//��Annotate����Targetʱ�����˴��󣬵���e.value.name�������ϸ���
			return true; // recovery
		}
		return false;
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"annotationApplicable(2)");
		}
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
		DEBUG.P("a.args.tail="+a.args.tail);
		// special case: java.lang.annotation.Target must not have
		// repeated values in its value member
		if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
			a.args.tail == null) //a.args.tail == null��@Target���Ӳ��������
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