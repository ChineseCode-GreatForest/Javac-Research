Դ�ļ�:com.sun.tools.javac.comp.Attr.java

���Է����׶ε����:
com.sun.tools.javac.comp.Attr===>attribClass(2)


���Է����׶ε���ϸ����

com.sun.tools.javac.comp.Attr===>attribClass(2)��ʼ
--------------------------------------------------------------------
1: ����com.sun.tools.javac.comp.Annotate===>flush()
��һ���п�����Enter�׶ε���annotate.enterDone()��ɣ�
annotate.flush()��MemberEnter�еļ�������Annotate.Annotator()�����


com.sun.tools.javac.comp.Attr===>attribClass(1)��ʼ
--------------------------------------------------------------------
2: ����com.sun.tools.javac.comp.Check===>checkNonCyclic(2)
��鵱ǰ���뵱ǰ������ʵ�ֵĽӿڡ����г��ࡢowner֮���Ƿ�
����ѭ���̳С�����ѭ���̳�ʱ��
�������ᱨһ���ؼ���Ϊ��cyclic.inheritance���ı������
(��ע��������Ϣ��com\sun\tools\javac\resources\compiler.properties�а�
�ؼ������ã��������˵�������������ʱ��������������ͬ)

����Դ����:
--------------------------------------------------------------------
package my.error;
public class cyclic_inheritance extends cyclic_inheritance2 {}
class cyclic_inheritance2 extends cyclic_inheritance {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\cyclic_inheritance.java:2: ѭ���̳��漰 my.error.cyclic_inheritance
public class cyclic_inheritance extends cyclic_inheritance2 {}
       ^
--------------------------------------------------------------------

����Դ����:
--------------------------------------------------------------------
package my.error;
public class cyclic_inheritance extends cyclic_inheritance {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\cyclic_inheritance.java:2: ѭ���̳��漰 my.error.cyclic_inheritance
public class cyclic_inheritance extends cyclic_inheritance {}
       ^
--------------------------------------------------------------------


�����ǰ������ʵ�ֵĽӿڡ����г��ࡢowner��checkNonCyclic(2)ǰ
��������Է�������ô��checkNonCyclic(2)��ȷ��û�д���ѭ���̳е�
����£���ACYCLIC��־�ӽ���ǰ������ʵ�ֵĽӿڡ����г��ࡢOwner
��flags_field�ֶ��С�


3: ����com.sun.tools.javac.code.Types===>supertype(Type t)
�ҳ���ǰ���ֱ�ӳ��࣬�����ǰ������Ӧ��ClassSymbol��flags_field�ֶ���
û��COMPOUND��־����ǰ���ֱ�ӳ����tag��CLASS����ô�ȶԵ�ǰ���ֱ�ӳ���
�������Է���(�ɵ���com.sun.tools.javac.comp.Attr===>attribClass(1)��ʼ)��
ͬ���ģ������ǰ���owner��tag��CLASS����ô�ڽ����Ŷ�������ͬ�������Է�����


4: �жϵ�ǰ������Ӧ��ClassSymbol��flags_field�ֶ����Ƿ���UNATTRIBUTED��־��
���û�У�˵����ǰ�������������ĳ����owner����֮ǰ�����ѽ��й����Է�����
���ԾͲ��ٶԵ�ǰ��������Է��������������


5: ִ��flags_field &= ~UNATTRIBUTED��
ȥ��UNATTRIBUTED��־��ע���ѿ�ʼ�Ե�ǰ��������Է�����


6: ��Enter��typeEnvs(һ��HashMap)����ȡ��ǰ���Ӧ��env��������
�ඨ��ǰ��ע������������env.info.lint�������ڵ�ǰ����ඨ��ǰ�У�
--------------------------------------------------------------------
@SuppressWarnings({"fallthrough","unchecked"})
@Deprecated
public class Test {...}
--------------------------------------------------------------------
��ô��ǰ��Test��Ӧ��env.info.lint����:
env.info.lint=Lint:[values(8)[CAST, DEP_ANN, DIVZERO, EMPTY, FINALLY, OVERRIDES, PATH, SERIAL] suppressedValues(3)[DEPRECATION, FALLTHROUGH, UNCHECKED]]

�����lint�������֣�values(8)���ֱ�ʾ����������8�����͵ľ��治�����Σ�һ�����־�
���û��������棬suppressedValues(3)���ֱ�ʾ������������û����������־��档


7: ��ö������صļ��
1)�ڶ���ö����ʱ����ʹ�á�extends���ؼ��֣�Ҳ����˵������Ϊȥָ��
һ��ö����ĳ���(��Parser.enumDeclaration(2)����)

2)�κ���ʾ�������඼���ܴ� java.lang.Enum �̳У�
����һ���ؼ���Ϊ��enum.no.subclassing���ı������

