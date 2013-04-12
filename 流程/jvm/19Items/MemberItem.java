    /** An item representing an instance variable or method.
     */
    class MemberItem extends Item {
		/** The represented symbol.
		 */
		Symbol member;

		/** Flag that determines whether or not access is virtual.
		 */
		boolean nonvirtual;

		MemberItem(Symbol member, boolean nonvirtual) {
			super(Code.typecode(member.erasure(types)));
			this.member = member;
			this.nonvirtual = nonvirtual;
		}

		Item load() {
			code.emitop2(getfield, pool.put(member));
			return stackItem[typecode];
		}

		void store() {
				DEBUG.P(this,"store()");
			DEBUG.P("member="+member);
			code.emitop2(putfield, pool.put(member));
				DEBUG.P(0,this,"store()");
		}
		
		//����Invokeָ�������<<����java�����>>P404-P409
		//��static�ֶ��뷽����StaticItem���ʾ�����Բ���invoke()��������Χ֮��
		Item invoke() {
			DEBUG.P(this,"invoke()");
			DEBUG.P("nonvirtual="+nonvirtual);
			DEBUG.P("member="+member);
			DEBUG.P("member.owner.flags()="+Flags.toString(member.owner.flags()));
			DEBUG.P("");
			DEBUG.P("member.type="+member.type);
			/*
			���member��һ���ڲ���Ա��Ĺ��췽������ô�ڵ���externalType����
			��õ�һ���µ�MethodType�����MethodType�ĵ�һ�����������������
			�ڲ���Ա���owner
			����Դ����:
			---------------------------
			package my.test;
			public class Test {
				class MyInnerClass{
					MyInnerClass(){
						this("str");
					}
					MyInnerClass(String str){}
				}
			}
			---------------------------
			�������ڱ��뵽��this("str");���������ʱ����ִ�е������invoke()����
			�����ǵ���������(����):

			com.sun.tools.javac.jvm.Items$MemberItem===>invoke()
			-------------------------------------------------------------------------
			nonvirtual=true
			member=MyInnerClass(java.lang.String)
			member.owner.flags()=0

			member.type=Method(java.lang.String)void		//ע������ֻ��һ������
			mtype=Method(my.test.Test,java.lang.String)void //ע������������������
			com.sun.tools.javac.jvm.Code===>emitInvokespecial(int meth, Type mtype)
			-------------------------------------------------------------------------
			meth=2 mtype=Method(my.test.Test,java.lang.String)void
			com.sun.tools.javac.jvm.Code===>emitop(int op)
			-------------------------------------------------------------------------
			emit@5 stack=3: invokespecial(183)
			com.sun.tools.javac.jvm.Code===>emitop(int op)  END
			-------------------------------------------------------------------------

			com.sun.tools.javac.jvm.Code===>emitInvokespecial(int meth, Type mtype)  END
			-------------------------------------------------------------------------

			com.sun.tools.javac.jvm.Items$MemberItem===>invoke()  END
			-------------------------------------------------------------------------
			*/
			MethodType mtype = (MethodType)member.externalType(types);
			DEBUG.P("mtype="+mtype);

			int rescode = Code.typecode(mtype.restype);
			if ((member.owner.flags() & Flags.INTERFACE) != 0) {
				code.emitInvokeinterface(pool.put(member), mtype);
			} else if (nonvirtual) {
				code.emitInvokespecial(pool.put(member), mtype);
			} else {
				code.emitInvokevirtual(pool.put(member), mtype);
			}
			DEBUG.P(0,this,"invoke()");
			return stackItem[rescode];
		}

		void duplicate() {
			stackItem[OBJECTcode].duplicate();
		}

		void drop() {
			stackItem[OBJECTcode].drop();
		}

		void stash(int toscode) {
			stackItem[OBJECTcode].stash(toscode);
		}

		int width() {
			return 1;
		}

		public String toString() {
			return "member(" + member + (nonvirtual ? " nonvirtual)" : ")");
		}
    }