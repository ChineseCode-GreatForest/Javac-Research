(1)
Ϊʲô
List<JCAnnotation> packageAnnotations = List.nil();
������ List.nil() ���� List.<JCAnnotation>nil().


(2)
������
ParserTest(final /** @deprecated */ int i){}
ParserTest(/** @deprecated */ final int i){}
��������һ����ǰһ����ʾ�ѹ�ʱ����һ��û����ʾ

(3)
LetExpr��ô���ģ���������������

(4)
------------------------------------------------------------
test\enter\EnterTest.java:3: ���棺[deprecation] test.enter �е� test.enter.Ente
rTestB �ѹ�ʱ
class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTest
B & EnterTestC,TD> {
                           ^
test\enter\EnterTest.java:3: ���棺[deprecation] test.enter �е� test.enter.Ente
rTestB �ѹ�ʱ
class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTest
B & EnterTestC,TD> {
                                                                       ^
2 ����
------------------------------------------------------------
package test.enter;

class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTestB & EnterTestC,TD> {
	class ClassA{}
	static class ClassB{}
	static void methodA() {
		class LocalClass{}
	}
	void methodB() {
		class LocalClass{}
	}

	/**
     * 
     * @deprecated  //��������ڶ���"}"��û���������������EnterTestB
     */
}
class EnterTestB {
	static class ClassB{}
}
interface EnterTestC {}