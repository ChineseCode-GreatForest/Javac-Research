���ݽṹ
JCTree	Symbol	Type	Scope	Env

variables, methods and operators,types, packages

Symbol������:
TypeSymbol
PackageSymbol		����		Scope members_field
ClassSymbol			����		Scope members_field;
MethodSymbol
VarSymbol

DelegatedSymbol
OperatorSymbol


ÿ��JCTree			����		Type type

ÿ��Type			����		TypeSymbol tsym

ÿ��Symbol			����		Type type

JCCompilationUnit	����		PackageSymbol packge
					����		Scope namedImportScope;
					����		Scope starImportScope;

JCClassDecl			����		ClassSymbol sym

JCMethodDecl		����		MethodSymbol sym

JCVariableDecl		����		VarSymbol sym

JCNewClass			����		Symbol constructor

JCAssignOp			����		Symbol operator;

JCUnary				����		Symbol operator;

JCBinary			����		Symbol operator;

JCFieldAccess		����		Symbol sym;

JCIdent				����		Symbol sym;


Parser�׶�ֻ���ɻ�����JCTree����ʱ���������JCTree��TypeΪnull��
������ʾ�ļ���JCTree��SymbolҲΪnull��ScopeҲΪnull

��Enter�׶Σ���visitClassDef(JCClassDecl tree)������
��дJCClassDecl��ClassSymbol sym�ֶΣ�����дClassSymbol sym
���ڲ��ֶ�flags_field,sourcefile,members_field
�ڲ����classfile�ֶ�һ��Ϊnull
ClassType��outer_field��typarams_field�ֶ�Ҳ��Enter�׶ε�
visitClassDef(JCClassDecl tree)����������,
supertype_field,interfaces_field,allparams_fieldΪnull

***��һ�׶�Enter���***
-----------------------------------------------
����: my.test
--------------------------
tree.packge.members_field: Scope[(nelems=17 owner=test)MyUncompletedClass, Test07, Test06, Test05, Test01, Test04, Test03, ExtendsTestBound, TestBound, TestOhter2, Test02, MyInterfaceB, MyInterfaceA, TestOhter, Test$TestInner, Test$1, Test]
tree.namedImportScope    : Scope[(nelems=1 owner=test)Test]
tree.starImportScope     : Scope[(nelems=0 owner=test)]

�ȴ�������������: 5
--------------------------
����             : my.test.Test
members_field    : Scope[(nelems=4 owner=Test)MyInnerEnum, MyInnerInterface, MyInnerClassStatic, MyInnerClass]
flags            : public 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : bin\mysrc\my\test\Test.java
type             : my.test.Test<S2704014,T13673945,E3705235>
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : S2704014,T13673945,E3705235
allparams_field  : null

����             : my.test.Test.MyInnerClass
members_field    : Scope[(nelems=0 owner=MyInnerClass)]
flags            : public 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test<S2704014,T13673945,E3705235>.MyInnerClass
outer_field      : my.test.Test<S2704014,T13673945,E3705235>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

����             : my.test.Test.MyInnerClassStatic
members_field    : Scope[(nelems=0 owner=MyInnerClassStatic)]
flags            : public static 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerClassStatic
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

����             : my.test.Test.MyInnerInterface
members_field    : Scope[(nelems=0 owner=MyInnerInterface)]
flags            : public static interface abstract 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerInterface
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

����             : my.test.Test.MyInnerEnum
members_field    : Scope[(nelems=0 owner=MyInnerEnum)]
flags            : public static final enum 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerEnum
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null



��MemberEnter�׶�:
1.�Ƚ�java.lang���е������ർ��ÿ��JCCompilationUnit��starImportScope;

2.��������non-static��static����(import)���
��Ϊ���еĵ���(import)��䶼����һ��JCFieldAccess����ʾ��(�μ�Parser.importDeclaration())��
JCFieldAccess��Ҳ����JCIdent��
��MemberEnter�׶ε�visitImport(1)�����л���
��JCFieldAccess��JCIdent��Symbol sym�ֶ�.
��com.sun.tools.javac.comp.Attr===>check(5)������JCTree��type�ֶ�

��complete(Symbol sym)������Type��supertype_field��interfaces_field

��com.sun.tools.javac.comp.Attr===>visitTypeParameter(1)��
��COMPOUND��TypeVar����һ��JCClassDecl����enter.typeEnvs

���JCMethodDecl��TypeParameter������typeΪForAll���ͣ�����ΪMethodType
�ο�com.sun.tools.javac.comp.MemberEnter===>signature(5)

��com.sun.tools.javac.comp.MemberEnter===>visitMethodDef(1)��
��JCMethodDecl��Ӧ��MethodSymbol����JCClassDecl��ClassSymbol sym��members_field

��com.sun.tools.javac.comp.MemberEnter===>signature(5)��
����������TypeParameter�����������е���ͨ���������ӦMethodSymbol��scope

Ĭ�Ϲ��췽����:
com.sun.tools.javac.comp.MemberEnter.DefaultConstructor()��������

�ڹ��췽���м���super()����:
com.sun.tools.javac.comp.Attr.visitMethodDef()��������

JCTree.type����erasure���type

��Ա���MemberEnter��
��ͨ����Ա�����Ե�ClassSymbol��һЩ�ܹ�����
ClassSymbol.complete()�ķ�������ӵ���
com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)�����
�Գ�Ա���MemberEnter��

��:
public class Test{
	public class MyTestInnerClass<Z>{}
	
	public void myMethod(MyTestInnerClass<String> m) {}
}

��������myMethod�����Ĳ�����MyTestInnerClass<String> mʱ
��ͨ��com.sun.tools.javac.comp.Attr===>visitIdent(1)����MyTestInnerClass��
����Ҫ֪��MyTestInnerClass��Ӧ��ClassSymbol��flags_field�ֶ�����ʱ��
��ͨ��ClassSymbol.flags()�������鿴�����MyTestInnerClass��δcomplete����ô
�͵���com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)�����
�Գ�Ա��MyTestInnerClass��MemberEnter��


JCTypeApply.type��JCTypeApply.type.tsym.type�ǲ�һ���ģ�
ǰ����ʵ�Σ�����ֻ���βΡ�
��:
tree.type=my.test.Test<S12122157,P28145575,V25864734,T10923757,E19300430>.ExtendsTest<?{:java.lang.Object:},? super my.test.Test.ExtendsTest{:java.lang.Object:}>
tree.type.tsym.type=my.test.Test<S12122157,P28145575,V25864734,T10923757,E19300430>.ExtendsTest<T471035,S31406333>
com.sun.tools.javac.comp.Attr===>visitTypeApply(JCTypeApply tree)  END




