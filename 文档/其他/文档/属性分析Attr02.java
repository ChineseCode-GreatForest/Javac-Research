10: ����com.sun.tools.javac.comp.Check===>validateTypeParams(1)����
��鷺������������Ͳ���(type parameters)

10.1: ����com.sun.tools.javac.comp.Check$Validator===>visitTypeParameter(1)
�ȼ��JCTypeParameter��bounds��JCTypeParameter��bounds������������:

������Щ������Դ����
-----------------------------
interface InterfaceA{}
interface InterfaceB{}
class ClassA{}
-----------------------------

��һ��:��COMPOUND�͵����ް�

1) ����extends  ��:class TestA<T>{}
2) ��extends������extends����������      ��:class TestB<T extends ClassA>{}
3) ��extends������extends�����ǽӿ���    ��:class TestC<T extends InterfaceA>{}
3) ��extends������extends���������ͱ���  ��:class TestD<T,V extends T>{}


�ڶ���:COMPOUND�͵����ް�(���ͱ������涼������extends�ؼ���)

1) ��ʽ:  �ӿ�A & �ӿ�B & ... & �ӿ�N

   ��:class TestE<T extends InterfaceA & InterfaceB>{}

2) ��ʽ:  ���� & �ӿ�A & �ӿ�B & ... & �ӿ�N

   ��:class TestF<T extends ClassA & InterfaceA & InterfaceB>{}

COMPOUND�͵����ް��б��У���һ���󶨿�������ͽӿڣ�
�ӵڶ����󶨿�ʼ�������ǽӿڣ����ͱ������ܳ�����COMPOUND�͵����ް��б��С�


����Դ����:
--------------------------------------------------------------------
package my.error;
public class UpperBoundTest {
	interface InterfaceA{}
	interface InterfaceB{}
	class ClassA{}
	class ClassB{}

	class TestA<T>{}
	class TestB<T extends ClassA>{}
	class TestC<T extends InterfaceA>{}
	class TestD<T,V extends T>{}

	class TestE<T extends InterfaceA & InterfaceB>{}
	class TestF<T extends ClassA & InterfaceA & InterfaceB>{}

	//�����ĸ����޷�����ͨ��
	class TestG<T extends ClassA & ClassB & InterfaceB>{}
	class TestH<T,V extends T & ClassA & InterfaceA>{}
	class TestI<T,V extends ClassA & T & InterfaceA>{}
	class TestJ<T,V extends ClassA & InterfaceA & T>{}
}
--------------------------------------------------------------------


���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\UpperBoundTest.java:17: �˴���Ҫ�ӿ�
        class TestG<T extends ClassA & ClassB & InterfaceB>{}
                                       ^
bin\mysrc\my\error\UpperBoundTest.java:18: ���ͱ������治�ܴ����������Ʒ�Χ
        class TestH<T,V extends T & ClassA & InterfaceA>{}
                                    ^
bin\mysrc\my\error\UpperBoundTest.java:19: ���������
�ҵ��� ���Ͳ��� T26867996
��Ҫ�� ��
        class TestI<T,V extends ClassA & T & InterfaceA>{}
                                         ^
bin\mysrc\my\error\UpperBoundTest.java:20: ���������
�ҵ��� ���Ͳ��� T20918341
��Ҫ�� ��
        class TestJ<T,V extends ClassA & InterfaceA & T>{}
                                                      ^
4 ����
--------------------------------------------------------------------


ÿһ��JCTypeParameter��bounds�ֶζ���List<JCExpression>���͵ģ�
���һ��JCTypeParameterû�а�(�� class Test<T>)����ôbounds�ֶ�
��һ��Ԫ�ظ���Ϊ0��List<JCExpression>��������bounds=null��
��com.sun.tools.javac.comp.Check===>visitTypeParameter(1)��������
����com.sun.tools.javac.comp.Check===>validate(List<? extends JCTree> trees)����
��List<JCExpression> bounds�е�ÿһ��JCExpression���м�飬
��ÿһ��JCExpression�����ݵ�validate(JCTree tree)�����У�
validate(JCTree tree)�������ٸ���JCTree�Ĳ�ͬ������ò�ͬ�ķ���:

JCArrayTypeTree:
��Ӧcom.sun.tools.javac.comp.Check$Validator===>visitTypeArray(1)

JCWildcard:
��Ӧcom.sun.tools.javac.comp.Check$Validator===>visitWildcard(1)

JCFieldAccess:
��Ӧcom.sun.tools.javac.comp.Check$Validator===>visitSelect(1)

JCTypeApply:
��Ӧcom.sun.tools.javac.comp.Check$Validator===>visitTypeApply(1)

����JCTree������:
��Ӧcom.sun.tools.javac.comp.Check$Validator===>visitTree(1)(�����κ��µķ���)


���JCTypeParameter������һ������JCFieldAccess�͵�JCTree����ô��
com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)�����еü��
����󶨲����ڲ�������������ѡ��̬�࣬
���򣬱������ᱨһ���ؼ���Ϊ��cant.select.static.class.from.param.type���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
class ExtendsTest<T> {
	static class InnerStaticClass {}
}
public class cant_select_static_class_from_param_type
             <T extends ExtendsTest<String>.InnerStaticClass> {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_select_static_class_from_param_type.java:6: �޷��Ӳ�����
��������ѡ��̬��
             <T extends ExtendsTest<String>.InnerStaticClass> {}
                                           ^
1 ����
--------------------------------------------------------------------


���JCTypeParameter������һ������JCFieldAccess�͵�JCTree����ô��
com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)�����л��ü��
����󶨲����ڲ�������������ѡ��ǲ�������(�ٶ��������һ��������)��
���򣬱������ᱨһ���ؼ���Ϊ��improperly.formed.type.param.missing���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
class ExtendsTest<T> {
	class InnerClass<V> {}
}
public class improperly_formed_type_param_missing 
             <T extends ExtendsTest<String>.InnerClass> {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\improperly_formed_type_param_missing.java:6: ���͵ĸ�ʽ����ȷ
��ȱ��ĳЩ����
             <T extends ExtendsTest<String>.InnerClass> {}
                                           ^
1 ����
--------------------------------------------------------------------






