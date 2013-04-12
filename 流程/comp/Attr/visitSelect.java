	public void visitSelect(JCFieldAccess tree) {
		/*************************************************************
		pkind��ʾ��ǰ�ڴ�tree.type.tsym��pkindָ��������
		����pkind=PCK���ͱ�ʾtree.type.tsym�������һ����(��:my.test)
		**************************************************************/
        // <editor-fold defaultstate="collapsed">
		try {
    	DEBUG.P(this,"visitSelect(1)");
    	DEBUG.P("tree.name="+tree.name);
		DEBUG.P("tree="+tree);
    	/*������Qualident = Ident { DOT Ident }�������﷨��
    	������һ��Ident�ǡ�this������super������class������ôǰ
    	һ��Ident�ķ�������(symbol kind)ֻ����TYP��Ҳ����˵ֻ��
    	������������ܸ���this������super������class����
    	
    	������һ��Ident����������PCK����ôǰһ��Ident�ķ�������
    	Ҳ��PCK����Ϊ����ǰ��ֻ���ǰ�����
    	
    	������һ��Ident����������TYP����ôǰһ��Ident�ķ�������
    	������TYP��PCK����Ϊ�������������ڲ��࣬��ʱǰһ��Ident
    	�ķ������;���TYP������ֻ����PCK��
    	
    	������һ��Ident����������VAL��MTH��Ҳ���ǵ�����
    	������Ǳ������ʽ(variables or non-variable expressions)
    	�����Ƿ�������ʱ����ôǰһ��Ident�ķ�������
    	������VAL��TYP��
    	*/
    	
        // Determine the expected kind of the qualifier expression.
        int skind = 0;
        if (tree.name == names._this || tree.name == names._super ||
            tree.name == names._class)
        {
            skind = TYP;
        } else {
            if ((pkind & PCK) != 0) skind = skind | PCK;
            if ((pkind & TYP) != 0) skind = skind | TYP | PCK;
			//ע��:���pkind=VAR����ô(pkind & (VAL | MTH)) != 0)�ǲ�����0��
			//��Ϊ(VAR & VAL)!=0;
			//DEBUG.P("(VAR & VAL)="+(VAR & VAL));
            if ((pkind & (VAL | MTH)) != 0) skind = skind | VAL | TYP;
        }

        // Attribute the qualifier expression, and determine its symbol (if any).
        Type site = attribTree(tree.selected, env, skind, Infer.anyPoly);//Infer.anyPoly��һ��Type(NONE, null)��JCNoType(NONE)����
        
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        DEBUG.P("pkind="+Kinds.toString(pkind));
        DEBUG.P("skind="+Kinds.toString(skind));
        if ((pkind & (PCK | TYP)) == 0)
            site = capture(site); // Capture field access

        // don't allow T.class T[].class, etc
        if (skind == TYP) {
            Type elt = site;
            while (elt.tag == ARRAY)
                elt = ((ArrayType)elt).elemtype;
            if (elt.tag == TYPEVAR) {
                log.error(tree.pos(), "type.var.cant.be.deref");
                result = syms.errType;

				//�Ҽ��ϵģ���if (tree.selected.type.tag == FORALL)��ע��
				tree.type = syms.errType;
                return;
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">

        // If qualifier symbol is a type or `super', assert `selectSuper'
        // for the selection. This is relevant for determining whether
        // protected symbols are accessible.
		DEBUG.P("tree.selected="+tree.selected);
        Symbol sitesym = TreeInfo.symbol(tree.selected);
        boolean selectSuperPrev = env.info.selectSuper;
        
        DEBUG.P("sitesym="+sitesym);
		if(sitesym==site.tsym)
			DEBUG.P("sitesym==site.tsym");
		else
			DEBUG.P("sitesym!=site.tsym");
        DEBUG.P("selectSuperPrev="+selectSuperPrev);
        
        env.info.selectSuper =
            sitesym != null &&
            sitesym.name == names._super;

        // If selected expression is polymorphic, strip
        // type parameters and remember in env.info.tvars, so that
        // they can be added later (in Attr.checkId and Infer.instantiateMethod).

		DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
		try {
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.selected="+tree.selected);
		DEBUG.P("tree.selected.type="+tree.selected.type);
		DEBUG.P("tree.selected.type.tag="+TypeTags.toString(tree.selected.type.tag));

		/*
		������NullPointerException
		������T t=T.super.toString();ʱ��
		�����skind = TYP���������"�޷������ͱ����н���ѡ��"�󷵻أ�
		����û�ж�(T.super)JCFieldAccess tree.type��ֵ��
		����tree.selected.type = null;
		*/
        if (tree.selected.type.tag == FORALL) {
            ForAll pstype = (ForAll)tree.selected.type;
            env.info.tvars = pstype.tvars;
            site = tree.selected.type = pstype.qtype;
        }

		} catch (RuntimeException e) {
			System.err.println("������:"+e);
			e.printStackTrace();
			throw e;
		}
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        // Determine the symbol represented by the selection.
        env.info.varArgs = false;
        Symbol sym = selectSym(tree, site, env, pt, pkind);
        
		DEBUG.P("tree="+tree);
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.type="+sym.type);
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
        DEBUG.P("sym.exists()="+sym.exists());
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("pkind="+Kinds.toString(pkind));
        
        if (sym.exists() && !isType(sym) && (pkind & (PCK | TYP)) != 0) {
            site = capture(site);
            sym = selectSym(tree, site, env, pt, pkind);
        }
        boolean varArgs = env.info.varArgs;
        tree.sym = sym;
        
        DEBUG.P("env.info.varArgs="+env.info.varArgs);
        DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        if (site.tag == TYPEVAR && !isType(sym) && sym.kind != ERR)
            site = capture(site.getUpperBound());

        // If that symbol is a variable, ...
        if (sym.kind == VAR) {
            VarSymbol v = (VarSymbol)sym;

            // ..., evaluate its initializer, if it has one, and check for
            // illegal forward reference.
            checkInit(tree, env, v, true);

            // If we are expecting a variable (as opposed to a value), check
            // that the variable is assignable in the current environment.
            if (pkind == VAR)
                checkAssignable(tree.pos(), v, tree.selected, env);
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("sitesym="+sitesym);
        if(sitesym!=null) DEBUG.P("sitesym.kind="+Kinds.toString(sitesym.kind));
        
        // Disallow selecting a type from an expression
        if (isType(sym) && (sitesym==null || (sitesym.kind&(TYP|PCK)) == 0)) {
            tree.type = check(tree.selected, pt,
                              sitesym == null ? VAL : sitesym.kind, TYP|PCK, pt);
        }
        
        DEBUG.P("isType(sitesym)="+isType(sitesym));
        
        if (isType(sitesym)) {
        	DEBUG.P("sym.name="+sym.name);
            if (sym.name == names._this) {
                // If `C' is the currently compiled class, check that
                // C.this' does not appear in a call to a super(...)
                if (env.info.isSelfCall &&
                    site.tsym == env.enclClass.sym) {
                    chk.earlyRefError(tree.pos(), sym);
                }
            } else {
                // Check if type-qualified fields or methods are static (JLS)
				/*
					test\attr\VisitSelectTest.java:15: �޷������ͱ����н���ѡ��
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:15: �޷������ͱ����н���ѡ��
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:19: �޷��Ӿ�̬�����������÷Ǿ�̬ ���� b
							B b=T.b;
								 ^
					test\attr\VisitSelectTest.java:20: �޷��Ӿ�̬�����������÷Ǿ�̬ ���� b()
							B b2=T.b();
								  ^
					4 ����
					class A<T>{}
					class B {
						//int i;
						B b;
						B b(){ return new B(); }
						class b{}
					}
					public class VisitSelectTest<T extends B> extends A<T.b> {
						//A<T.i> al;

						//A<T.b> al;
						B b=T.b;
						B b2=T.b();
					}
				*/
                if ((sym.flags() & STATIC) == 0 &&
                    sym.name != names._super &&
                    (sym.kind == VAR || sym.kind == MTH)) {
                    rs.access(rs.new StaticError(sym),
                              tree.pos(), site, sym.name, true);
                }
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
        DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
        DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
        // If we are selecting an instance member via a `super', ...
        if (env.info.selectSuper && (sym.flags() & STATIC) == 0) {
			/*
				class ClassA<T>{
					void m(){};
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //site.isRaw()=true
				}


				abstract class ClassA{
					abstract void m();
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //�޷�ֱ�ӷ��� test.attr.ClassA �еĳ��� ����
				}
			*/
            // Check that super-qualified symbols are not abstract (JLS)
            rs.checkNonAbstract(tree.pos(), sym);

			DEBUG.P("site="+site);
			DEBUG.P("site.isRaw()="+site.isRaw());
            if (site.isRaw()) {
                // Determine argument types for site.
                Type site1 = types.asSuper(env.enclClass.sym.type, site.tsym);
                
				DEBUG.P("(site1 == site)="+(site1 == site));
				if (site1 != null) site = site1;
            }
        }

        env.info.selectSuper = selectSuperPrev;
        result = checkId(tree, site, sym, env, pkind, pt, varArgs);
        env.info.tvars = List.nil();
        
        
		}finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"visitSelect(1)");
        }
        // </editor-fold>
    }