����Դ����:
--------------------------------------------------------------------
package my.error;
public class enum_no_subclassing extends Enum {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\enum_no_subclassing.java:2: ���޷�ֱ�Ӽ̳� java.lang.Enum
public class enum_no_subclassing extends Enum {}
       ^
1 ����
--------------------------------------------------------------------

���������µķ����ඨ���ǺϷ���:
class MyTestA<T extends Enum> {}
class MyTestB<T extends Enum & Cloneable> {}

������ MyTestA �ķ��ͱ���T�����ް�(upper bound)��Enum��
��������COMPOUND�͵����ް󶨣����Ա���������Ϊ����������һ��ClassSymbol��
ͬ��Ҳ���ᵥ�������ް󶨽������Է�����

�����ڷ����� MyTestB ��˵�����ķ��ͱ���T�����ް��ǡ�Enum & Cloneable����
����һ��COMPOUND�͵����ް󶨣���������Ϊ����������һ��ClassSymbol��
�������ClassSymbol��flags_field��ABSTRACT|PUBLIC|SYNTHETIC|COMPOUND|ACYCLIC��
���ClassSymbol��Ӧ��ClassType��supertype_field��java.lang.Enum,
interfaces_field=java.lang.Cloneable,��������������COMPOUND�͵����ް�
��Ӧ��ClassSymbol�������Է�������Ȼ���ĳ����� java.lang.Enum ��������������
���������
(�й�COMPOUND�͵����ް󶨼�com.sun.tools.javac.code.Types���makeCompoundType(2)����)



3)�����ǰ�಻��ö�����ͣ�����ǰ���ֱ�ӳ�����ö�����ͣ�
��ô�������ᱨһ���ؼ���Ϊ��enum.types.not.extensible���Ĵ���
(ͬʱ������һ������)

