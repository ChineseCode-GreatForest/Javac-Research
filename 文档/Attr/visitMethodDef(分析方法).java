ע��:��������� ����:ʵ����������̬���������캯��

����һ������ʱ��������ǰ����� @Deprecated ��������µ�JAVADOC:
---------------------
	/**
     * @deprecated
     */
---------------------
�����ַ�ʽ������ʹ�÷�����flags_field(���η���־�ֶ�)��DEPRECATED(��Flags���ж���)


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.1

Lint lint = env.info.lint.augment(m.attributes_field, m.flags());
------------------------------------------
class VisitMethodDefTest {

	@SuppressWarnings({"fallthrough","unchecked"})
	@Deprecated
	VisitMethodDefTest() {}
}
------------------------------------------
��-Xlint
env.info.lint=Lint:[values(11)[CAST, DEPRECATION, DEP_ANN, DIVZERO, EMPTY, FALLTHROUGH, FINALLY, OVERRIDES, PATH, SERIAL, UNCHECKED] suppressedValues(0)[]]
lint=Lint:[values(8)[CAST, DEP_ANN, DIVZERO, EMPTY, FINALLY, OVERRIDES, PATH, SERIAL] suppressedValues(3)[DEPRECATION, FALLTHROUGH, UNCHECKED]]


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.2

chk.checkDeprecatedAnnotation(tree.pos(), m);
------------------------------------------
class VisitMethodDefTest {
	/**
     * @deprecated
     */
	VisitMethodDefTest() {}
}
------------------------------------------
��-Xlint  key=missing.deprecated.annotation
���棺[dep-ann] δʹ�� @Deprecated ���ѹ�ʱ����Ŀ����ע��
        VisitMethodDefTest() {}
        ^
1 ����


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.3 �������ͱ�����compound bounds (extends ClassA & InterfaceA)

attribBounds(tree.typarams);
------------------------------------------
class VisitMethodDefTest {
	class ClassA{}
	interface InterfaceA {}

	<TA,TB extends ClassA & InterfaceA> VisitMethodDefTest() {}
}
------------------------------------------
com.sun.tools.javac.comp.Attr===>attribBounds(1)
-------------------------------------------------------------------------
typarams=TA,TB extends ClassA & InterfaceA

typaram=TA
bound=java.lang.Object
bound.tsym.className=com.sun.tools.javac.code.Symbol$ClassSymbol
bound.tsym.flags_field=0x40000001 public acyclic 

typaram=TB extends ClassA & InterfaceA
bound=test.attr.VisitMethodDefTest.ClassA&test.attr.VisitMethodDefTest.InterfaceA
bound.tsym.className=com.sun.tools.javac.code.Symbol$ClassSymbol
bound.tsym.flags_field=0x51001401 public abstract synthetic compound unattributed acyclic 


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.4

chk.checkOverride(tree, m);
------------------------------------------
class VisitMethodDefTest {
	public enum enum_no_finalize {
		;
		protected final void finalize(){}
		public final void finalize(){}
		public final void finalize(int i){}
		public final int finalize(){return 0;}
	}
}
------------------------------------------
key=enum.no.finalize

test\attr\VisitMethodDefTest.java:7: ���� test.attr.VisitMethodDefTest.enum_no_f
inalize �ж��� finalize()
                public final void finalize(){}
                                  ^
test\attr\VisitMethodDefTest.java:9: ���� test.attr.VisitMethodDefTest.enum_no_f
inalize �ж��� finalize()
                public final int finalize(){return 0;}
                                 ^
test\attr\VisitMethodDefTest.java:6: ö�ٲ����� finalize ����
                protected final void finalize(){}
                                     ^
test\attr\VisitMethodDefTest.java:7: ö�ٲ����� finalize ����
                public final void finalize(){}
                                  ^
test\attr\VisitMethodDefTest.java:9: ö�ٲ����� finalize ����
                public final int finalize(){return 0;}
                                 ^
5 ����


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.5

com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	void m1() {}
}
class VisitMethodDefTest extends ClassA {
	static void m1() {}
}
------------------------------------------
key=override.static   key2=cant.override
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest �е� m1() �޷�
���� test.attr.ClassA �е� m1()�����ǵķ���Ϊ��̬
        static void m1() {}
                    ^
1 ����

��ע(��̬�����޷�ʵ�ֽӿ��еķ���)
com.sun.tools.javac.comp.Attr===>attribClassBody(2)
com.sun.tools.javac.comp.Check===>checkImplementations(1)
com.sun.tools.javac.comp.Check===>checkImplementations(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
interface InterfaceA {
	void m1();
}
class VisitMethodDefTest implements InterfaceA {
	static void m1() {}
}
------------------------------------------
key=override.static   key2=cant.implement
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest �е� m1() �޷�
ʵ�� test.attr.InterfaceA �е� m1()�����ǵķ���Ϊ��̬
        static void m1() {}
                    ^
1 ����


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	final void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}
------------------------------------------
key=override.meth   key2=cant.override
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest �е� m1() �޷�
���� test.attr.ClassA �е� m1()�������ǵķ���Ϊ 0x10 final
        void m1() {}
             ^
1 ����


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	static void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}
------------------------------------------
key=override.meth   key2=cant.override
test\attr\VisitMethodDefTest.java:6: test.attr.VisitMethodDefTest �е� m1() �޷�
���� test.attr.ClassA �е� m1()�������ǵķ���Ϊ 0x8 static
        void m1() {}
             ^
1 ����


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	void m1(){}
	private void m2(){}
	public void m3(){}
	protected void m4(){}
}
class VisitMethodDefTest extends ClassA {
	protected void m1(){}
	void m2(){}
	void m3(){}
	private void m4(){}
}
------------------------------------------
key=override.weaker.access   key2=cant.override
test\attr\VisitMethodDefTest.java:12: test.attr.VisitMethodDefTest �е� m3() ��
������ test.attr.ClassA �е� m3()�����ڳ���ָ�����͵ķ���Ȩ�ޣ�Ϊ 0x1 public
        void m3(){}
             ^
test\attr\VisitMethodDefTest.java:13: test.attr.VisitMethodDefTest �е� m4() ��
������ test.attr.ClassA �е� m4()�����ڳ���ָ�����͵ķ���Ȩ�ޣ�Ϊ 0x4 protected
        private void m4(){}
                     ^
2 ����





















