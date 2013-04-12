    /** Check that given modifiers are legal for given symbol and
     *  return modifiers together with any implicit modififiers for that symbol.
     *  Warning: we can't use flags() here since this method
     *  is called during class enter, when flags() would cause a premature
     *  completion.
     *  @param pos           Position to be used for error reporting.
     *  @param flags         The set of modifiers given in a definition.
     *  @param sym           The defined symbol.
     */
    long checkFlags(DiagnosticPosition pos, long flags, Symbol sym, JCTree tree) {
		DEBUG.P(this,"checkFlags(4)");
		DEBUG.P("flags="+Flags.toString(flags));
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));

		long mask;
		long implicit = 0;
		switch (sym.kind) {
			case VAR:
				if (sym.owner.kind != TYP)
					mask = LocalVarFlags; //���ر���
				else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceVarFlags; //�ӿ��ж�����ֶ�
				else
					mask = VarFlags; //���ж�����ֶ�
				break;
			case MTH:
				DEBUG.P("sym.name="+sym.name);
				DEBUG.P("if (sym.name == names.init)="+(sym.name == names.init));
				DEBUG.P("sym.owner.flags_field="+Flags.toString(sym.owner.flags_field));
				if (sym.name == names.init) {
					if ((sym.owner.flags_field & ENUM) != 0) { 
						/*��������:
						bin\mysrc\my\test\Test.java:16: �˴�������ʹ�����η� public
						   public MyInnerEnum() {}
								  ^
						bin\mysrc\my\test\Test.java:16: �˴�������ʹ�����η� protected
							protected MyInnerEnum() {}
									  ^         
						*/
						// enum constructors cannot be declared public or
						// protected and must be implicitly or explicitly
						// private
						implicit = PRIVATE;
						mask = PRIVATE;
					} else
						mask = ConstructorFlags;
				} else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceMethodFlags;
				else {
					mask = MethodFlags;
				}

				//����������ǳ����(abstract)��
				//���Ҷ��巽�������ͺ���strictfp���η���
				//��˷���ҲĬ�Ϻ���strictfp���η�
				//�ӿڷ���Ĭ����public abstract��������strictfp���η�
				DEBUG.P("((flags|implicit) & Flags.ABSTRACT)="+Flags.toString(((flags|implicit) & Flags.ABSTRACT)));
				// Imply STRICTFP if owner has STRICTFP set.
				if (((flags|implicit) & Flags.ABSTRACT) == 0) //�ο�<<����java�����>>P290
					implicit |= sym.owner.flags_field & STRICTFP;
				DEBUG.P("implicit="+Flags.toString(implicit));
				break;
			case TYP:
				DEBUG.P("sym.isLocal()="+sym.isLocal());
				DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
				if (sym.isLocal()) {
					mask = LocalClassFlags;
					if (sym.name.len == 0) { // Anonymous class
						// Anonymous classes in static methods are themselves static;
						// that's why we admit STATIC here.
						mask |= STATIC;
						// JLS: Anonymous classes are final.
						implicit |= FINAL;
					}
						
					if ((sym.owner.flags_field & STATIC) == 0 &&
						(flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
				} else if (sym.owner.kind == TYP) {
					mask = MemberClassFlags;
					if (sym.owner.owner.kind == PCK ||
						(sym.owner.flags_field & STATIC) != 0)
						mask |= STATIC;
					/*Դ������:
					public class Test {
						public class MyInnerClass {
							public enum MyInnerEnum2{}
						}
					}
					
					������ʾ:
					bin\mysrc\my\test\Test.java:11: ֻ���ھ�̬�������в�����ʹ��ö������
							public enum MyInnerEnum2{}
								   ^
					*/
					else if ((flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
					// Nested interfaces and enums are always STATIC (Spec ???)
					if ((flags & (INTERFACE | ENUM)) != 0 ) implicit = STATIC;
				} else {
					mask = ClassFlags;
				}
				// Interfaces are always ABSTRACT
				if ((flags & INTERFACE) != 0) implicit |= ABSTRACT;

				if ((flags & ENUM) != 0) {
					// enums can't be declared abstract or final
					mask &= ~(ABSTRACT | FINAL);
					implicit |= implicitEnumFinalFlag(tree);
				}
				// Imply STRICTFP if owner has STRICTFP set.
				implicit |= sym.owner.flags_field & STRICTFP;
				break;
			default:
				throw new AssertionError();
		}

		//mask��ֵ��ʾ������VAR��MTH��TYPǰ���������η��ļ���(��Flags���е�Modifier masks)
		//�����һ���ӿ�����������һ������:strictfp void methodA();
		//��Ϊmask = InterfaceMethodFlags = ABSTRACT | PUBLIC
		//���Ծͻᱨ��:�˴�������ʹ�����η� strictfp
		long illegal = flags & StandardFlags & ~mask;
        if (illegal != 0) {
			if ((illegal & INTERFACE) != 0) {
				log.error(pos, "intf.not.allowed.here");
				mask |= INTERFACE;
			}
			else {
				log.error(pos, "mod.not.allowed.here", TreeInfo.flagNames(illegal));
			}
		}
        else if ((sym.kind == TYP ||
		  // ISSUE: Disallowing abstract&private is no longer appropriate
		  // in the presence of inner classes. Should it be deleted here?
		  checkDisjoint(pos, flags,
				ABSTRACT,  //ABSTRACT��"PRIVATE,STATIC"����֮һ����ͬʱ����
				PRIVATE | STATIC))//��static abstract void methodC();�Ƿ������η���� abstract ��  static
		 && //�����checkDisjointͬ�ϣ����ǵ�������������ĸ����������������η�����ͬʱ������flags��
		 checkDisjoint(pos, flags,
			       ABSTRACT | INTERFACE,
			       FINAL | NATIVE | SYNCHRONIZED)
		 &&
                 checkDisjoint(pos, flags,
                               PUBLIC,
                               PRIVATE | PROTECTED)
		 &&
                 checkDisjoint(pos, flags,
                               PRIVATE,
                               PUBLIC | PROTECTED)
		 &&
		 checkDisjoint(pos, flags,
			       FINAL,
			       VOLATILE)
		 &&
		 (sym.kind == TYP ||
		  checkDisjoint(pos, flags,
				ABSTRACT | NATIVE,
				STRICTFP))) {
	    // skip
        }
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("mask="+Flags.toString(mask));
        DEBUG.P("implicit="+Flags.toString(implicit));
        DEBUG.P("returnFlags="+(flags & (mask | ~StandardFlags) | implicit)+"("+Flags.toString((flags & (mask | ~StandardFlags) | implicit))+")");
        DEBUG.P(0,this,"checkFlags(4)");
        return flags & (mask | ~StandardFlags) | implicit;
    }