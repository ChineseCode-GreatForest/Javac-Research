    /**
     * Insert all files in subdirectory `subdirectory' of `directory' which end
     * in one of the extensions in `extensions' into packageSym.
     */
    private void listDirectory(File directory,
                               String subdirectory,
                               Set<JavaFileObject.Kind> fileKinds,
                               boolean recurse,
                               ListBuffer<JavaFileObject> l) {
        
        DEBUG.P("directory.isFile()="+directory.isFile()+" directory="+directory);
        DEBUG.P("recurse="+recurse+" subdirectory="+subdirectory+" fileKinds="+fileKinds);
        //DEBUG.P("ListBuffer<JavaFileObject>.size()="+l.size());
        //DEBUG.P("archive="+archives.get(directory));
        	                     	
        Archive archive = archives.get(directory);
        
        //��ѹ���ļ�(��jar,zip)�в����Ƿ���subdirectoryĿ¼,�����ļ����ͼ���fileKinds
        //���ҵ����ļ������һ��ZipFileObject����ListBuffer<JavaFileObject>
        //��recurse=tureʱ���ݹ������Ŀ¼
        if (archive != null || directory.isFile()) {
            if (archive == null) {
                try {
                    archive = openArchive(directory);
                } catch (IOException ex) {
                    log.error("error.reading.file",
                       directory, ex.getLocalizedMessage());
                    return;
                }
            }
            if (subdirectory.length() != 0) {
                subdirectory = subdirectory.replace('\\', '/');
                if (!subdirectory.endsWith("/")) subdirectory = subdirectory + "/";
            }
            //DEBUG.P("subdirectory="+subdirectory);
            //DEBUG.P("archiveClassName="+archive.getClass());
            
            List<String> files = archive.getFiles(subdirectory);
            if (files != null) {
                for (String file; !files.isEmpty(); files = files.tail) {
                    file = files.head;
                    if (isValidFile(file, fileKinds)) {
                    	//DEBUG.P("fname="+file);
                        l.append(archive.getFileObject(subdirectory, file));
                    }
                }
            }
            if (recurse) {
                for (String s: archive.getSubdirectories()) {
                    if (s.startsWith(subdirectory) && !s.equals(subdirectory)) {
                        // Because the archive map is a flat list of directories,
                        // the enclosing loop will pick up all child subdirectories.
                        // Therefore, there is no need to recurse deeper.
                        //��˼����˵ZipArchive�е�map�Ѿ��г�����Ŀ¼(������Ŀ¼)��
                        //ֻҪ�������map�е�key���൱�ڲ�������Ŀ¼��
                        listDirectory(directory, s, fileKinds, false, l);
                    }
                }
            }
        } else {
        	//���ļ����ͼ���fileKinds����Ŀ¼directory\subdirectory\�µ������ļ�
        	//���ҵ����ļ������һ��RegularFileObject����ListBuffer<JavaFileObject>
        	//��recurse=tureʱ���ݹ������Ŀ¼
            File d = subdirectory.length() != 0
                ? new File(directory, subdirectory)
                : directory;
            
            //DEBUG.P("File(directory, subdirectory).name="+d);  
            
            //if (!caseMapCheck(d, subdirectory))
            //    return;
			boolean caseMapCheckFlag=caseMapCheck(d, subdirectory);
			DEBUG.P("caseMapCheckFlag="+caseMapCheckFlag);
			if (!caseMapCheckFlag)
                return;

            File[] files = d.listFiles();
			if (files == null) DEBUG.P("files=null");
            else {
                DEBUG.P("files.length="+files.length);
                //DEBUG.P("files="+files);
                for (File direntry : files) {
					String fname = direntry.getName();
					DEBUG.P("direntry="+direntry);
                    DEBUG.P("fname="+fname);
                }
            }

            if (files == null)
                return;

            for (File f: files) {
                String fname = f.getName();
                if (f.isDirectory()) {
                    if (recurse && SourceVersion.isIdentifier(fname)) {
                        listDirectory(directory,
                                      subdirectory + File.separator + fname,
                                      fileKinds,
                                      recurse,
                                      l);
                    }
                } else {
                    if (isValidFile(fname, fileKinds)) {
                    	DEBUG.P("fname="+fname);
                        JavaFileObject fe =
                        new RegularFileObject(fname, new File(d, fname));
                        l.append(fe);
                    }
                }
            }
        }
    }

	//�жϸ����ļ�s����չ���Ƿ��ڸ������ļ����ͼ���fileKinds��
    private boolean isValidFile(String s, Set<JavaFileObject.Kind> fileKinds) {
        int lastDot = s.lastIndexOf(".");
        String extn = (lastDot == -1 ? s : s.substring(lastDot));
        JavaFileObject.Kind kind = getKind(extn);
        return fileKinds.contains(kind);
    }