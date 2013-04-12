    public void visitTopLevel(JCCompilationUnit tree) {
    	try {
            // <editor-fold defaultstate="collapsed">
    	DEBUG.P(this,"visitTopLevel(1)");
    	DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
    	DEBUG.P("tree.packge.name="+tree.packge.name);
    	DEBUG.P("tree.packge.fullname="+tree.packge.fullname);
    	DEBUG.P("tree.packge.owner.name="+tree.packge.owner.name);
    	DEBUG.P("tree.packge.owner.fullname="+tree.packge.owner.getQualifiedName());
    	
		DEBUG.P(2);
    	DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
    	//��tree.starImportScope.nelems=0ʱtree.starImportScope.elems==null
        if (tree.starImportScope.elems != null) {
        	/*
        	����ͬһ�ļ��ڶ����˶����ʱ�ͻ�����������
        	���´�����ʾ:
        	
        	package my.test;
			public class Test {}
        	class MyTheSamePackageClass {}
        	
        	*/
        	DEBUG.P("starImportScope �Ѵ���");
        	
            // we must have already processed this toplevel
            return;
        }

		DEBUG.P("checkClash="+checkClash);
		DEBUG.P("tree.pid="+tree.pid);

        // check that no class exists with same fully qualified name as
        // toplevel package
        if (checkClash && tree.pid != null) {
            Symbol p = tree.packge;
			while (p.owner != syms.rootPackage) {
                p.owner.complete(); // enter all class members of p            
                /*
                ����:���������my.test,Ȼ����myĿ¼���и�test.java�ļ�
                ��ô�ͻ���ִ�����ʾ:
                package my.test clashes with class of same name
                package my.test;
                ^
                ԭ��:
                �����·����: F:\javac\bin\mybin��
                test.java�ļ�λ��: F:\javac\bin\mybin\my\test.java��
                p��: my.test
                p.owner����: my
                ��ô���صİ�����: my
                test.java�ļ����ݲ��ùܣ�ʲô�����ԣ�

                �����õ�com.sun.tools.javac.util.JavacFileManager===>inferBinaryName(2)ʱ
                ��������my�ض�F:\javac\bin\mybin\my\test.java�õ�my\test.java
                ��Ŀ¼�ָ����滻��".",ȥ����չ�����õ�һ����ȫ����"my.test"��
                �������İ���Ҳ��"my.test"�ͻ������ͻ

				���ǣ������F:\javac\bin\mybinĿ¼���и����ļ�my.java�ǲ����ͻ�ģ�
				��Ϊ��p��Ϊ��my"ʱ��p.owner�����syms.rootPackage��whileѭ�������ˡ�
				�����ѭ�������ĳ�(p.owner != null)���Ϳ��Լ���my.java�����my��ͻ
                */
                if (syms.classes.get(p.getQualifiedName()) != null) {
                    log.error(tree.pos,
                              "pkg.clashes.with.class.of.same.name",
                              p);
                }
                p = p.owner;
                
                DEBUG.P("p.name="+p.name);
                DEBUG.P("p.fullname="+p.getQualifiedName());
                DEBUG.P("p.owner.name="+p.owner.name);
                DEBUG.P("p.owner.fullname="+p.owner.getQualifiedName());
            }
        }
        // </editor-fold>

        // process package annotations
		//����package-info.javaʱ�ܲ���tree.packageAnnotations!=null
        annotateLater(tree.packageAnnotations, env, tree.packge);
        
        // Import-on-demand java.lang.
        importAll(tree.pos, reader.enterPackage(names.java_lang), env);

		DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
		DEBUG.P("env="+env);

        // Process all import clauses.
        memberEnter(tree.defs, env);

		//DEBUG.P("stop",true);
        
    	}finally{
    	DEBUG.P(0,this,"visitTopLevel(1)");
    	}
    }