    /** Include class corresponding to given class file in package,
     *  unless (1) we already have one the same kind (.class or .java), or
     *         (2) we have one of the other kind, and the given class file
     *             is older.
     */
    protected void includeClassFile(PackageSymbol p, JavaFileObject file) {
    	DEBUG.P("");
    	DEBUG.P(this,"includeClassFile(2)");
    	DEBUG.P("PackageSymbol p.flags_field="+p.flags_field+" ("+Flags.toString(p.flags_field)+")");
    	DEBUG.P("p.members_field="+p.members_field);
    	
    	//���PackageSymbol�Ƿ����г�Ա(��ǰ��û��ClassSymbol�ӽ���members_field)
    	//����ֻҪ�Ӱ����г�Ա����ô����Ϊ�Ӱ�������owner�����г�Ա
    	//����ο�Flags���EXISTS�ֶ�˵��
        if ((p.flags_field & EXISTS) == 0)
            for (Symbol q = p; q != null && q.kind == PCK; q = q.owner)
                q.flags_field |= EXISTS;
        JavaFileObject.Kind kind = file.getKind();
        int seen;
        if (kind == JavaFileObject.Kind.CLASS)
            seen = CLASS_SEEN;//CLASS_SEEN��Flags���ж���
        else
            seen = SOURCE_SEEN;
        
        //binaryName����ǰ��fillIn(3)�����ҹ�һ����,����������һ��,
        //�����ʵ��Ľ�һ��,��Ϊ����inferBinaryName�������Ǻ�ʱ���
        String binaryName = fileManager.inferBinaryName(currentLoc, file);
        DEBUG.P("binaryName="+binaryName);
        int lastDot = binaryName.lastIndexOf(".");
        Name classname = names.fromString(binaryName.substring(lastDot + 1));
        DEBUG.P("classname="+classname);
        boolean isPkgInfo = classname == names.package_info;
        ClassSymbol c = isPkgInfo
            ? p.package_info
            : (ClassSymbol) p.members_field.lookup(classname).sym;
        DEBUG.P("ClassSymbol c="+c);
        if (c != null) DEBUG.P("�ڰ�("+p+")��Scope���������ClassSymbol");
        if (c == null) {
            c = enterClass(classname, p);
            if (c.classfile == null) // only update the file if's it's newly created
                c.classfile = file;
            if (isPkgInfo) {
                p.package_info = c;
            } else {
            	DEBUG.P("c="+c+" c.owner="+c.owner+" p="+p);
            	if(c.owner != p) 
            		DEBUG.P("(�ڲ���û��Enter����Scope)");
            	else 
            		DEBUG.P("(��Enter����Scope)");
            	/*
            	Ҳ����˵PackageSymbol��members_field���Ậ���ڲ���
            	������Ϊ��enterClass(classname, p)���ڲ����Ըı�
            	c��owner,����һ���Ǵ���ȥ�Ĳ���PackageSymbol p.
            	
            	���ǻ������,���´���:
            	package my.test;
            	public class Test{
					public class MyInnerClass {
					}
				}
				��ӡ�������:
				c=my.test.Test$MyInnerClass c.owner=my.test p=my.test
				*/
                if (c.owner == p)  // it might be an inner class
                    p.members_field.enter(c);
            }
        //����·�����ҵ�������������ͬ�Ķ���ļ�ʱ��
        //1.����ļ���չ����ͬ����ѡ���ҵ�����һ��
        //2.����ļ���չ����ͬ����javac�м��ϡ�-Xprefer:source��ѡ��ʱ����ѡԴ�ļ�(.java)
        //3.����ļ���չ����ͬ����javac��û�мӡ�-Xprefer:source��ѡ���ѡ����޸Ĺ�����һ��
        
        //(c.flags_field & seen) == 0)��ʾԭ�ȵ�ClassSymbol��������ļ�
        //����չ�������ڵ�file��������ļ�����չ����ͬ
        } else if (c.classfile != null && (c.flags_field & seen) == 0) {
        	DEBUG.P("ClassSymbol c.classfile(��)="+c.classfile);
            // if c.classfile == null, we are currently compiling this class
            // and no further action is necessary.
            // if (c.flags_field & seen) != 0, we have already encountered
            // a file of the same kind; again no further action is necessary.
            if ((c.flags_field & (CLASS_SEEN | SOURCE_SEEN)) != 0)
                c.classfile = preferredFileObject(file, c.classfile);
        }
        c.flags_field |= seen;
        DEBUG.P("ClassSymbol c.classfile="+c.classfile);
        DEBUG.P("ClassSymbol c.flags_field="+c.flags_field+" ("+Flags.toString(c.flags_field)+")");
        DEBUG.P(1,this,"includeClassFile(2)");
    }