����Դ����:
--------------------------------------------------------------------
package my.error;
enum MyEnum {}
public class enum_types_not_extensible extends MyEnum {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\enum_types_not_extensible.java:3: �޷������� my.error.MyEnum
���м̳�
public class enum_types_not_extensible extends MyEnum {}
                                               ^
bin\mysrc\my\error\enum_types_not_extensible.java:3: ö�����Ͳ��ɼ̳�
public class enum_types_not_extensible extends MyEnum {}
       ^
2 ����
--------------------------------------------------------------------


8: ��ʼ������������Է���
com.sun.tools.javac.comp.Attr===>attribClassBody(2)��ʼ
--------------------------------------------------------------------

8.1: ����com.sun.tools.javac.comp.Check===>validateAnnotations(2)�����ע��

8.1.1: ����com.sun.tools.javac.comp.Check===>validateAnnotation(2)

����com.sun.tools.javac.comp.Check===>validateAnnotation(1)��ʼ
--------------------------------------------------------------------

�ȼ��ע�ͳ�Աֵ�Ƿ����ظ������ظ���
��������ᱨһ���ؼ���Ϊ��duplicate.annotation.member.value���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
@interface MyAnnotation {
    String value();
}
@MyAnnotation(value="testA",value="testB")
public class duplicate_annotation_member_value  {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\duplicate_annotation_member_value.java:5: my.error.MyAnnotation �е�ע�ͳ�Աֵ value �ظ�
@MyAnnotation(value="testA",value="testB")
                                  ^
1 ����
--------------------------------------------------------------------

Ȼ�����Ƿ�Ϊ����û��Ĭ��ֵ��ע�ͳ�Աָ����Ӧֵ��
���򣬱������ᱨһ���ؼ���Ϊ��annotation.missing.default.value���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
@interface MyAnnotation {
    String valueA();
	String valueB() default "testB";
	String valueC();
}
@MyAnnotation(valueA="testA")
public class annotation_missing_default_value  {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\annotation_missing_default_value.java:7: 
ע�� my.error.MyAnnotation ȱ�� valueC
@MyAnnotation(valueA="testA")
^
1 ����
--------------------------------------------------------------------


������ע�������� java.lang.annotation.Target ��ô���Ŀ��ֵ�Ƿ��ظ���
���򣬱������ᱨһ���ؼ���Ϊ��repeated.annotation.target���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.TYPE})
public @interface repeated_annotation_target {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\repeated_annotation_target.java:4: ע��Ŀ���ظ�
@Target({ElementType.TYPE,ElementType.TYPE})
                                     ^
1 ����
--------------------------------------------------------------------

����com.sun.tools.javac.comp.Check===>validateAnnotation(1)����
--------------------------------------------------------------------


8.1.2: ����com.sun.tools.javac.comp.Check===>annotationApplicable(2)

����com.sun.tools.javac.comp.Check===>annotationApplicable(2)��ʼ
--------------------------------------------------------------------
�ȵ���com.sun.tools.javac.code.Symbol$ClassSymbol===>attribute(Symbol anno)
�����ע�����͵�java.lang.annotation.Target, ���ע�������ڶ���ʱû��ָ
�� Target ��ôattribute(Symbol anno)����������null�����򷵻�ָ���� Target.

�������ע�����͡� java.lang.Deprecated ��
------------------------------------------
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Deprecated {
}
------------------------------------------
��ôattribute(Symbol anno)�������������� Target Ϊnull��
��ͱ�ʾע������ Deprecated ��������Դ�����е�
�κγ���Ԫ��(any program element)ǰ(��:�ࡢ�ӿڡ��������ֶ�����ǰ��)


���������ע�����͡� java.lang.SuppressWarnings ��
----------------------------------------------------------------
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    String[] value();
}
----------------------------------------------------------------
attribute(Symbol anno)�������ص� Target ����:
Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
Ҳ����˵ע������ SuppressWarnings ��������:
TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE�⼸��
����Ԫ������ǰ�棬������������PACKAGE����ǰ�棬

����Դ����:
--------------------------------------------------------------------
@SuppressWarnings({"fallthrough","unchecked"})
package my.error;
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
�����ע��Ӧ���ļ� package-info.java ��
@SuppressWarnings({"fallthrough","unchecked"})
^
1 ����
--------------------------------------------------------------------
(��ע:ע������ SuppressWarnings ����������ANNOTATION_TYPE����ǰ�棬
��Ȼ Target ��û����ʾָ��ANNOTATION_TYPE������ʾָ����TYPE��ָ����
TYPE��Ҳ���൱��ָ����ANNOTATION_TYPE��
��ΪTYPE������ǣ��ࡢ�ӿڡ�ע�͡�ö������������)

��ע:@SuppressWarnings({"fallthrough","unchecked"})�������ã���Ϊpackage-info���μ�Attr

��annotationApplicable(2)����������attribute(Symbol anno)�������ص� Target
�뵱ǰSymbol������ֶ�(kind��owner��flags_field)���бȽϣ�������ڲ�ƥ���
������ͷ���false�����򷵻�true��

����com.sun.tools.javac.comp.Check===>annotationApplicable(2)����
--------------------------------------------------------------------


8.1.3: ��com.sun.tools.javac.comp.Check===>validateAnnotation(2)������
����com.sun.tools.javac.comp.Check===>annotationApplicable(2)�����ķ���ֵ��
���Ϊfalse���������ᱨһ���ؼ���Ϊ��annotation.type.not.applicable���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.test;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

@MyAnnotation("test")
public class annotation_type_not_applicable {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\annotation_type_not_applicable.java:9: ע�����Ͳ������ڸ����͵�����
@MyAnnotation("test")
^
1 ����
--------------------------------------------------------------------

��Ϊ�ڶ���ע������ MyAnnotation ��ʱ��ָ������TargetΪFIELD��METHOD��
Ҳ����˵ע������ MyAnnotation ֻ�������ֶκͷ���������ǰ�棬��������
���������ȴ�������ඨ��ǰ���ˡ�


8.1.4: �����һ������ǰʹ���ˡ�@Override��ע�ͱ�ǣ���ô����
com.sun.tools.javac.comp.Check===>isOverrider(Symbol s)���÷����Ƿ�
���ǻ�ʵ�ֳ����͵ķ���������÷���û�и��ǻ�ʵ�ֳ����͵ķ�����
��ôisOverrider(Symbol s)���� false ��
ͬʱ�������ᱨһ���ؼ���Ϊ��method.does.not.override.superclass���Ĵ���

(
��ע:
����ĳ���������ָ��̳�����ʵ�����Ϲ�������������ӿ���ɵ�һ���հ����ͼ���,
�������С�method_does_not_override_superclass����ĳ����;������µ����ͼ���:
[method_does_not_override_superclass��superClassTestB��superClassTestA��
InterfaceTest��java.lang.Object]
)

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest {
	void myOverrideMethodA(int i,char c);
}

abstract class superClassTestA implements InterfaceTest {
	public void myOverrideMethodB(int i,char c) {}
}

abstract class superClassTestB extends superClassTestA {
	public void myOverrideMethodC(int i,char c) {}
}

public class method_does_not_override_superclass extends superClassTestB {
	//�������������ĵڶ��������볬�����ж�Ӧ�����������ĵڶ���������ͬ��
	//����ʹ�á�@Override��ע�ͱ�ǲ���ǡ������û�������ﵽ���ǵ�Ŀ�ġ�
	@Override
	public void myOverrideMethodA(int i,byte b) {}

