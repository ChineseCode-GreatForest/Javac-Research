    //���������ķ���ʾ�ò���׼ȷ
    /** CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     */
    //�����ע����LL(1)�ķ����ܵ�ȫò, ˵��CompilationUnit =��Ҳ����,
    //����ͬ����һ��û���κ����ݵ�Դ�ļ�Ҳ���ᱨ��һ��
    public JCTree.JCCompilationUnit compilationUnit() {
    	DEBUG.P(this,"compilationUnit() ��ʽ��ʼ�﷨����......");
    	DEBUG.P("startPos="+S.pos());
    	DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("errorEndPos="+errorEndPos);
        DEBUG.P("startToken="+S.token());
        
        int pos = S.pos();
        JCExpression pid = null;//��Ӧ�ķ��е�Qualident
        //��ǰtoken��Ӧ��javadoc(��DocCommentScanner.processComment(1))
        String dc = S.docComment();
        DEBUG.P("dc="+dc);

		//��Ӧ�ķ��е�{ "@" Annotation }�������ǰ�ע�ͣ�
		//Ҳ�����ǵ�һ��������������η�
        JCModifiers mods = null;
        
        List<JCAnnotation> packageAnnotations = List.nil();
        
        if (S.token() == MONKEYS_AT)
            mods = modifiersOpt();
        /*
        ֻ����package-info.java�ļ��в����а�ע��(��û���ر�ָ��������£���ע�͡�ָ����Annotation)
        ������д�����ʾ���������ע��Ӧ���ļ� package-info.java �С�
        ��Ӧcompiler.properties�е�"pkg.annotations.sb.in.package-info.java"
        �������﷨�����׶μ�飬������com.sun.tools.javac.comp.Enter�м��
        */
        if (S.token() == PACKAGE) {
            //����ڡ�package��ǰ��JavaDoc,��������@deprecated��
            //���Ǻ���û��@Annotation������modifiers���ǺϷ��ġ�
            if (mods != null) {
            	/*
            	����Ƿ�����ʹ�����η�
            	���package-info.java�ļ���Դ������������:
            	@Deprecated public
                package my.test;

                �ͻᱨ��:
                bin\mysrc\my\test\package-info.java:2: �˴�������ʹ�����η� public
                package my.test;
                ^
                1 ����				
                */
                checkNoMods(mods.flags);
                packageAnnotations = mods.annotations;
                mods = null;
            }
            S.nextToken();
            pid = qualident();
            accept(SEMI);
        }
        //defs�д�Ÿ�import���������(class,interface��)������ص�JTree
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
       	boolean checkForImports = true;
        while (S.token() != EOF) {
            DEBUG.P("S.pos()="+S.pos()+"  errorEndPos="+errorEndPos);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (S.token() == EOF)
                    break;
            }
            
            //�����ע��Ӧ���ļ� package-info.java ��,��package-info.java��û��import�ģ�
            //��package-info.java�ļ������а�ע�ͣ�����mods==null(???)
            //(�������ʺŵ�ע�ͱ���Ŀǰ��δ��ȫ������)
			//��Ϊ��һ��������֮ǰ����û��import����ʱ��Ϊ�ǵ�һ�ν���whileѭ��
			//checkForImportsΪtrue������mods���ܲ�Ϊnull(�纬��public��)
            if (checkForImports && mods == null && S.token() == IMPORT) {
                defs.append(importDeclaration());
            } else {
				//��û��ָ��package��import���ʱ��������������֮ǰ����@��
				//�磺@MyAnnotation public ClassA {}����mods!=null
                JCTree def = typeDeclaration(mods);
                
                //��JCExpressionStatement��JCErroneous����װ������
                if (def instanceof JCExpressionStatement)
                    def = ((JCExpressionStatement)def).expr;
                defs.append(def);

				//���ﱣ֤����������֮������import���
                if (def instanceof JCClassDecl)
                    checkForImports = false;
				//���������������������η���
				//������ͬһ�ļ��������������������Ϊnull��
				//��ΪtypeDeclaration(mods)ʱ������modifiersOpt(mods)
                mods = null;
            }
        }
        //F.at(pos)���pos����int pos = S.pos();ʱ��pos,һֱû��
        JCTree.JCCompilationUnit toplevel = F.at(pos).TopLevel(packageAnnotations, pid, defs.toList());
        attach(toplevel, dc);

		DEBUG.P("defs.elems.isEmpty()="+defs.elems.isEmpty());
        if (defs.elems.isEmpty())
            storeEnd(toplevel, S.prevEndPos());
        if (keepDocComments) toplevel.docComments = docComments;
        
        //���е�����﷨������ɣ�������һ�ó����﷨��
		//DEBUG.P("toplevel="+toplevel);
		DEBUG.P("toplevel.startPos="+getStartPos(toplevel));
		DEBUG.P("toplevel.endPos  ="+getEndPos(toplevel));
        DEBUG.P(3,this,"compilationUnit()");
        //DEBUG.P("Parser stop",true);
        return toplevel;
    }