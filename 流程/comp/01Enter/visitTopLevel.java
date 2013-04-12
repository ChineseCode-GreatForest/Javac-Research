    public void visitTopLevel(JCCompilationUnit tree) {
		JavaFileObject prev = log.useSource(tree.sourcefile);
		DEBUG.P(this,"visitTopLevel(1)");
		//��û�н��е�Enter�׶ε�ʱ��JCCompilationUnit��PackageSymbol packge
		//��null����Ҳ˵����:Parser�ĺ����׶ε��������������JCTree�С��������ݡ�
		DEBUG.P("JCCompilationUnit tree.sourcefile="+tree.sourcefile);
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
        DEBUG.P("JCCompilationUnit tree.pid="+tree.pid);

		boolean addEnv = false;
		
		//DEBUG.P("JCCompilationUnit tree.sourcefile.className="+tree.sourcefile.getClass().getName());
		//���һ����:com.sun.tools.javac.util.JavacFileManager$RegularFileObject
		//JavacFileManager.RegularFileObject, JavacFileManager.ZipFileObject��ʵ����
		//JavaFileObject�ӿ�
		
		//���JCCompilationUnit tree.sourcefile���ļ����Ƿ���package-info.java
		boolean isPkgInfo = tree.sourcefile.isNameCompatible("package-info",
									 JavaFileObject.Kind.SOURCE);
		DEBUG.P("isPkgInfo="+isPkgInfo);

		//tree.pid��Դ�ļ����ڰ���ȫ��					     
		if (tree.pid != null) {
				//��ִ����TreeInfo.fullName(tree.pid)�󣬽�����һ�������İ���������
				//�����Name.Table��
				//(ע:���������:my.test,��Name.Table�л�������name:(my),(test)��(my.test)
				//����һ��ֻ�����javac��ִ���ٶ�
				//DEBUG.P(names.myNames());
			tree.packge = reader.enterPackage(TreeInfo.fullName(tree.pid));
			//DEBUG.P(names.myNames());
			DEBUG.P("tree.packageAnnotations="+tree.packageAnnotations);
			if (tree.packageAnnotations.nonEmpty()) {
					if (isPkgInfo) {
						addEnv = true;
					} else {
						//ֻ��package-info.java�����а�ע��
						//�ο�:Parser.compilationUnit()
						log.error(tree.packageAnnotations.head.pos(),
								  "pkg.annotations.sb.in.package-info.java");
					}
			}
		} else {
				//Դ�ļ�δ��������package�����
			tree.packge = syms.unnamedPackage;
		}
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
		DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		
		/*
		complete()��com.sun.tools.javac.code.Symbol����
		tree.packge��com.sun.tools.javac.code.Symbol.PackageSymbol��ʵ������
		com.sun.tools.javac.jvm.ClassReaderʵ����com.sun.tools.javac.code.Symbol.Completer�ӿ�
		����Symbol.complete()��ͨ��Symbol.Completer completer(��ClassReader��enterPackage�����и�ֵ)
		��ӵ���ClassReader��complete(Symbol sym)����
		
		���ù���:com.sun.tools.javac.code.Symbol::complete()==>
				 com.sun.tools.javac.jvm.ClassReader::complete(1)

		��ûִ��complete()ǰ����ִ���������enterPackage�󣬵õ���һ��
		PackageSymbol�������PackageSymbol��Scope members_field��null�ģ�
		ִ��complete()��Ŀ�ľ���Ϊ���ҳ�PackageSymbol����ʾ�İ����е�
		�������ļ���������Щ���ļ�����װ����һ��ClassSymbol����members_field
		*/

		//��Ȼcomplete()�����׳�CompletionFailure��
		//����ΪCompletionFailure��RuntimeException�����࣬
		//������visitTopLevel�˷����п��Բ�����
		tree.packge.complete(); // Find all classes in package.

		//��ԱҲ�п�����δ�����.java�ļ�
		//����ļ���Package-Info1.java��
		//����Ϊ"-"������ClassReader�ķ���fillIn(3)�е�SourceVersion.isIdentifier(simpleName)�������˵���
		//�����ļ�Package-Info1.java��ClassReader�ķ���includeClassFile(2)�б�����tree.packge.package_info�������Ǽ���tree.packge.members_field
		DEBUG.P(3);
		DEBUG.P(tree.packge+"���е����г�Աװ�����(Enter)");
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
        DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		DEBUG.P(3);

        Env<AttrContext> env = topLevelEnv(tree);

		// Save environment of package-info.java file.
		if (isPkgInfo) {
			Env<AttrContext> env0 = typeEnvs.get(tree.packge);
			if (env0 == null) {
				typeEnvs.put(tree.packge, env);
			} else {
				JCCompilationUnit tree0 = env0.toplevel;
				if (!fileManager.isSameFile(tree.sourcefile, tree0.sourcefile)) {
					/* ��ͬʱ���������ڲ�ͬĿ¼��ͬ��package-info.java�ļ�ʱ��
					���������package-info.java�����ݶ�����ͬ�İ��磬:package test.enter;
					��ᷢ��"���棺[package-info] ���ҵ������ test.enter �� package-info.java �ļ�"
					//test\enter\package-info.java
					package test.enter;
					//test\enter\package-info.java
					package test.enter;
					*/
					log.warning(tree.pid != null ? tree.pid.pos()
								: null,
								"pkg-info.already.seen",
								tree.packge);
					if (addEnv || (tree0.packageAnnotations.isEmpty() &&
						   tree.docComments != null &&
						   tree.docComments.get(tree) != null)) {
						typeEnvs.put(tree.packge, env);
					}
				}
			}
		}

		classEnter(tree.defs, env);
        if (addEnv) {//��ע�ʹ�����
            todo.append(env);
        }
		log.useSource(prev);
		result = null;
	
	/*******************���¶��Ǵ�ӡ��Ϣ�����(������;)********************/
        DEBUG.P(2);
        DEBUG.P("***��һ�׶�Enter���***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("����: "+tree.packge);
        DEBUG.P("--------------------------");
        DEBUG.P("tree.packge.members_field: "+tree.packge.members_field);
        DEBUG.P("tree.namedImportScope    : "+tree.namedImportScope);
        DEBUG.P("tree.starImportScope     : "+tree.starImportScope);
        DEBUG.P("");
        
        //ListBuffer<ClassSymbol> uncompleted
        DEBUG.P("�ȴ�������������: "+uncompleted.size());
        DEBUG.P("--------------------------");
        for(ClassSymbol myClassSymbol:uncompleted) {
        	DEBUG.P("����             : "+myClassSymbol);
        	DEBUG.P("members_field    : "+myClassSymbol.members_field);
        	DEBUG.P("flags            : "+Flags.toString(myClassSymbol.flags_field));
        	DEBUG.P("sourcefile       : "+myClassSymbol.sourcefile);
        	DEBUG.P("classfile        : "+myClassSymbol.classfile);
        	DEBUG.P("completer        : "+myClassSymbol.completer);
        	ClassType myClassType=(ClassType)myClassSymbol.type;
        	DEBUG.P("type             : "+myClassType);
        	DEBUG.P("outer_field      : "+myClassType.getEnclosingType());
        	DEBUG.P("supertype_field  : "+myClassType.supertype_field);
        	DEBUG.P("interfaces_field : "+myClassType.interfaces_field);
        	DEBUG.P("typarams_field   : "+myClassType.typarams_field);
        	DEBUG.P("allparams_field  : "+myClassType.allparams_field);
        	DEBUG.P("");
        }
        DEBUG.P("");
        DEBUG.P("Env����: "+typeEnvs.size());
        DEBUG.P("--------------------------");
        for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
        	DEBUG.P(""+myMapEntry);
        DEBUG.P(2);
        
        DEBUG.P("Todo����: "+todo.size());
        DEBUG.P("--------------------------");
        for(List<Env<AttrContext>> l=todo.toList();l.nonEmpty();l=l.tail)
        	DEBUG.P(""+l.head);
        DEBUG.P(2);
        
    	DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
        DEBUG.P(2);
		DEBUG.P(2,this,"visitTopLevel(1)");
	//
    }