13: 
����com.sun.tools.javac.comp.Check===>checkClassBounds(2)������
// Check that class does not import the same parameterized interface
        // with two different argument lists.

�������ᱨһ���ؼ���Ϊ��cant.inherit.diff.arg���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest<A extends Number> {}
class ExtendsTest implements InterfaceTest<Integer>{}
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>{}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_inherit_diff_arg.java:4: �޷�ʹ�����²�ͬ�Ĳ����̳� my.error.InterfaceTest��<java.lang.Float> �� <java.lang.Integer>
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>{}
                                   ^
1 ����
*/
--------------------------------------------------------------------