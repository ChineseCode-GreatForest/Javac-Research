15: 
����com.sun.tools.javac.comp.Check===>checkImplementations(1)������
// Check that all methods which implement some
        // method conform to the method they implement.



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



