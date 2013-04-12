    /** Main method: attribute class definition associated with given class symbol.
     *  reporting completion failures at the given position.
     *  @param pos The source position at which completion errors are to be
     *             reported.
     *  @param c   The class symbol whose definition will be attributed.
     */
    public void attribClass(DiagnosticPosition pos, ClassSymbol c) {
    	DEBUG.P(5);DEBUG.P(this,"attribClass(2)");
        try {
            annotate.flush();
            attribClass(c);
        } catch (CompletionFailure ex) {
            chk.completionError(pos, ex);
        }
        DEBUG.P(2,this,"attribClass(2)");
    }

    /** Attribute class definition associated with given class symbol.
     *  @param c   The class symbol whose definition will be attributed.
     */
    void attribClass(ClassSymbol c) throws CompletionFailure {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"attribClass(1)");
    	DEBUG.P("ClassSymbol c="+c);
    	DEBUG.P("c.type="+c.type);
    	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
    	DEBUG.P("c.type.supertype="+((ClassType)c.type).supertype_field);
        
        //����package-info.javaʱ�д�java.lang.NullPointerException
    	//DEBUG.P("c.type.supertype.tag="+TypeTags.toString((((ClassType)c.type).supertype_field).tag));
    	
    	
    	
        if (c.type.tag == ERROR) return;

        // Check for cycles in the inheritance graph, which can arise from
        // ill-formed class files.
        chk.checkNonCyclic(null, c.type);
        //�����(��ӿ�)�Ƿ��Լ��̳�(��ʵ��)�Լ����Ƿ�˴�֮�以��̳�(��ʵ��)
        //��Test4 extends Test4(�Լ��̳��Լ�)
        //��Test4 extends Test5��Test5 extends Test4(�˴�֮�以��̳�)
        //��:public class Test4 extends Test4
        //����:cyclic inheritance involving my.test.Test4


        Type st = types.supertype(c.type);
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        DEBUG.P("c.supertype="+st);
        DEBUG.P("c.supertype.tag="+TypeTags.toString(st.tag));
        DEBUG.P("c.owner="+c.owner);
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        if(c.owner.type!=null) DEBUG.P("c.owner.type.tag="+TypeTags.toString(c.owner.type.tag));
        
        
        //c.flags_field������Flags.COMPOUNDʱִ��
        if ((c.flags_field & Flags.COMPOUND) == 0) {
        	DEBUG.P("c.flags_field������Flags.COMPOUND");
            // First, attribute superclass.
            if (st.tag == CLASS)
                attribClass((ClassSymbol)st.tsym);

            // Next attribute owner, if it is a class.
            if (c.owner.kind == TYP && c.owner.type.tag == CLASS)
                attribClass((ClassSymbol)c.owner);
        }
        
        DEBUG.P("��ɶԣ�"+c+" ��superclass��owner��attribute");
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        // The previous operations might have attributed the current class
        // if there was a cycle. So we test first whether the class is still
        // UNATTRIBUTED.
        if ((c.flags_field & UNATTRIBUTED) != 0) {
        	//�����������ã���Ϊ�����c��������attribClass��
        	//��c.flags_field�о�û��UNATTRIBUTED�����־�ˣ�������
        	//��ĳ�����cʱ���ڵ���Check.checkNonCyclic�������ѭ��ʱ��
        	//�Ϳ��԰�ACYCLIC��־�ӽ�c.flags_field�С�
            c.flags_field &= ~UNATTRIBUTED;

            // Get environment current at the point of class definition.
            Env<AttrContext> env = enter.typeEnvs.get(c);
            
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            DEBUG.P("env="+env);

            // The info.lint field in the envs stored in enter.typeEnvs is deliberately uninitialized,
            // because the annotations were not available at the time the env was created. Therefore,
            // we look up the environment chain for the first enclosing environment for which the
            // lint value is set. Typically, this is the parent env, but might be further if there
            // are any envs created as a result of TypeParameter nodes.
            Env<AttrContext> lintEnv = env;
            while (lintEnv.info.lint == null)
                lintEnv = lintEnv.next;
                
            DEBUG.P("lintEnv="+lintEnv);
            // Having found the enclosing lint value, we can initialize the lint value for this class
            env.info.lint = lintEnv.info.lint.augment(c.attributes_field, c.flags());
            
            DEBUG.P("env.info.lint="+env.info.lint);

            Lint prevLint = chk.setLint(env.info.lint);
            JavaFileObject prev = log.useSource(c.sourcefile);

            try {
            	
            	DEBUG.P("");
            	DEBUG.P("st.tsym="+st.tsym);
            	if (st.tsym != null) 
            		DEBUG.P("st.tsym.flags_field="+Flags.toString(st.tsym.flags_field));
            	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            
                // java.lang.Enum may not be subclassed by a non-enum
                if (st.tsym == syms.enumSym &&
                    ((c.flags_field & (Flags.ENUM|Flags.COMPOUND)) == 0))
                    /*����:
                    F:\javac\bin\mysrc\my\test\TestOhter.java:2: ���޷�ֱ�Ӽ̳� java.lang.Enum
					public class TestOhter<TestOhterS,TestOhterT> extends Enum {
					       ^
					1 ����
					*/
                    log.error(env.tree.pos(), "enum.no.subclassing");

                // Enums may not be extended by source-level classes
                //ע:���((c.flags_field & Flags.ENUM) == 0)Ϊtrue����ô
                //target.compilerBootstrap(c)����Ϊfasle�ģ�Ҳ����
                //!target.compilerBootstrap(c)����Ϊtrue���������Ƕ�����ж�
                if (st.tsym != null &&
                    ((st.tsym.flags_field & Flags.ENUM) != 0) &&
                    ((c.flags_field & Flags.ENUM) == 0) &&
                    !target.compilerBootstrap(c)) {
                    
                    /*����:
                    Դ����:
                    package my.test.myenum;
					public class EnumTest extends MyEnum {}
					enum MyEnum {}
					
					������ʾ:
					bin\mysrc\my\test\myenum\EnumTest.java:3: �޷������� my.test.myenum.MyEnum ���м̳�
					public class EnumTest extends MyEnum {}
					                              ^
					bin\mysrc\my\test\myenum\EnumTest.java:3: ö�����Ͳ��ɼ̳�
					public class EnumTest extends MyEnum {}
					       ^
					2 ����
					*/
                    log.error(env.tree.pos(), "enum.types.not.extensible");
                }
                DEBUG.P(2);
                attribClassBody(env, c);

                chk.checkDeprecatedAnnotation(env.tree.pos(), c);
            } finally {
                log.useSource(prev);
                chk.setLint(prevLint);
                
            }
        }
        
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P("�����Դ�������Է���: "+c);
        DEBUG.P(1,this,"attribClass(1)");
    	}
    }