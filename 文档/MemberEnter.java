����˵���ġ����͡���ָ:
��(Class)���ӿ�(Interface)��ע��(Annotation)��ö����(Enum)
1.��com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)��ʼ��
���sym��һ���������࣬ת��1.2

1.2 com.sun.tools.javac.comp.MemberEnter===>visitTopLevel(1)
�����ͬһԴ�ļ��У��������ͬʱ������N��(N>=2)����:
-----------------------------
package test.memberEnter;

public class ClassA{}
class ClassB{}
interface InterfaceA{}
...........
-----------------------------
��ô��visitTopLevel(1)��ֻ����ClassA������ClassB��InterfaceAֱ�ӷ���

visitTopLevel(1)��������:
1) ������������ͻ��飬
��:
-----------------------------
package test.memberEnter.clash1.clash2;
public class ClashTest {}
-----------------------------
����-classpath��E:\javac�����������ļ�:
E:\javac\test.java
E:\javac\test\memberEnter.java
E:\javac\test\memberEnter\clash1.java
E:\javac\test\memberEnter\clash1\clash2.java
����������ļ�������ʲô��
������ClashTest.javaʱ���ᱨ��:
------------------------------------
test\memberEnter\clash1\clash2\ClashTest.java:1: ����� test.memberEnter.clash1.
clash2 �������ͬ���Ƶ����ͻ
package test.memberEnter.clash1.clash2;
^
1 ����
------------------------------------
����clash1.java��memberEnter.javaҲ����������������ͻ��������
��package test.memberEnter.clash1.clash2;�����ڵ�JCTree�Ŀ�ʼλ��(pos)����ͬһ����
������log.error(...)ʱֻ����һ�Σ����Ƕ���E:\javac\test.javaȴ�ǺϷ��ģ�
��Ȼ��������еġ�test��ͬ��E:\javac���Ŀ¼�£�
���Ǳ�����ֻ�������е�һ����.����֮����Ӱ����Ƿ��ڶ�Ӧ����Ŀ¼��������ͻ��
�������û�С�.���ţ��硰package test������һ����û��ָ��package��
��ô�������Ŀ¼��������ͻ,����Ŀ�������:
package test.memberEnter.clash1.clash2;
����test�⣬memberEnter��clash1��clash2��Ҫ���

2) ����com.sun.tools.javac.comp.MemberEnter===>annotateLater(3)
���������ļ���package-info.java���������а�ע�ͣ� 
����Annotate�ġ�ListBuffer<Annotator> q = new ListBuffer<Annotator>();����
���������ȣ������Ժ���
������ע�⣬��package-info.java�ļ��ǲ����а�ע�͵ģ���Enter���Ѽ����

3) ����com.sun.tools.javac.comp.MemberEnter===>importAll(3)
��"java.lang"���е����������JCCompilationUnit toplevel.starImportScope��
ע��:starImportScope��һ��ImportScope��

4) ���û��import��䣬��visitTopLevel(1)����������
��������import��䣬ת��1.3