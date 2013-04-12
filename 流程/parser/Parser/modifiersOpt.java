    /** ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"(����һ��@�ǲ��е�)
     *           | "@" Annotation
     */
    JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }
    JCModifiers modifiersOpt(JCModifiers partial) {
    	DEBUG.P(this,"modifiersOpt(1)");	
    	
    	//flags�Ǹ���Modifierͨ����λ������(|)���õ�
    	//��com.sun.tools.javac.code.Flags������һλ(bit)��ʾһ��Modifier
    	//��flags��long���ͣ����Կɱ�ʾ64����ͬ��Modifier
    	//��flags=0x01ʱ��ʾFlags.PUBLIC,��flags=0x03ʱ��ʾFlags.PUBLIC��Flags.PRIVATE
    	//��flags����Flags.toString(long flags)�����Ϳ���֪��flags�����ĸ�(��Щ)Modifier
        long flags = (partial == null) ? 0 : partial.flags;

        //��Scanner��Javadoc��ɨ�赽��@deprecatedʱS.deprecatedFlag()����true
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        DEBUG.P("(whileǰ) flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        
        ListBuffer<JCAnnotation> annotations = new ListBuffer<JCAnnotation>();
        if (partial != null) annotations.appendList(partial.annotations);
        int pos = S.pos();
        int lastPos = Position.NOPOS;
    loop:
        while (true) {
            // <editor-fold defaultstate="collapsed">
            long flag;
			/*
			��Flags���ж�����12��Standard Java flags��
			���������switch���������INTERFACE��
			������ΪINTERFACE(����ENUM)���治�������������η��ˣ�
			��S.token()==INTERFACEʱ���˳�whileѭ���������׷��INTERFACE���η���־
			*/
            switch (S.token()) {
	            case PRIVATE     : flag = Flags.PRIVATE; break;
	            case PROTECTED   : flag = Flags.PROTECTED; break;
	            case PUBLIC      : flag = Flags.PUBLIC; break;
	            case STATIC      : flag = Flags.STATIC; break;
	            case TRANSIENT   : flag = Flags.TRANSIENT; break;
	            case FINAL       : flag = Flags.FINAL; break;
	            case ABSTRACT    : flag = Flags.ABSTRACT; break;
	            case NATIVE      : flag = Flags.NATIVE; break;
	            case VOLATILE    : flag = Flags.VOLATILE; break;
	            case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
	            case STRICTFP    : flag = Flags.STRICTFP; break;
	            case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
	            default: break loop;
            }
            //���η��ظ�,������ʾ��Ϣ��com\sun\tools\javac\resources\compiler.properties����
            if ((flags & flag) != 0) log.error(S.pos(), "repeated.modifier");
            //��������û���жϳ�������У�ֻ����Log�м�¼�´���������
            //DEBUG.P("Log.nerrors="+log.nerrors);
            
            lastPos = S.pos();
            S.nextToken();
           
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();//��鵱ǰ��-source�汾�Ƿ�֧��ע��
                
                //�ǡ�@interface���﷨ע��ʶ��(@interface����ע�����͵Ķ���)
                //��@interface���﷨��com.sun.tools.javac.util.Version����������������
                //JDK1.6���й���Annotations���ĵ���technotes/guides/language/annotations.html
                if (S.token() != INTERFACE) {
					//lastPos��@�Ŀ�ʼλ��
                    JCAnnotation ann = annotation(lastPos);
					DEBUG.P("pos="+pos);
					DEBUG.P("ann.pos="+ann.pos);
                    // if first modifier is an annotation, set pos to annotation's.
                    if (flags == 0 && annotations.isEmpty())
                        pos = ann.pos;
                    annotations.append(ann);
                    lastPos = ann.pos;

                    //ע������,�������checkNoMods(mods.flags)��Ӱ��
                    flag = 0;
                }
            }
            flags |= flag;
            // </editor-fold>
        }
        switch (S.token()) {
	        case ENUM: flags |= Flags.ENUM; break;
	        case INTERFACE: flags |= Flags.INTERFACE; break;
	        default: break;
        }
        
        DEBUG.P("(while��)  flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        DEBUG.P("JCAnnotation count="+annotations.size());

        /* A modifiers tree with no modifier tokens or annotations
         * has no text position. */
        if (flags == 0 && annotations.isEmpty())
            pos = Position.NOPOS;
            
        JCModifiers mods = F.at(pos).Modifiers(flags, annotations.toList());
        
        if (pos != Position.NOPOS)
            storeEnd(mods, S.prevEndPos());//storeEnd()ֻ��һ���շ���,����EndPosParser����д
            
        DEBUG.P(1,this,"modifiersOpt(1)");	
        return mods;
    }