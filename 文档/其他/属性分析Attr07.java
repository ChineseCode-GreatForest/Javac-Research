14: 

// Check that a generic class doesn't extend Throwable
�������ᱨһ���ؼ���Ϊ��generic.throwable���Ĵ���

����Դ����:
--------------------------------------------------------------------
package my.error;
public class generic_throwable<T> extends Exception {}
--------------------------------------------------------------------

���������ʾ��Ϣ����:
--------------------------------------------------------------------
bin\mysrc\my\error\generic_throwable.java:2: �������޷��̳� java.lang.Throwable
public class generic_throwable<T> extends Exception {}
                                          ^
1 ����
--------------------------------------------------------------------