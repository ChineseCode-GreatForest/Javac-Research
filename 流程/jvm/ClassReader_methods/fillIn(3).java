    protected Location currentLoc; // FIXME

    private boolean verbosePath = true;

    /** Load directory of package into members scope.
     */
    private void fillIn(PackageSymbol p) throws IOException {
    	DEBUG.P(this,"fillIn(PackageSymbol p)");
    	DEBUG.P("Scope members_field="+p.members_field);
        if (p.members_field == null) p.members_field = new Scope(p);
        String packageName = p.fullname.toString();
        
        //����İ����������Ŀ¼������ļ������ǡ�.class���͡�.java��
        Set<JavaFileObject.Kind> kinds = getPackageFileKinds();
        
        //PLATFORM_CLASS_PATH��javax.tools.StandardLocation�ж���
        //DEBUG.P("fileManager.getClass().getName()="+fileManager.getClass().getName(),true);
        //�����:com.sun.tools.javac.util.JavacFileManager
        
        //��������PLATFORM_CLASS_PATH������packageNameĿ¼�µ�����class�ļ�
        fillIn(p, PLATFORM_CLASS_PATH,
               fileManager.list(PLATFORM_CLASS_PATH,
                                packageName,
                                EnumSet.of(JavaFileObject.Kind.CLASS),
                                false));
        
        DEBUG.P(2);
        DEBUG.P("***��PLATFORM_CLASS_PATH��Enter���ļ��������***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("����: "+packageName);
        DEBUG.P("��Ա: "+p.members_field);
       	DEBUG.P(2);
 
        DEBUG.P("kinds="+kinds);                       
        Set<JavaFileObject.Kind> classKinds = EnumSet.copyOf(kinds);
        DEBUG.P("classKinds1="+classKinds); 
        classKinds.remove(JavaFileObject.Kind.SOURCE);
        DEBUG.P("classKinds2="+classKinds);
        boolean wantClassFiles = !classKinds.isEmpty();

        Set<JavaFileObject.Kind> sourceKinds = EnumSet.copyOf(kinds);
        sourceKinds.remove(JavaFileObject.Kind.CLASS);
        boolean wantSourceFiles = !sourceKinds.isEmpty();

        boolean haveSourcePath = fileManager.hasLocation(SOURCE_PATH);
        
        DEBUG.P("sourceKinds="+sourceKinds);
        DEBUG.P("wantClassFiles="+wantClassFiles);
        DEBUG.P("wantSourceFiles="+wantSourceFiles);
        DEBUG.P("haveSourcePath="+haveSourcePath);
        DEBUG.P("verbose="+verbose);
        DEBUG.P("verbosePath="+verbosePath);

        if (verbose && verbosePath) {
        	//javac��-verboseʱ���[search path for source files:.....]
        	//[search path for class files:...........................]
            if (fileManager instanceof StandardJavaFileManager) {
                StandardJavaFileManager fm = (StandardJavaFileManager)fileManager;
                //����-sourcepathѡ��ʱ����ӡ-sourcepath��ָʾ��·��
                //·����com.sun.tools.javac.util.Paths.computeSourcePath()���
                if (haveSourcePath && wantSourceFiles) {
                    List<File> path = List.nil();
                    for (File file : fm.getLocation(SOURCE_PATH)) {
                    	DEBUG.P("file="+file);
                        path = path.prepend(file);
                    }
                    printVerbose("sourcepath", path.reverse().toString());
                //û��-sourcepathѡ��ʱ,Ĭ�ϴ�ӡ��·���ϵ���Ϣ
                //·����com.sun.tools.javac.util.Paths.computeUserClassPath()���
                } else if (wantSourceFiles) {
                    List<File> path = List.nil();
                    for (File file : fm.getLocation(CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    printVerbose("sourcepath", path.reverse().toString());
                }
                if (wantClassFiles) {
                    List<File> path = List.nil();
                    //һ����jre\lib��jre\lib\extĿ¼�µ�.jar�ļ�
                    //·����com.sun.tools.javac.util.Paths.computeBootClassPath()���
                    for (File file : fm.getLocation(PLATFORM_CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    
                    //·����com.sun.tools.javac.util.Paths.computeUserClassPath()���
                    for (File file : fm.getLocation(CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    //������������·������һ�����
                    printVerbose("classpath",  path.reverse().toString());
                }
            }
        }
        
        //��ûָ��-sourcepathʱ��Ĭ����CLASS_PATH������packageNameĿ¼�µ�����class��java�ļ�
        if (wantSourceFiles && !haveSourcePath) {
            fillIn(p, CLASS_PATH,
                   fileManager.list(CLASS_PATH,
                                    packageName,
                                    kinds,
                                    false));
        } else {
        	//��CLASS_PATH������packageNameĿ¼�µ�����class�ļ�
            if (wantClassFiles)
                fillIn(p, CLASS_PATH,
                       fileManager.list(CLASS_PATH,
                                        packageName,
                                        classKinds,
                                        false));
            //��SOURCE_PATH������packageNameĿ¼�µ�����java�ļ�
            if (wantSourceFiles)
                fillIn(p, SOURCE_PATH,
                       fileManager.list(SOURCE_PATH,
                                        packageName,
                                        sourceKinds,
                                        false));
        }
        verbosePath = false;
        
        //��ԱҲ�п�����δ�����.java�ļ�
        DEBUG.P(2);
        DEBUG.P("***���г�ԱEnter�������***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("����: "+packageName);
        DEBUG.P("��Ա: "+p.members_field);
        DEBUG.P(2,this,"fillIn(PackageSymbol p)"); 
    }
    // where
        private void fillIn(PackageSymbol p,
                            Location location,
                            Iterable<JavaFileObject> files)
        {
            currentLoc = location;
            DEBUG.P(this,"fillIn(3)");
           
            for (JavaFileObject fo : files) {
            	DEBUG.P("fileKind="+fo.getKind()+" fileName="+fo);
                switch (fo.getKind()) {
                case CLASS:
                case SOURCE: {
                    // TODO pass binaryName to includeClassFile
                    String binaryName = fileManager.inferBinaryName(currentLoc, fo);
                    String simpleName = binaryName.substring(binaryName.lastIndexOf(".") + 1);
					DEBUG.P("fo="+fo);
                    DEBUG.P("binaryName="+binaryName);
					DEBUG.P("simpleName="+simpleName);
                    if (SourceVersion.isIdentifier(simpleName) ||
                        simpleName.equals("package-info"))
                        includeClassFile(p, fo);
                    break;
                }
                default:
                    extraFileActions(p, fo);//һ���շ���
                }
                DEBUG.P(1);
            }
            DEBUG.P(2,this,"fillIn(3)");
        }