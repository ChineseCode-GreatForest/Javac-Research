    /** Make a package, given its fully qualified name.
     */

	/*
	��packageName=java.lang,�״ε���enterPackage()ʱ�����:
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=java.lang
	Convert.shortName(fullname)=lang
	Convert.packagePart(fullname)=java
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=java
	Convert.shortName(fullname)=java
	Convert.packagePart(fullname)=
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	*/
    public PackageSymbol enterPackage(Name fullname) {
    	DEBUG.P(this,"enterPackage(1)");
		DEBUG.P("fullname="+fullname);
		
		//packages��һ��Map
        PackageSymbol p = packages.get(fullname);
        if (p == null) {
        	//����:��assert���������Ϊ��ʱִ��assert�����������䣬���򱨴��˳���
        	//p == null��fullnameҲ��һ���մ�(fullname=names.empty)��������������ͬʱ������
        	//��Ϊ�մ�(fullname=names.empty)�ڳ�ʼ��Systab��ʱ�Ѹ�PackageSymbol rootPackage��Ӧ
        	//��PackageSymbol rootPackage�ѷ���packages
            assert !fullname.isEmpty() : "rootPackage missing!";
            
            DEBUG.P("Convert.shortName(fullname)="+Convert.shortName(fullname));
            DEBUG.P("Convert.packagePart(fullname)="+Convert.packagePart(fullname));
            
            /*
			���fullname��û���ֹ���һ���ݹ���õ���fullname��names.empty(Table.empty)ʱ����,
			rootPackage��fullname����names.empty,��init()ʱ�Ѽӽ�packages.
			����,PackageSymbol���ǰ�����������ݹ�Ƕ�׵�,�ڲ��ֶ�Symbol owner�������������
			��enterPackage(Convert.packagePart(fullname))
			
			����:����my.test��Ƕ�׸�ʽ����:
			PackageSymbol {
				Name name="test";
				Symbol owner=new PackageSymbol {
					Name name="my";
					Symbol owner=rootPackage = new PackageSymbol(names.empty, null);
				}
			}
			*/
            p = new PackageSymbol(
                Convert.shortName(fullname),
                enterPackage(Convert.packagePart(fullname)));
            //��һ����Ϊ���Ժ����Symbol.complete()����ӵ���ClassReader��complete(Symbol sym)
            p.completer = this;
            packages.put(fullname, p);
        }
        DEBUG.P(0,this,"enterPackage(1)");
        return p;
    }

    /** Make a package, given its unqualified name and enclosing package.
     */
    public PackageSymbol enterPackage(Name name, PackageSymbol owner) {
        return enterPackage(TypeSymbol.formFullName(name, owner));
    }