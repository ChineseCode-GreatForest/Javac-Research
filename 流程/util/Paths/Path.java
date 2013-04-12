    private class Path extends LinkedHashSet<File> {
		private static final long serialVersionUID = 0;

		private boolean expandJarClassPaths = false;
        private Set<File> canonicalValues = new HashSet<File>();

		public Path expandJarClassPaths(boolean x) {
			expandJarClassPaths = x;
			return this;
		}

		/** What to use when path element is the empty string */
		private String emptyPathDefault = null;

		public Path emptyPathDefault(String x) {
			emptyPathDefault = x;
			return this;
		}

		public Path() { super(); }

		public Path addDirectories(String dirs, boolean warn) {
			DEBUG.P(this,"addDirectories(2)");
			DEBUG.P("warn="+warn+" dirs="+dirs);
			
			if (dirs != null)
				for (String dir : new PathIterator(dirs))
					addDirectory(dir, warn);
			
			DEBUG.P(1,this,"addDirectories(2)");
			return this;
		}

		public Path addDirectories(String dirs) {
			return addDirectories(dirs, warn);
		}
	
		//�Ӹ���Ŀ¼�²����ļ�ʱ��ֻ����չ��Ϊjar��zip���ļ�
		private void addDirectory(String dir, boolean warn) {
            try {//�Ҽ��ϵ�
            DEBUG.P(this,"addDirectory(2)");
            DEBUG.P("warn="+warn+" dir="+dir);
            DEBUG.P("isDirectory()="+new File(dir).isDirectory());
		
			if (! new File(dir).isDirectory()) {
				//�������System.getProperty("java.endorsed.dirs")���ֱ����������Ŀ¼
				//Ҫ�ǲ����ڵĻ�����Ϊ�ڵ���addDirectoryʱ��warn���false�ˣ����Բ��ᾯ�档
				if (warn)
					log.warning("dir.path.element.not.found", dir);
				return;
			}

            File[] files = new File(dir).listFiles();//�г�dirĿ¼�µ��ļ���Ŀ¼(û�еݹ���Ŀ¼)
            
            if (files == null) DEBUG.P("files=null");
            else {
                DEBUG.P("files.length="+files.length);
                //DEBUG.P("files="+files);
                for (File direntry : files) {
                    DEBUG.P("[isArchive="+isArchive(direntry)+"]direntry="+direntry);
                }
            }
            
            if (files == null)
                return;
            
			for (File direntry : files) {
                if (isArchive(direntry)) {
                    DEBUG.P("direntry="+direntry);
                    addFile(direntry, warn);
                }
			}
	    
			} finally {
            DEBUG.P(0,this,"addDirectory(2)");
			}
		}

		public Path addFiles(String files, boolean warn) {
            DEBUG.P(this,"addFiles(2)");
            DEBUG.P("warn="+warn+" files="+files);
            
			if (files != null)
			for (String file : new PathIterator(files, emptyPathDefault)) {
				//DEBUG.P("fileName="+file);
				addFile(file, warn);
			}
                
            DEBUG.P(1,this,"addFiles(2)");
			return this;
		}

		public Path addFiles(String files) {
			return addFiles(files, warn);
		}
		
		public Path addFile(String file, boolean warn) {
			addFile(new File(file), warn);
			return this;
		}
        
        //����file���Դ���һ���ļ�Ҳ�ɴ���һ��Ŀ¼
		public void addFile(File file, boolean warn) {
            try {//�Ҽ��ϵ�
            DEBUG.P(this,"addFile(2)");
            DEBUG.P("warn="+warn+" file="+file);
		
		
            File canonFile;
            try {
                //�淶�����ļ�(һ���ǰ�������·�����ļ�)
                canonFile = file.getCanonicalFile();
            } catch (IOException e) {
                canonFile = file;
            }
            DEBUG.P("canonFile="+canonFile);
        
        
            //contains(file)����??? ��LinkedHashSet<File>(Path�̳���LinkedHashSet<File>)
			if (contains(file) || canonicalValues.contains(canonFile)) {
                /* Discard duplicates and avoid infinite recursion */

                DEBUG.P("�ļ��Ѵ���,����");
                return;
			}
	    
			DEBUG.P("file.exists()="+file.exists());
			DEBUG.P("file.isFile()="+file.isFile());
			DEBUG.P("file.isArchive()="+isArchive(file));
			DEBUG.P("expandJarClassPaths="+expandJarClassPaths);
	    
            /*
            �����У�javac -Xlint:path -Xbootclasspath/p:srcs:JarTest:args.txt:classes
             * ����srcs��һ�������ڵ�Ŀ¼��JarTest���ɡ�JarTest.jar��ɾ����չ����.jar����õ���
             * ʵ�ʴ��ڵ�jar�ļ���args.txtҲ��һ�����ڵ��ı��ļ������Ӧ���¾���:
			���棺[path] �����·��Ԫ�� "srcs"���޴��ļ���Ŀ¼
            ���棺[path] ���¹鵵�ļ������������չ��: JarTest
            ���棺[path] ����·���д���������ļ�: args.txt
            */

            if (! file.exists()) {
                /* No such file or directory exists */
                if (warn)
                    log.warning("path.element.not.found", file);	
			} else if (file.isFile()) {
                /* File is an ordinary file. */ 
                if (!isArchive(file)) {
                    /* Not a recognized extension; open it to see if
                     it looks like a valid zip file. */
                    try {
                        ZipFile z = new ZipFile(file);
                        z.close();
                        if (warn)
                            log.warning("unexpected.archive.file", file);
                    } catch (IOException e) {
                        // FIXME: include e.getLocalizedMessage in warning
                        if (warn)
                            log.warning("invalid.archive.file", file);
                        return;
                    }
                }
			}
        
			/* Now what we have left is either a directory or a file name
			   confirming to archive naming convention */
			   
			//���ļ���Ŀ¼������ʱ�����߻���ͬ�������ӵ�HashSet<File>
			super.add(file);//���� java.util.HashSet �̳еķ���
				canonicalValues.add(canonFile);

				//�Ƿ�չ��ѹ���ļ�(��jar�ļ�)
			if (expandJarClassPaths && file.exists() && file.isFile())
                addJarClassPath(file, warn);

            } finally {
                DEBUG.P(0,this,"addFile(2)");
            }
		}

		// Adds referenced classpath elements from a jar's Class-Path
		// Manifest entry.  In some future release, we may want to
		// update this code to recognize URLs rather than simple
		// filenames, but if we do, we should redo all path-related code.
		private void addJarClassPath(File jarFile, boolean warn) {
            try {
            DEBUG.P(this,"addJarClassPath(2)");
            DEBUG.P("warn="+warn+" jarFile="+jarFile);
            
			try {
				String jarParent = jarFile.getParent();
				
				DEBUG.P("jarParent="+jarParent);
				
				JarFile jar = new JarFile(jarFile);

				try {
					Manifest man = jar.getManifest();
							DEBUG.P("man="+man);
					if (man == null) return;

					Attributes attr = man.getMainAttributes();
							DEBUG.P("attr="+attr);
					if (attr == null) return;
					
					//��ָ��java.util.jar.Attributes.Name
					String path = attr.getValue(Attributes.Name.CLASS_PATH);
					DEBUG.P("Attributes.Name.CLASS_PATH="+path);
					//��System.getProperty("sun.boot.class.path")�������jar�ļ�û��һ����CLASS_PATH
					if (path == null) return;

					for (StringTokenizer st = new StringTokenizer(path);
					 st.hasMoreTokens();) {
					String elt = st.nextToken();
					File f = (jarParent == null ? new File(elt) : new File(jarParent, elt));
					addFile(f, warn);
					}
				} finally {
					jar.close();
				}
			} catch (IOException e) {
				log.error("error.reading.file", jarFile, e.getLocalizedMessage());
			}
            
            
            } finally {
			DEBUG.P(0,this,"addJarClassPath(2)");
            }
		}
    }