11: ע�����Ͷ��岻��ʹ��extends��Ҳ���������Ͳ���������
�������ͻᱨһ���ؼ���Ϊ��cant.extend.intf.annotation���Ĵ���
�Լ�һ���ؼ���Ϊ��intf.annotation.cant.have.type.params���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest {}
public @interface cant_extend_intf_annotation extends InterfaceTest {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_extend_intf_annotation.java:3: ���� @interface�������� "extends"
public @interface cant_extend_intf_annotation extends InterfaceTest {}
                                                      ^
1 ����
--------------------------------------------------------------------

����Դ����:
--------------------------------------------------------------------
package my.error;
public @interface intf_annotation_cant_have_type_params<T> {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\intf_annotation_cant_have_type_params.java:2: @interface ����
�������Ͳ���
public @interface intf_annotation_cant_have_type_params<T> {}
                                                        ^
1 ����
--------------------------------------------------------------------