public boolean isCastable(Type t, Type s, Warner warn)
��������t�ܷ�ת��������s
�ٶ�t��s����(t.isPrimitive() != s.isPrimitive())��
��˼����˵���������������:t��ԭʼ���Ͷ�s����ԭʼ���ͣ�����s��ԭʼ���Ͷ�t����ԭʼ����
���t��
BYTE
CHAR
SHORT
INT
LONG
FLOAT
DOUBLE
��ôS��������������7��ԭʼ�����е�����һ����
Ҳ����˵�������7��ԭʼ��������֮������໥ת��
��:
double d=10.22;
int i = (int)d;
��ʱt����DOUBLE��s����INT��isCastable������true����ΪDOUBLE����ת����INT

���t��BOOLEAN����ôs������BOOLEAN

���t��VOID����ôt����ת������������
��:
void m();
int i=(int)m();

���t��null(BOT)��
ֻҪs��BOT��CLASS��ARRAY��TYPEVAR����ô�Ϳ���ת����
��Ϊnull(BOT)��BOT��CLASS��ARRAY��TYPEVAR������