	@Override
	public void myOverrideMethodB(int i,byte b) {}

	@Override
	public void myOverrideMethodC(int i,byte b) {}
}
--------------------------------------------------------------------


���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\method_does_not_override_superclass.java:17: �������Ḳ�ǻ�ʵ�ֳ����͵ķ���
        @Override
        ^
bin\mysrc\my\error\method_does_not_override_superclass.java:20: �������Ḳ�ǻ�ʵ�ֳ����͵ķ���
        @Override
        ^
bin\mysrc\my\error\method_does_not_override_superclass.java:23: �������Ḳ�ǻ�ʵ�ֳ����͵ķ���
        @Override
        ^
3 ����
--------------------------------------------------------------------

����com.sun.tools.javac.comp.Check===>validateAnnotation(2) ����
-------------------------------------------------------------------------

�����ǰ�໹����ע��Ҫ��飬����ת�� 8.1.1

����ת�� 9

����com.sun.tools.javac.comp.Check===>validateAnnotations(2)����
-------------------------------------------------------------------------


9: ����com.sun.tools.javac.comp.Attr===>attribBounds(1)����
�����ǰ����һ�������࣬�������������ķ��ͱ�����COMPOUND�͵����ް󶨣�
������������Է����׶�֮ǰ(MemberEnter�׶�)��ΪCOMPOUND�͵����ް󶨵�������
һ��ClassSymbol�����ڵö����ClassSymbol��ʼ�������Է�����






















