    /** Create a new toplevel or member class symbol with given name
     *  and owner and enter in `classes' unless already there.
     */
    public ClassSymbol enterClass(Name name, TypeSymbol owner) {
    	DEBUG.P(this,"enterClass(Name name, TypeSymbol owner)");
    	DEBUG.P("name="+name+" owner="+owner);
    	
        Name flatname = TypeSymbol.formFlatName(name, owner);
        ClassSymbol c = classes.get(flatname);
        
        DEBUG.P("flatname="+flatname+" ClassSymbol c="+c);
        
        if (c == null) {
            c = defineClass(name, owner);
            classes.put(flatname, c);
        } else if ((c.name != name || c.owner != owner) && owner.kind == TYP && c.owner.kind == PCK) {
        	/*
        	���������Ҫ����һ�������ֶ�����һ����(��ӿ�)(Ҳ���ǳ�Ա������)
        	��ִ��Enter.visitTopLevel()����ʱ��ҪΪJCCompilationUnit.packge.members_field
        	���ذ���Ŀ¼�µ��������ļ�������װ����ClassSymbol����members_field�У�����ִ��
        	��Enter.visitClassDef()ʱ��Ա��������Ƶ�����owner��Scope��
        	
        	����:���´���Ƭ��:
        	package my.test;
        	public class Test {
				public static interface MyInterface {
				}
			}
			��ӡ���:
			com.sun.tools.javac.jvm.ClassReader===>enterClass(Name name, TypeSymbol owner)
			-------------------------------------------------------------------------
			name=MyInterface owner=my.test.Test
			flatname=my.test.Test$MyInterface ClassSymbol c=my.test.Test$MyInterface
			c.name=Test$MyInterface c.owner=my.test
			c.fullname(ע�����)=my.test.Test.MyInterface
			com.sun.tools.javac.jvm.ClassReader===>enterClass(Name name, TypeSymbol owner)  END
			-------------------------------------------------------------------------
        	*/
        	
        	
            // reassign fields of classes that might have been loaded with
            // their flat names.
            DEBUG.P("c.name="+c.name+" c.owner="+c.owner);
            c.owner.members().remove(c);
            DEBUG.P("("+name+")��һ����Ա�࣬�Ѵ�("+c.owner+")����Scope��ɾ��");
            c.name = name;
            c.owner = owner;
            c.fullname = ClassSymbol.formFullName(name, owner);
            DEBUG.P("c.fullname(ע�����)="+c.fullname);
            
        }
        //DEBUG.P("c.owner="+c.owner);
        DEBUG.P(0,this,"enterClass(Name name, TypeSymbol owner)");
        return c;
    }

    /**
     * Creates a new toplevel class symbol with given flat name and
     * given class (or source) file.
     *
     * @param flatName a fully qualified binary class name
     * @param classFile the class file or compilation unit defining
     * the class (may be {@code null})
     * @return a newly created class symbol
     * @throws AssertionError if the class symbol already exists
     */
    public ClassSymbol enterClass(Name flatName, JavaFileObject classFile) {
    	DEBUG.P(this,"enterClass(2)");
    	DEBUG.P("flatName="+flatName+" classFile="+classFile);
        ClassSymbol cs = classes.get(flatName);
        if (cs != null) {
            String msg = Log.format("%s: completer = %s; class file = %s; source file = %s",
                                    cs.fullname,
                                    cs.completer,
                                    cs.classfile,
                                    cs.sourcefile);
            throw new AssertionError(msg);
        }
        Name packageName = Convert.packagePart(flatName);
        DEBUG.P("packageName="+packageName);
        /*
        symsδ����Ƿ�Ϊnull,�����С����(�μ�Symtab���е�ע��)
        syms����protected ClassReader(Context context, boolean definitive)��ͨ��
        "syms = Symtab.instance(context);"���г�ʼ���ģ�����ִ��Symtab.instance(context)�Ĺ�
        �����ֻ���Symtab(Context context)�м��ִ�е��������ʱ��û�����
        Symtab(Context context)��Ҳ����symsû�г�ʼ������ִ��syms.unnamedPackageʱ�ͻ�����
        java.lang.NullPointerException
        */
        PackageSymbol owner = packageName.isEmpty()
				? syms.unnamedPackage
				: enterPackage(packageName);
        cs = defineClass(Convert.shortName(flatName), owner);
        cs.classfile = classFile;
        classes.put(flatName, cs);

        DEBUG.P(0,this,"enterClass(2)");
        return cs;
    }

    /** Create a new member or toplevel class symbol with given flat name
     *  and enter in `classes' unless already there.
     */
    public ClassSymbol enterClass(Name flatname) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"enterClass(1)");
		
        ClassSymbol c = classes.get(flatname);
        if(c!=null) DEBUG.P("ClassSymbol("+flatname+")�Ѵ���");
        //DEBUG.P("ClassSymbol c="+(JavaFileObject)null);//�Ǻǣ���һ�μ������﷨(JavaFileObject)null
        /*2008-11-15����:
		��Ϊ��������������:
		1.public ClassSymbol enterClass(Name name, TypeSymbol owner)
		2.public ClassSymbol enterClass(Name flatName, JavaFileObject classFile)
		��������ַ�ʽ����:enterClass(flatname, null)
		�������������:��enterClass�����ò���ȷ
		��Ϊnull�ȿ��Ը���TypeSymbol ownerҲ�ɸ���JavaFileObject classFile
		���Ա���������ת��:(JavaFileObject)null�����߱����������õ��Ƿ���2
		*/
		if (c == null)
            return enterClass(flatname, (JavaFileObject)null);
        else
            return c;
            
        }finally{//�Ҽ��ϵ�
		DEBUG.P(1,this,"enterClass(1)");
		}
    }
