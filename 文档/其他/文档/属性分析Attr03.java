11: �����ǰ����һ���ǳ�����(flags_field�ֶβ�����ABSTRACT | INTERFACE��־)��
����com.sun.tools.javac.comp.Check===>checkAllDefined(2)�����������ǳ�����
�Ƿ��г��󷽷����Լ�ʵ��ʵ���˳������е����г��󷽷���
ֻҪ�ҵ���һ��û��ʵ�ֵĳ��󷽷���
�������ͻᱨһ���ؼ���Ϊ��does.not.override.abstract���Ĵ���
���������ʹ����δʵ�ֵĳ��󷽷���Ҳ���ټ�����顣

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract ���ǳ���ģ�����δ���� my.error.does_not_override_abstract �еĳ���
�� innerAbstractMethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 ����
--------------------------------------------------------------------

����ѡ�abstract void innerAbstractMethod();����һ��ע�͵���
����������ż�鳬�ࡰAbstractClass���������ڳ��ࡰAbstractClass������һ��
��abstractClassMethod()���ĳ��󷽷���Ҳͬ������

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract ���ǳ���ģ�����δ���� my.error.AbstractClass �еĳ��󷽷� abstractCl
assMethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 ����
--------------------------------------------------------------------

����ٰѡ�abstract void abstractClassMethod();����һ��ע�͵���
��Ϊ��AbstractClass����ʵ���ˡ�InterfaceTest���ӿڣ�����
����������ż�顰InterfaceTest���ӿڣ������ڡ�InterfaceTest���ӿ�����һ��
interfaceMethod()����û��ʵ�֣�Ҳͬ������

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	//abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract ���ǳ���ģ�����δ���� my.error.InterfaceTest �еĳ��󷽷� interfaceM
ethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 ����
--------------------------------------------------------------------

����ٰѡ�void interfaceMethod();����һ��ע�͵���
��Ϊ��does_not_override_abstract����ʵ���ˡ�InterfaceTest2���ӿڣ�����
����������ż�顰InterfaceTest2���ӿڣ������ڡ�InterfaceTest2���ӿ�����һ��
interfaceMethod2()����û��ʵ�֣�Ҳͬ������

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	//void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	//abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract ���ǳ���ģ�����δ���� my.error.InterfaceTest2 �еĳ��󷽷� interface
Method2()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 ����
--------------------------------------------------------------------