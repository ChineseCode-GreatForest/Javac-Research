/*
 * @(#)Parser.java	1.103 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.parser;

import java.util.*;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import static com.sun.tools.javac.util.ListBuffer.lb;

import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.parser.Token.*;

/** The parser maps a token sequence into an abstract syntax
 *  tree. It operates by recursive descent, with code derived
 *  systematically from an LL(1) grammar. For efficiency reasons, an
 *  operator precedence scheme is used for parsing binary operation
 *  expressions.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Parser.java	1.103 07/03/21")
public class Parser {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Parser);//�Ҽ��ϵ�
	private void DEBUGPos(JCTree t) {//�Ҽ��ϵ�
		DEBUG.P("Tree.StartPos="+getStartPos(t));
		DEBUG.P("Tree.EndPos  ="+getEndPos(t));
		DEBUG.P("errorEndPos  ="+errorEndPos);
	}
	
    /** A factory for creating parsers. */
    public static class Factory {
        /** The context key for the parser factory. */
        protected static final Context.Key<Parser.Factory> parserFactoryKey =
            new Context.Key<Parser.Factory>();

        /** Get the Factory instance for this context. */
        public static Factory instance(Context context) {
            Factory instance = context.get(parserFactoryKey);
            if (instance == null)
                instance = new Factory(context);
            return instance;
        }

        final TreeMaker F;
        final Log log;
        final Keywords keywords;
        final Source source;
        final Name.Table names;
        final Options options;

        /** Create a new parser factory. */
        protected Factory(Context context) {
            DEBUG.P(this,"Factory(1)");
                
            context.put(parserFactoryKey, this);
            this.F = TreeMaker.instance(context);
            this.log = Log.instance(context);
            this.names = Name.Table.instance(context);
            this.keywords = Keywords.instance(context);
            this.source = Source.instance(context);
            this.options = Options.instance(context);
            
            DEBUG.P(0,this,"Factory(1)");
        }

        /**
         * Create a new Parser.
         * @param S Lexer for getting tokens while parsing
         * @param keepDocComments true if javadoc comments should be kept
         * @param genEndPos true if end positions should be generated
         */
        public Parser newParser(Lexer S, boolean keepDocComments, boolean genEndPos) {
            try {//�Ҽ��ϵ�
            DEBUG.P(this,"newParser(3)");
            DEBUG.P("keepDocComments="+keepDocComments);
            DEBUG.P("genEndPos="+genEndPos);
        	
            if (!genEndPos)
                return new Parser(this, S, keepDocComments);
            else
                return new EndPosParser(this, S, keepDocComments);
                
            }finally{//�Ҽ��ϵ�
            DEBUG.P(0,this,"newParser(3)");
            }
        }
    }
    
    /*Ϊʲô����10�أ���Ϊ��׺�������������
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     �պ���10������Ҫ������������׺�������ɵı��ʽʱ
     ֻҪ����һ������Ϊ10+1(0�������������ڱ�������־)
     ����������ʾһ����ջ�ռ�����Դ������ⳤ�ȵ���׺��������ʽ��
     
     ��ο���������е�newOdStack()��newOpStack()��term2Rest()
     */
    /** The number of precedence levels of infix operators.
     */
    private static final int infixPrecedenceLevels = 10;

    /** The scanner used for lexical analysis.
     */
    private Lexer S;

    /** The factory to be used for abstract syntax tree construction.
     */
    protected TreeMaker F;

    /** The log to be used for error diagnostics.
     */
    private Log log;

    /** The keyword table. */
    private Keywords keywords;

    /** The Source language setting. */
    private Source source;

    /** The name table. */
    private Name.Table names;

    /** Construct a parser from a given scanner, tree factory and log.
     */
    protected Parser(Factory fac,
                     Lexer S,
                     boolean keepDocComments) {
        DEBUG.P(this,"Parser(3)");
        this.S = S;
        S.nextToken(); // prime the pump
        this.F = fac.F;
        this.log = fac.log;
        this.names = fac.names;
        this.keywords = fac.keywords;
        this.source = fac.source;
        Options options = fac.options;//������俴������ʲô�ã�
        this.allowGenerics = source.allowGenerics();
        this.allowVarargs = source.allowVarargs();
        this.allowAsserts = source.allowAsserts();
        this.allowEnums = source.allowEnums();
        this.allowForeach = source.allowForeach();
        this.allowStaticImport = source.allowStaticImport();
        this.allowAnnotations = source.allowAnnotations();
        this.keepDocComments = keepDocComments;
        if (keepDocComments) docComments = new HashMap<JCTree,String>();
        this.errorTree = F.Erroneous();
        DEBUG.P(0,this,"Parser(3)");
    }

    /** Switch: Should generics be recognized?
     */
    boolean allowGenerics;

    /** Switch: Should varargs be recognized?
     */
    boolean allowVarargs;

    /** Switch: should we recognize assert statements, or just give a warning?
     */
    boolean allowAsserts;

    /** Switch: should we recognize enums, or just give a warning?
     */
    boolean allowEnums;

    /** Switch: should we recognize foreach?
     */
    boolean allowForeach;

    /** Switch: should we recognize foreach? 
     */
    //Ӧ��:Switch: should we recognize static import? 
    boolean allowStaticImport;

    /** Switch: should we recognize annotations?
     */
    boolean allowAnnotations;

    /** Switch: should we keep docComments?
     */
    boolean keepDocComments;

    /** When terms are parsed, the mode determines which is expected:
     *     mode = EXPR        : an expression
     *     mode = TYPE        : a type
     *     mode = NOPARAMS    : no parameters allowed for type
     *     mode = TYPEARG     : type argument
     */
    static final int EXPR = 1;
    static final int TYPE = 2;
    static final int NOPARAMS = 4;
    static final int TYPEARG = 8;

    /** The current mode.
     */
    private int mode = 0;
    
    //�������Ҽ��ϵģ�������;
    public static String myMode(int m) {
        StringBuffer buf = new StringBuffer();
        if ((m&EXPR) != 0) buf.append("EXPR ");
        if ((m&TYPE) != 0) buf.append("TYPE ");
        if ((m&NOPARAMS) != 0) buf.append("NOPARAMS ");
        if ((m&TYPEARG) != 0) buf.append("TYPEARG ");
        
        if(buf.length()==0) buf.append(m);
        return buf.toString();
    }

    /** The mode of the term that was parsed last.
     */
    private int lastmode = 0;

/* ---------- error recovery -------------- */

    private JCErroneous errorTree;

    //ʲôʱ��õ�������������Ӵ����лָ��أ���S.pos() <= errorEndPos������
    //��ʲôʱ����ж�S.pos() <= errorEndPos����errorEndPos�п��ܸı���
    /** Skip forward until a suitable stop token is found.
     */
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"skip(4)");
		DEBUG.P("stopAtImport    ="+stopAtImport);
		DEBUG.P("stopAtMemberDecl="+stopAtMemberDecl);
		DEBUG.P("stopAtIdentifier="+stopAtIdentifier);
		DEBUG.P("stopAtStatement ="+stopAtStatement);

		while (true) {
			switch (S.token()) {
				case SEMI:
                    S.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                	//���֮ǰ�Ĵ������ڷ���import���ʱ���ֵ�,�������ɴ�
                	//nextToken()���ҵ����µĽ�IMPORT��token��˵���ҵ���
                	//һ���µ�import��䣬���ھͿ�������������
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
					if (stopAtIdentifier)
						return;
					break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            S.nextToken();
        }
        
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"skip(4)");
		}
    }

    private JCErroneous syntaxError(int pos, String key, Object... arg) {
	    try {//�Ҽ��ϵ�
	    DEBUG.P(this,"syntaxError(3)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);

        return syntaxError(pos, null, key, arg);

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"syntaxError(3)");
		}
    }

    private JCErroneous syntaxError(int pos, List<JCTree> errs, String key, Object... arg) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"syntaxError(4)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);
	    DEBUG.P("errs="+errs);

		setErrorEndPos(pos);
        reportSyntaxError(pos, key, arg);
        return toP(F.at(pos).Erroneous(errs));

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"syntaxError(4)");
		}
    }

    private int errorPos = Position.NOPOS;
    /**
     * Report a syntax error at given position using the given
     * argument unless one was already reported at the same position.
     */
    private void reportSyntaxError(int pos, String key, Object... arg) {
	    DEBUG.P(this,"reportSyntaxError(3)");
    	DEBUG.P("pos="+pos);
    	DEBUG.P("S.errPos()="+S.errPos());
		DEBUG.P("S.token()="+S.token());

        if (pos > S.errPos() || pos == Position.NOPOS) {
            if (S.token() == EOF)
                log.error(pos, "premature.eof");
            else
                log.error(pos, key, arg);
        }
        S.errPos(pos);

		DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("S.pos()="+S.pos());
		
		//��:Class c=int[][].char;
        if (S.pos() == errorPos)
            S.nextToken(); // guarantee progress
        errorPos = S.pos();

		DEBUG.P(0,this,"reportSyntaxError(3)");
    }


    /** Generate a syntax error at current position unless one was already
     *  reported at the same position.
     */
    private JCErroneous syntaxError(String key) {
        return syntaxError(S.pos(), key); //����syntaxError(int pos, String key, Object... arg)
    }

    /** Generate a syntax error at current position unless one was
     *  already reported at the same position.
     */
    private JCErroneous syntaxError(String key, String arg) {
        return syntaxError(S.pos(), key, arg);
    }
    // <editor-fold defaultstate="collapsed">//
    /*����:(���о�accept(1)��skip(4)�����Ĺ��������а���)
    
    ���﷨�����Դ����:
    package my.test k
	import java.util.ArrayList;
	
	���������ʾ:
    bin\mysrc\my\test\Test3.java:1: ��Ҫ ';'
	package my.test k
	               ^
	1 ����
	
	���ִ�ӡ������:
	nextToken(11,15)=|test|  tokenName=|IDENTIFIER|  prevEndPos=11
	com.sun.tools.javac.parser.Parser===>ident()
	-------------------------------------------------------------------------
	ident.name=test
	processWhitespace(15,16)=| |
	nextToken(16,17)=|k|  tokenName=|IDENTIFIER|  prevEndPos=15
	com.sun.tools.javac.parser.Parser===>ident()  END
	-------------------------------------------------------------------------
	qualident=my.test
	com.sun.tools.javac.parser.Parser===>qualident()  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.parser.Parser===>accept(1)
	-------------------------------------------------------------------------
	accToken=SEMI
	curToken=IDENTIFIER
	com.sun.tools.javac.parser.Parser===>accept(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.parser.Parser===>skip(4)
	-------------------------------------------------------------------------
	stopAtImport    =true
	stopAtMemberDecl=false
	stopAtIdentifier=false
	stopAtStatement =false
	processTerminator(17,18)=|
	|
	nextToken(18,24)=|import|  tokenName=|IMPORT|  prevEndPos=17
	com.sun.tools.javac.parser.Parser===>skip(4)  END
	-------------------------------------------------------------------------
	*/
	// </editor-fold>
    /** If next input token matches given token, skip it, otherwise report
     *  an error.
     */
    public void accept(Token token) {
    	DEBUG.P(this,"accept(1)");
    	DEBUG.P("accToken="+token);
    	DEBUG.P("curToken="+S.token());
        if (S.token() == token) {
            S.nextToken();
        } else {
            setErrorEndPos(S.pos());
            reportSyntaxError(S.prevEndPos(), "expected", keywords.token2string(token));
        }
        DEBUG.P(0,this,"accept(1)");
    }

    /** Report an illegal start of expression/type error at given position.
     */
    JCExpression illegal(int pos) {
        setErrorEndPos(S.pos());
        if ((mode & EXPR) != 0)
            return syntaxError(pos, "illegal.start.of.expr");
        else
            return syntaxError(pos, "illegal.start.of.type");

    }

    /** Report an illegal start of expression/type error at current position.
     */
    JCExpression illegal() {
        return illegal(S.pos());
    }

    /** Diagnose a modifier flag from the set, if any. */
    void checkNoMods(long mods) {
    	DEBUG.P(this,"checkNoMods(long mods)");
    	DEBUG.P("mods="+Flags.toString(mods).trim());
    	
        if (mods != 0) {
            /*
            ֻȡmods��׷�0λ,����λ����0:
            for(int mods=1;mods<6;mods++) {
                System.out.println("ʮ����: "+mods+" & -"+mods+" = "+(mods & -mods));
                System.out.println("������: "+Integer.toBinaryString(mods)+" & "+Integer.toBinaryString(-mods)+" = "+Integer.toBinaryString(mods & -mods));
                System.out.println();
            }
            ���:(��Ϊ�෴�������������ɰ�����λȡ����1����ԭ��õ��Է�)
            ʮ����: 1 & -1 = 1
            ������: 1 & 11111111111111111111111111111111 = 1

            ʮ����: 2 & -2 = 2
            ������: 10 & 11111111111111111111111111111110 = 10

            ʮ����: 3 & -3 = 1
            ������: 11 & 11111111111111111111111111111101 = 1

            ʮ����: 4 & -4 = 4
            ������: 100 & 11111111111111111111111111111100 = 100

            ʮ����: 5 & -5 = 1
            ������: 101 & 11111111111111111111111111111011 = 1
            */
            long lowestMod = mods & -mods;
            DEBUG.P("lowestMod="+Flags.toString(lowestMod).trim());
            log.error(S.pos(), "mod.not.allowed.here",
                      Flags.toString(lowestMod).trim());
        }
        DEBUG.P(0,this,"checkNoMods(long mods)");
    }

/* ---------- doc comments --------- */

    /** A hashtable to store all documentation comments
     *  indexed by the tree nodes they refer to.
     *  defined only if option flag keepDocComment is set.
     */
    Map<JCTree, String> docComments;

    /** Make an entry into docComments hashtable,
     *  provided flag keepDocComments is set and given doc comment is non-null.
     *  @param tree   The tree to be used as index in the hashtable
     *  @param dc     The doc comment to associate with the tree, or null.
     */
    void attach(JCTree tree, String dc) {
        if (keepDocComments && dc != null) {
//          System.out.println("doc comment = ");System.out.println(dc);//DEBUG
            docComments.put(tree, dc);
        }
    }

/* -------- source positions ------- */

    private int errorEndPos = -1;

    private void setErrorEndPos(int errPos) {
	    DEBUG.P(this,"setErrorEndPos(1)");
	    DEBUG.P("errPos="+errPos);
	    DEBUG.P("errorEndPos="+errorEndPos);

        if (errPos > errorEndPos)
            errorEndPos = errPos;

		DEBUG.P(0,this,"setErrorEndPos(1)");
    }

    protected int getErrorEndPos() {
        return errorEndPos;
    }

    /**
     * Store ending position for a tree.
     * @param tree   The tree.
     * @param endpos The ending position to associate with the tree.
     */
    protected void storeEnd(JCTree tree, int endpos) {}

    /**
     * Store ending position for a tree.  The ending position should
     * be the ending position of the current token.
     * @param t The tree.
     */
    protected <T extends JCTree> T to(T t) { return t; }

    /**
     * Store ending position for a tree.  The ending position should
     * be greater of the ending position of the previous token and errorEndPos.
     * @param t The tree.
     */
    protected <T extends JCTree> T toP(T t) { return t; }

    /** Get the start position for a tree node.  The start position is
     * defined to be the position of the first character of the first
     * token of the node's source text.
     * @param tree  The tree node
     */
    public int getStartPos(JCTree tree) {
        return TreeInfo.getStartPos(tree);
    }

    /**
     * Get the end position for a tree node.  The end position is
     * defined to be the position of the last character of the last
     * token of the node's source text.  Returns Position.NOPOS if end
     * positions are not generated or the position is otherwise not
     * found.
     * @param tree  The tree node
     */
    public int getEndPos(JCTree tree) {
        return Position.NOPOS;
    }



/* ---------- parsing -------------- */

    /**
     * Ident = IDENTIFIER
     */
    Name ident() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"ident()");
		
        if (S.token() == IDENTIFIER) {
            Name name = S.name();
            DEBUG.P("ident.name="+name);
            S.nextToken();
            return name;
        } else if (S.token() == ASSERT) {
            if (allowAsserts) {
            	/*
            	��:
                F:\Javac\bin\other>javac Test5.java
                Test5.java:4: �Ӱ汾 1.4 ��ʼ��'assert' ��һ���ؼ��֣�������������ʶ��
                ����ʹ�� -source 1.3 ����Ͱ汾�Ա㽫 'assert' ������ʶ����
                        int assert=0;
                            ^
                1 ����
                */
                log.error(S.pos(), "assert.as.identifier");
                S.nextToken();
                return names.error;//error��com.sun.tools.javac.util.Name.Table�ж���
            } else {
            	/*
            	��:
            	F:\Javac\bin\other>javac -source 1.3 Test5.java
                Test5.java:4: ���棺�Ӱ汾 1.4 ��ʼ��'assert' ��һ���ؼ��֣�������������ʶ��
                ����ʹ�� -source 1.4 ����߰汾�Ա㽫 'assert' �����ؼ��֣�
                                int assert=0;
                                    ^
                1 ����
                */
                log.warning(S.pos(), "assert.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else if (S.token() == ENUM) {
        	//��ASSERT����
            if (allowEnums) {
                log.error(S.pos(), "enum.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "enum.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else {
            accept(IDENTIFIER);
            return names.error;
        }

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"ident()");
		}        
	}

    /**
     * Qualident = Ident { DOT Ident }
     */
    public JCExpression qualident() {
    	DEBUG.P(this,"qualident()");
    	//ע����������F.at(S.pos())��Ȼ���ٵ���ident()
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
		DEBUGPos(t);
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            
            /*
            //�õ�ǰpos����TreeMaker���pos,Ȼ������һ��JCFieldAccess��
            //�����ɵ�JCFieldAccessʵ����TreeMaker���pos�����Լ���pos
            //JCFieldAccess��Ident��������Ƕ��
            
            //�統Qualident =java.lang.Byteʱ��ʾΪ:
            JCFieldAccess {
            	Name name = "Byte";
            	JCExpression selected = {
            		JCFieldAccess {
            			Name name="lang";
            			JCExpression selected = {
				            JCIdent {
				            	Name name = "java";
				            }
				        }
				    }
				}
			}
			*/
			//DEBUG.P("pos="+pos);//�����pos��"."�ŵĿ�ʼλ��
            t = toP(F.at(pos).Select(t, ident()));
			//DEBUGPos(t);//������������Ŀ�ʼλ�����ǵ�һ��ident�Ŀ�ʼλ��
        }
        
        DEBUG.P("qualident="+t);
		DEBUGPos(t);
        DEBUG.P(0,this,"qualident()");
        return t;
    }

    /**
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */

     //Ϊʲôû��byte,short�أ���Ϊ��Scanner�з������ֻ��ַ��������������������ģ�
     //ֻ�ǵ���������ֵ����������û��byte,short����������ֵ(LITERAL)
    JCExpression literal(Name prefix) {
    	DEBUG.P(this,"literal(Name prefix)");
    	DEBUG.P("prefix="+prefix);
    	//prefix��ָ��������(Literal)��ǰ׺,���Ƿ������(-)
    	
        int pos = S.pos();
        JCExpression t = errorTree;

        switch (S.token()) {
        case INTLITERAL:
            try {
            	//��ȫ�޶�����:com.sun.tools.javac.code.TypeTags
            	//��ȫ�޶�����:com.sun.tools.javac.util.Convert
                t = F.at(pos).Literal(
                    TypeTags.INT,
                    Convert.string2int(strval(prefix), S.radix()));
            } catch (NumberFormatException ex) {
            	/*��������:
            	bin\mysrc\my\test\Test3.java:29: ����������� 099
		        public final int c=099;
		                           ^
		        */                   
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case LONGLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.LONG,
                    new Long(Convert.string2long(strval(prefix), S.radix())));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case FLOATLITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Float n;
            try {
				//�����ʷ�������proper����ĸ�������ʽ�϶�����ȷ�ģ�
				//���Ǵʷ�����ʱ����֪����������ֵ�Ƿ��С���ǹ���
				//�����С����ôFloat.valueOf(proper)���Ƿ���0.0f��
				//����������0.0f�޷����֣�����������ͨ��!isZero(proper)���жϣ�
				//���proper("0x"����)�е�ÿ���ַ�ֻҪ��һ������0��'.'�ţ�
				//��һ���ǹ�С�ĸ�����
				//���⣬���ڹ���ĸ�������Float.valueOf(proper)���Ƿ���Float.POSITIVE_INFINITY
                n = Float.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already repoted in scanner
                n = Float.NaN;
            }
            if (n.floatValue() == 0.0f && !isZero(proper)) //��:float f1=1.1E-33333f;
                log.error(S.pos(), "fp.number.too.small");
            else if (n.floatValue() == Float.POSITIVE_INFINITY) //��:float f2=1.1E+33333f;
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.FLOAT, n);
            break;
        }
        case DOUBLELITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Double n;
            try {
                n = Double.valueOf(proper); //ͬ��
            } catch (NumberFormatException ex) {
                // error already reported in scanner
                n = Double.NaN;
            }
            if (n.doubleValue() == 0.0d && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.doubleValue() == Double.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.DOUBLE, n);
            break;
        }
        case CHARLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CHAR,
                S.stringVal().charAt(0) + 0); //ע������ַ�ת��������,Literal�������յ���Integer����
            break;
        case STRINGLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CLASS,
                S.stringVal());
            break;
        case TRUE: case FALSE:
            t = F.at(pos).Literal(
                TypeTags.BOOLEAN,
                (S.token() == TRUE ? 1 : 0));
            break;
        case NULL:
            t = F.at(pos).Literal(
                TypeTags.BOT,
                null);
            break;
        default:
            assert false;
        }
        if (t == errorTree)
            t = F.at(pos).Erroneous();
        storeEnd(t, S.endPos());
        S.nextToken();
        
        DEBUG.P("return t="+t);
        DEBUG.P(0,this,"literal(Name prefix)");
        return t;
    }
//where
        boolean isZero(String s) {
            char[] cs = s.toCharArray();
            int base = ((Character.toLowerCase(s.charAt(1)) == 'x') ? 16 : 10);
            int i = ((base==16) ? 2 : 0);
            while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) i++;
            return !(i < cs.length && (Character.digit(cs[i], base) > 0));
        }

        String strval(Name prefix) {
        	//��������(Literal)��Scanner�б�����
        	//�ַ����������ʱ�����ַ�������
            String s = S.stringVal();
            return (prefix.len == 0) ? s : prefix + s;
        }

    /** terms can be either expressions or types.
     */
    public JCExpression expression() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"expression()");
		
        return term(EXPR);

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"expression()");
		}        
    }

    public JCExpression type() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"type()");

        return term(TYPE);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"type()");
		}
    }

    JCExpression term(int newmode) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term(int newmode)");
		DEBUG.P("newmode="+myMode(newmode)+"  mode="+myMode(mode));
		
        int prevmode = mode;
        mode = newmode;
        JCExpression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term(int newmode)");
		}
    }
    
    /*
    ���ղ��������ȼ��ӵ͵��ߵ�˳������������﷨��ÿ�γ��ֵ�һ�Ѳ�����
    ���������ȼ��ο�<<core java ��I) p47
    
    �﷨��ÿ�����ս���ʹ���һ�������������ĵ��ô�������˲����������ȼ�
    �縳ֵ�����AssignmentOperator�����ȼ���ף��������ŵ���termRest����
    */
    
    /**
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    JCExpression term() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term()");
		
        JCExpression t = term1();   
        /*
        ����"="֮������и�ֵ�������Token.java�еĶ���˳������:
        PLUSEQ("+="),
	    SUBEQ("-="),
	    STAREQ("*="),
	    SLASHEQ("/="),
	    AMPEQ("&="),
	    BAREQ("|="),
	    CARETEQ("^="),
	    PERCENTEQ("%="),
	    LTLTEQ("<<="),
	    GTGTEQ(">>="),
	    GTGTGTEQ(">>>="),
	    
	    ���PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0
	    ��ʾS.token()����������Token֮һ��
        
        PLUSEQ.compareTo(S.token()) <= 0��ʾPLUSEQ.ordinal<=S.token().ordinal
        compareTo()������java.lang.Enum<E>����,����:
        public final int compareTo(E o) {
		Enum other = (Enum)o;
		Enum self = this;
		............
		return self.ordinal - other.ordinal;
	    }
        */
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        //���if����Ϊtrue˵����һ����ֵ���ʽ���
        if ((mode & EXPR) != 0 &&
            S.token() == EQ || PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0)
            return termRest(t);
        else
            return t;
            
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term()");
		}    
    }

    JCExpression termRest(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"termRest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        switch (S.token()) {
        case EQ: {
            int pos = S.pos();
            S.nextToken();
            mode = EXPR;
            /*ע��������term()��������term1()�������﷨:
            Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			�о�Ӧ��term1()�Ŷԣ���Ϊjava����������a=b=c=d�������﷨,
			���԰�ExpressionRest = [AssignmentOperator Expression1]
			����  ExpressionRest = [AssignmentOperator Expression]
			����ֱ��������һ���﷨:
			Expression = Expression1 {AssignmentOperator Expression1}
			�滻
			Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			�����ַ�ʽ����ԭ���ĺ����
			
			������
			Java Language Specification, Third Edition
			18.1. The Grammar of the Java Programming Language
			�еĶ�������:
			   Expression:
      		   Expression1 [AssignmentOperator Expression1]]
      		   
      		��]]���е�Ī�������֪���ǲ��Ƕ���˸���]��
			*/
            JCExpression t1 = term();
            return toP(F.at(pos).Assign(t, t1));
        }
        case PLUSEQ:
        case SUBEQ:
        case STAREQ:
        case SLASHEQ:
        case PERCENTEQ:
        case AMPEQ:
        case BAREQ:
        case CARETEQ:
        case LTLTEQ:
        case GTGTEQ:
        case GTGTGTEQ:
            int pos = S.pos();
            Token token = S.token();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term(); //ͬ��
            return F.at(pos).Assignop(optag(token), t, t1);
        default:
            return t;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"termRest(JCExpression t)");
		}  
    }

    /** Expression1   = Expression2 [Expression1Rest]
     *  Type1         = Type2
     *  TypeNoParams1 = TypeNoParams2
     */
    JCExpression term1() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term1()");
        JCExpression t = term2();
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        if ((mode & EXPR) != 0 && S.token() == QUES) {
            mode = EXPR;
            return term1Rest(t);
        } else {
            return t;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term1()");
		}
    }

    /** Expression1Rest = ["?" Expression ":" Expression1]
     */
    JCExpression term1Rest(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term1Rest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == QUES) {
            int pos = S.pos();
            DEBUG.P("pos="+pos);
            S.nextToken();
            JCExpression t1 = term();
            accept(COLON);
            
            //����condition ? trueExpression : falseExpression���
            //��������Կ���falseExpression���ܺ��и�ֵ�����AssignmentOperator
            //����trueExpression����
            JCExpression t2 = term1();
            
            //JCConditional��pos��QUES��pos,������t��pos
            return F.at(pos).Conditional(t, t1, t2);
        } else {
            return t;
        }
             
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term1Rest(JCExpression t)");
		}
    }

    /** Expression2   = Expression3 [Expression2Rest]
     *  Type2         = Type3
     *  TypeNoParams2 = TypeNoParams3
     */
    JCExpression term2() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term2()");
        JCExpression t = term3();
        
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
		
		//��ǰ����������ȼ�>=��||������������ȼ�ʱ���ŵ���term2Rest
        if ((mode & EXPR) != 0 && prec(S.token()) >= TreeInfo.orPrec) {
            mode = EXPR;
            return term2Rest(t, TreeInfo.orPrec);
        } else {
            return t;
        }
        
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term2()");
		}        
    }
    
    //instanceof������ͱȽ������("<" | ">" | "<=" | ">=")�����ȼ�һ��
    
    /*  Expression2Rest = {infixop Expression3}
     *                  | Expression3 instanceof Type
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     */
    JCExpression term2Rest(JCExpression t, int minprec) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term2Rest(JCExpression t, int minprec)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		//DEBUG.P("odStackSupply.size="+odStackSupply.size());
		//DEBUG.P("opStackSupply.size="+opStackSupply.size());
		
		//odStackָ��odStackSupply.elems.head
        //odStackSupply.elems������
        List<JCExpression[]> savedOd = odStackSupply.elems;
		//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		//DEBUG.P("savedOd.size="+savedOd.size());
		//DEBUG.P("savedOd="+savedOd);
        JCExpression[] odStack = newOdStack();


        List<Token[]> savedOp = opStackSupply.elems;
		//DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		//DEBUG.P("savedOp.size="+savedOp.size());
		//DEBUG.P("savedOp="+savedOp);
        Token[] opStack = newOpStack();

		/*
		DEBUG.P(1);
		DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		DEBUG.P("savedOd.size="+savedOd.size());
		DEBUG.P("savedOd="+savedOd);
		DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		DEBUG.P("savedOp.size="+savedOp.size());
		DEBUG.P("savedOp="+savedOp);
		*/

        // optimization, was odStack = new Tree[...]; opStack = new Tree[...];
        int top = 0;
        odStack[0] = t;
        int startPos = S.pos();
        Token topOp = ERROR;
        while (prec(S.token()) >= minprec) {
        	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            opStack[top] = topOp;
            top++;
            topOp = S.token();
            int pos = S.pos();
            S.nextToken();
            odStack[top] = topOp == INSTANCEOF ? type() : term3();
            //for(int i=0;i<odStack.length;i++) {
			for(int i=0;i<=top;i++) {
            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
            }
            for(int i=0;i<=top;i++) {
            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
            }
            //ֻҪǰһ������������ȼ�>=���ӵ�����������ȼ�
            //�����Ϲ鲢����:1+2+4*5,���ȹ鲢1+2��������4*5
            //�����(1+2)+(4*5)
            while (top > 0 && prec(topOp) >= prec(S.token())) {
            	DEBUG.P("pos="+pos);//�����pos��topOp��pos
            	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            	//DEBUG.P("odStack[top-1]="+odStack[top-1]);
            	//DEBUG.P("odStack[top]="+odStack[top]);
                odStack[top-1] = makeOp(pos, topOp, odStack[top-1],
                                        odStack[top]);
                top--;
                topOp = opStack[top];
                DEBUG.P("topOp="+topOp+" S.token()="+S.token());
                
                for(int i=0;i<=top;i++) {
	            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
	            }
	            for(int i=0;i<=top;i++) {
	            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
	            }
            }
        }
        assert top == 0;
        /*
        odStack[0]�������Binary���ʽ�������(opcode)�����ȼ�
        ����������������ʽ����С���ұߵ��Ǹ�
        
        ��a || 1<=2 && 3<=4����odStack[0].opcode=||
        ������������:
        ----------------------------
        t=a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=a
		t.rhs=1 <= 2 && 3 <= 4
        
        
        ����1+2>0 || a || 1<=2 && 3<=4,��odStack[0].opcode���ǵ���||
        ������������:
        ----------------------------
        t=1 + 2 > 0 || a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=1 + 2 > 0 || a
		t.rhs=1 <= 2 && 3 <= 4
		*/
        t = odStack[0];
		DEBUG.P(1);
        DEBUG.P("t="+t);
        DEBUG.P("t.tag="+t.getKind());
        if(t instanceof JCBinary) {
			DEBUG.P("t.lhs="+((JCBinary)t).lhs);
			DEBUG.P("t.rhs="+((JCBinary)t).rhs);
        }
        
        if (t.tag == JCTree.PLUS) {
            StringBuffer buf = foldStrings(t);
            DEBUG.P("buf="+buf);
            if (buf != null) {
                t = toP(F.at(startPos).Literal(TypeTags.CLASS, buf.toString()));
            }
        }
        
        //�����ٴη����ջ�ռ�
        odStackSupply.elems = savedOd; // optimization
        opStackSupply.elems = savedOp; // optimization
        return t;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term2Rest(JCExpression t, int minprec)");
		} 
    }
//where
        /** Construct a binary or type test node.
         */
        private JCExpression makeOp(int pos,
                                    Token topOp,
                                    JCExpression od1,
                                    JCExpression od2)
        {
            if (topOp == INSTANCEOF) {
                return F.at(pos).TypeTest(od1, od2);
            } else {
                return F.at(pos).Binary(optag(topOp), od1, od2);
            }
        }
        /** If tree is a concatenation of string literals, replace it
         *  by a single literal representing the concatenated string.
         */
        protected StringBuffer foldStrings(JCTree tree) {
        	try {//�Ҽ��ϵ�
        	DEBUG.P(this,"foldStrings(JCTree tree");
        	DEBUG.P("tree="+tree);
       		DEBUG.P("tree.tag="+tree.getKind());
       		
            List<String> buf = List.nil();
            /*
            ֻ�б���е������ȫ�ǼӺ�(+)�������üӺ���������
            ��ÿ������ֵȫ�����ַ���ʱ���Ű�ÿ������ֵ�ַ����ϲ�������
            ���� "ab"+"cd"+"ef"+"gh":
            List<String> buf���ڲ��ṹ�����¹��̱仯:
            1. buf.prepend("gh") = "gh"
            2. buf.prepend("ef") = "ef"==>"gh"
            3. buf.prepend("cd") = "cd"==>"ef"==>"gh"
            
            Ȼ��StringBuffer sbuf = new StringBuffer("ab");
            sbuf.append("cd") = "abcd"
            sbuf.append("ef") = "abcdef"
            sbuf.append("gh") = "abcdefgh"
            
            ��󷵻�:"abcdefgh"��
            
            ����"ab"+"cd"+"ef"+1 �� 1+"cd"+"ef"+"gh"
                                 �� "ab"+1*2+"cd"+"ef"+"gh"
            ��������null

			ע��:String str="A"+"B"+'c';Ҳ����null����Ϊ'c'���ַ��������ַ���
			��str="A"+"B"+"c";�ͷ���ABc
            */
            
            while (true) {
                if (tree.tag == JCTree.LITERAL) { //����ߵ��ַ���
                    JCLiteral lit = (JCLiteral) tree;
                    if (lit.typetag == TypeTags.CLASS) {
                        StringBuffer sbuf =
                            new StringBuffer((String)lit.value);
                        while (buf.nonEmpty()) {
                            sbuf.append(buf.head);
                            buf = buf.tail;
                        }
                        return sbuf;
                    }
                } else if (tree.tag == JCTree.PLUS) {
                    JCBinary op = (JCBinary)tree;
                    DEBUG.P("op.rhs.tag="+op.rhs.getKind());
                    if (op.rhs.tag == JCTree.LITERAL) {
                        JCLiteral lit = (JCLiteral) op.rhs;
                        if (lit.typetag == TypeTags.CLASS) {
                            buf = buf.prepend((String) lit.value);
                            tree = op.lhs;
                            continue;
                        }
                    }
                }
                return null;
            }
	        
	        }finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"foldStrings(JCTree tree");
			}
        }

        /** optimization: To save allocating a new operand/operator stack
         *  for every binary operation, we use supplys.
         */
		//odStackSupply.size()��opStackSupply.size() = ���ʽ�е����Ŷ���+1
		//����ʽ:a=a*(b+a)����ôodStackSupply.size() = opStackSupply.size() = 2
        ListBuffer<JCExpression[]> odStackSupply = new ListBuffer<JCExpression[]>();
        ListBuffer<Token[]> opStackSupply = new ListBuffer<Token[]>();

        private JCExpression[] newOdStack() {
			//DEBUG.P(this,"newOdStack()");
			//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
			//DEBUG.P("odStackSupply.last="+odStackSupply.last);
			//DEBUG.P("if (odStackSupply.elems == odStackSupply.last)="+(odStackSupply.elems == odStackSupply.last));

            if (odStackSupply.elems == odStackSupply.last)
                odStackSupply.append(new JCExpression[infixPrecedenceLevels + 1]);
            JCExpression[] odStack = odStackSupply.elems.head;
            odStackSupply.elems = odStackSupply.elems.tail;
            return odStack;
        }

        private Token[] newOpStack() {
            if (opStackSupply.elems == opStackSupply.last)
                opStackSupply.append(new Token[infixPrecedenceLevels + 1]);
            Token[] opStack = opStackSupply.elems.head;
            opStackSupply.elems = opStackSupply.elems.tail;
            return opStack;
        }
    //�����Expr��ָExpression(�ο�18.1. The Grammar of the Java Programming Language)
    /** Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeArguments] THIS [Arguments]
     *                 | [TypeArguments] SUPER SuperSuffix
     *                 | NEW [TypeArguments] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | [TypeArguments] SUPER Arguments | NEW [TypeArguments] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeArguments] {TypeSelector} BracketsOpt
     *                 | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeArguments] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeArguments] SUPER SuperSuffix
     *                 | "." NEW [TypeArguments] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeArguments]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    protected JCExpression term3() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"term3()");

        int pos = S.pos();
        JCExpression t;
        List<JCExpression> typeArgs = typeArgumentsOpt(EXPR);

        switch (S.token()) {
        case QUES: //TypeArguments���������� expr=<?>
        	DEBUG.P("case QUES:");
			//��: ClassB<?> c=(ClassB<?>)cb;(��:case LPAREN)
            if ((mode & TYPE) != 0 && (mode & (TYPEARG|NOPARAMS)) == TYPEARG) {
                mode = TYPE;
                return typeArgument();
            } else
                return illegal();
                
                
        /*
        ���ʽ�������: ++��--��BANG("!")��TILDE("~")��+��-  ��ʼ,
        �⼸�����������һԪ�������������Ĵ��롰t = term3()������
        �������˳���Ǵ��ҵ����,��:++--myInt �൱��:++(--myInt)
        ++--myInt����������JCUnary��
        
        ���ǳ�ֵ��ע����ǲ�����++--myInt����++(--myInt)�������﷨ȴ��
        �����(������Parser�׶�û�з���):
        
        bin\mysrc\my\test\Test.java:98: ���������
		��Ҫ�� ����
		�ҵ��� ֵ
		                ++(--myInt);
		                   ^
		1 ����
        */
        case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
        	DEBUG.P("(case PrefixOp) mode="+myMode(mode));
            if (typeArgs == null && (mode & EXPR) != 0) {
                Token token = S.token();
                S.nextToken();
                mode = EXPR;
                if (token == SUB &&
                    (S.token() == INTLITERAL || S.token() == LONGLITERAL) &&
                    S.radix() == 10) {
                    mode = EXPR;
                    t = literal(names.hyphen);
                } else {
                    t = term3();
                    return F.at(pos).Unary(unoptag(token), t);
                }
            } else return illegal();
            break;
        case LPAREN:
        	DEBUG.P("case LPAREN:");
            if (typeArgs == null && (mode & EXPR) != 0) {
                S.nextToken();
                mode = EXPR | TYPE | NOPARAMS;
                t = term3();
				//��: ClassB<?> c=(ClassB<?>)cb;
                if ((mode & TYPE) != 0 && S.token() == LT) {
                    // Could be a cast to a parameterized type
                    int op = JCTree.LT;
                    int pos1 = S.pos();
                    S.nextToken();
                    mode &= (EXPR | TYPE);
                    mode |= TYPEARG;
                    JCExpression t1 = term3();
                    if ((mode & TYPE) != 0 &&
                        (S.token() == COMMA || S.token() == GT)) {
                        mode = TYPE;
                        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                        args.append(t1);
                        while (S.token() == COMMA) {
                            S.nextToken();
                            args.append(typeArgument());
                        }
                        accept(GT);
                        t = F.at(pos1).TypeApply(t, args.toList());
                        checkGenerics();
                        t = bracketsOpt(toP(t));
                    } else if ((mode & EXPR) != 0) {
                        mode = EXPR;
                        t = F.at(pos1).Binary(op, t, term2Rest(t1, TreeInfo.shiftPrec));
                        t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                    } else {
                        accept(GT);
                    }
                } else {
                    t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                }
                accept(RPAREN);
                lastmode = mode;
                mode = EXPR;
				DEBUG.P("lastmode="+myMode(lastmode));
                if ((lastmode & EXPR) == 0) {//�磺byte b=(byte)++i;
                    JCExpression t1 = term3();
                    return F.at(pos).TypeCast(t, t1);
                } else if ((lastmode & TYPE) != 0) {
                    switch (S.token()) {
                    /*case PLUSPLUS: case SUBSUB: */
                    case BANG: case TILDE:
                    case LPAREN: case THIS: case SUPER:
                    case INTLITERAL: case LONGLITERAL: case FLOATLITERAL:
                    case DOUBLELITERAL: case CHARLITERAL: case STRINGLITERAL:
                    case TRUE: case FALSE: case NULL:
                    case NEW: case IDENTIFIER: case ASSERT: case ENUM:
                    case BYTE: case SHORT: case CHAR: case INT:
                    case LONG: case FLOAT: case DOUBLE: case BOOLEAN: case VOID:
                        JCExpression t1 = term3();
                        return F.at(pos).TypeCast(t, t1);
                    }
                }
            } else return illegal();
            t = toP(F.at(pos).Parens(t));
            break;
        case THIS:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(F.at(pos).Ident(names._this));
                S.nextToken();
                if (typeArgs == null)
                    t = argumentsOpt(null, t);
                else
                    t = arguments(typeArgs, t);
                typeArgs = null;
            } else return illegal();
            break;
        case SUPER:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(superSuffix(typeArgs, F.at(pos).Ident(names._super)));
                typeArgs = null;
            } else return illegal();
            break;
        case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
        case CHARLITERAL: case STRINGLITERAL:
        case TRUE: case FALSE: case NULL:
            if (typeArgs == null && (mode & EXPR) != 0) {
                mode = EXPR;
                t = literal(names.empty);
            } else return illegal();
            break;
        case NEW:
            if (typeArgs != null) return illegal();
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                S.nextToken();
                if (S.token() == LT) typeArgs = typeArguments();
                t = creator(pos, typeArgs);
                typeArgs = null;
            } else return illegal();
            break;
        case IDENTIFIER: case ASSERT: case ENUM:
            if (typeArgs != null) return illegal();
            t = toP(F.at(S.pos()).Ident(ident()));
            loop: while (true) {
                pos = S.pos();
                switch (S.token()) {
                case LBRACKET:
                    S.nextToken();
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos).TypeArray(t));
                        t = bracketsSuffix(t);//��:Class c=ParserTest[][].class;
                    } else {
                        if ((mode & EXPR) != 0) {
							//��:{ int a1[]={1,2}, a2; a1[0]=3; a2=a1[1]; }
                            mode = EXPR;
                            JCExpression t1 = term();
                            DEBUG.P("(case IDENTIFIER LBRACKET) t="+t+" t1="+t1);
                            t = to(F.at(pos).Indexed(t, t1));
                        }
                        accept(RBRACKET);
                    }
                    break loop;
                case LPAREN:
                    if ((mode & EXPR) != 0) {
                        mode = EXPR;
						DEBUG.P("(case IDENTIFIER LPAREN) t="+t+" typeArgs="+typeArgs);
						/*��:
						static class MemberClassB {
							static <R> R methodA(R r) { return r; }
						}
						{ MemberClassB.methodA(this); }
						{ MemberClassB.methodA("str"); }
						{ MemberClassB.<ParserTest>methodA(this); }
						{ MemberClassB.<String>methodA("str"); }

						//���
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=ParserTest
						t=MemberClassB.methodA typeArgs=String
						*/
                        t = arguments(typeArgs, t);
                        typeArgs = null;
                    }
                    break loop;
                case DOT:
                    S.nextToken();
                    typeArgs = typeArgumentsOpt(EXPR);
                    if ((mode & EXPR) != 0) {
                        switch (S.token()) {
                        case CLASS:
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._class));
                            S.nextToken();
                            break loop;
                        case THIS:
							/*��
							class MemberClassC {
								{ ParserTest.this(); } //�д�
								{ ParserTest pt=ParserTest.this; } //��ȷ
							}
							*/
							DEBUG.P("(case IDENTIFIER THIS) t="+t+" typeArgs="+typeArgs);
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._this));
                            S.nextToken();
                            break loop;
                        case SUPER:
							DEBUG.P("(case IDENTIFIER SUPER) t="+t+" typeArgs="+typeArgs);
							/*��
							int superField;
							<T> ParserTest(T t){}
							static <T> void methodB(T t){}
							class MemberClassD extends ParserTest {
								MemberClassD() { <String>super("str"); }
								{ int sf=MemberClassD.super.superField; }
								{ MemberClassD.super.<String>methodB("str"); }
							}
							*/
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._super));
                            t = superSuffix(typeArgs, t);
                            typeArgs = null;
                            break loop;
                        case NEW:
							/*����
							class MemberClassE {
								class MemberClassF<T> {
									<T> MemberClassF(T t){}
								}
							}
							{
								MemberClassE me=new MemberClassE();
								MemberClassE.MemberClassF<Long> mf=me.new <String>MemberClassF<Long>("str");
								//���͵ĸ�ʽ����ȷ��ȱ��ĳЩ����(��Check���м��)
								//MemberClassE.MemberClassF mf=me.new <String>MemberClassF<Long>("str");
							}
							*/
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            int pos1 = S.pos();
                            S.nextToken();
                            if (S.token() == LT) typeArgs = typeArguments();
                            t = innerCreator(pos1, typeArgs, t);
                            typeArgs = null;
                            break loop;
                        }
                    }
                    // typeArgs saved for next loop iteration.
                    t = toP(F.at(pos).Select(t, ident()));
                    break;
                default:
                    break loop;
                }
            }
            if (typeArgs != null) illegal();
            t = typeArgumentsOpt(t);
            break;
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs != null) illegal();
            t = bracketsSuffix(bracketsOpt(basicType()));
            break;
        case VOID:
            if (typeArgs != null) illegal();
            if ((mode & EXPR) != 0) {
                S.nextToken();
                if (S.token() == DOT) {
                    JCPrimitiveTypeTree ti = toP(F.at(pos).TypeIdent(TypeTags.VOID));
                    t = bracketsSuffix(ti);
                } else {
                    return illegal(pos);
                }
            } else {
                return illegal();
            }
            break;
        default:
            return illegal();
        }
        if (typeArgs != null) illegal();
        while (true) { //��Ӧ{Selector}
            int pos1 = S.pos();
            if (S.token() == LBRACKET) {
                S.nextToken();
				DEBUG.P("mode="+myMode(mode));
                if ((mode & TYPE) != 0) {
                    int oldmode = mode;
                    mode = TYPE;
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos1).TypeArray(t));
                        return t;
                    }
                    mode = oldmode;
                }
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    JCExpression t1 = term();
					//�����������Ķ�ά����
					//int[][] ii2={{1,2},{3,4}};
					//int i2=ii2[1][2]; //��Ҫ�����
					//����case IDENTIFIER�д���ii2[1]����ת�����ﴦ��[2]
					//(while (true) t=ii2[1] t1=2
					//Indexed t=ii2[1][2]
					DEBUG.P("(while (true) t="+t+" t1="+t1);
                    t = to(F.at(pos1).Indexed(t, t1));
					DEBUG.P("Indexed t="+t);
                }
                accept(RBRACKET);
            } else if (S.token() == DOT) {
                S.nextToken();
                typeArgs = typeArgumentsOpt(EXPR);
                if (S.token() == SUPER && (mode & EXPR) != 0) {
                    mode = EXPR;
                    t = to(F.at(pos1).Select(t, names._super));
                    S.nextToken();
                    t = arguments(typeArgs, t);
                    typeArgs = null;
                } else if (S.token() == NEW && (mode & EXPR) != 0) {
                    if (typeArgs != null) return illegal();
                    mode = EXPR;
                    int pos2 = S.pos();
                    S.nextToken();
                    if (S.token() == LT) typeArgs = typeArguments();
                    t = innerCreator(pos2, typeArgs, t);
                    typeArgs = null;
                } else {
                    t = toP(F.at(pos1).Select(t, ident()));
                    t = argumentsOpt(typeArgs, typeArgumentsOpt(t));
                    typeArgs = null;
                }
            } else {
                break;
            }
        }
		 //��Ӧ{PostfixOp}
        while ((S.token() == PLUSPLUS || S.token() == SUBSUB) && (mode & EXPR) != 0) {
			/* ���﷨�����׶�:i++--++--����ȷ�ģ����Ҵ���������JCUnary
			PostfixOp t=i++
			PostfixOp t=i++--
			PostfixOp t=i++--++
			PostfixOp t=i++--++--
			----------------------------------------------
			test\parser\ParserTest.java:200: ���������
			��Ҫ�� ����
			�ҵ��� ֵ
							int i2=i++--++--;
									^
			1 ����
			*/
            mode = EXPR;
            t = to(F.at(S.pos()).Unary(
                  S.token() == PLUSPLUS ? JCTree.POSTINC : JCTree.POSTDEC, t));
			DEBUG.P("PostfixOp t="+t);
            S.nextToken();
        }
        return toP(t);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"term3()");
		}
    }

    /** SuperSuffix = Arguments | "." [TypeArguments] Ident [Arguments]
     */
    JCExpression superSuffix(List<JCExpression> typeArgs, JCExpression t) {
    	DEBUG.P(this,"superSuffix(2)");
        S.nextToken();
        if (S.token() == LPAREN || typeArgs != null) {
            t = arguments(typeArgs, t);
        } else {
            int pos = S.pos();
            accept(DOT);
            typeArgs = (S.token() == LT) ? typeArguments() : null;
            t = toP(F.at(pos).Select(t, ident()));
            t = argumentsOpt(typeArgs, t);
        }
        DEBUG.P(0,this,"superSuffix(2)");
        return t;
    }

    /** BasicType = BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN
     */
    JCPrimitiveTypeTree basicType() {
    	DEBUG.P(this,"basicType");
    	DEBUG.P("S.token()="+S.token());
    	
        JCPrimitiveTypeTree t = to(F.at(S.pos()).TypeIdent(typetag(S.token())));
        S.nextToken();
        
        DEBUG.P(0,this,"basicType");
        return t;
    }

    /** ArgumentsOpt = [ Arguments ]
     */
    JCExpression argumentsOpt(List<JCExpression> typeArgs, JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"argumentsOpt(2)");
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token()+" typeArgs="+typeArgs);
		
        if ((mode & EXPR) != 0 && S.token() == LPAREN || typeArgs != null) {
            mode = EXPR;
            return arguments(typeArgs, t);
        } else {
            return t;
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"argumentsOpt(2)");
		}
    }

    /** Arguments = "(" [Expression { COMMA Expression }] ")"
     */
    List<JCExpression> arguments() {
    	DEBUG.P(this,"arguments()");
		DEBUG.P("S.token()="+S.token());
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LPAREN) {
            S.nextToken();
            if (S.token() != RPAREN) {
                args.append(expression());
                while (S.token() == COMMA) {
                    S.nextToken();
                    args.append(expression());
                }
            }
            accept(RPAREN);
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LPAREN));
        }
        
        DEBUG.P(0,this,"arguments()");
        return args.toList();
    }

    JCMethodInvocation arguments(List<JCExpression> typeArgs, JCExpression t) {
        int pos = S.pos();
        List<JCExpression> args = arguments();
        return toP(F.at(pos).Apply(typeArgs, t, args));
    }

    /**  TypeArgumentsOpt = [ TypeArguments ]
     */
    JCExpression typeArgumentsOpt(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgumentsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		/*��������ǲ���������������
		class MemberClassH<T> {}
		MemberClassH<?> Mh1;
		MemberClassH<String> Mh2;
		MemberClassH<? extends Number> Mh3;
		*/
		
        if (S.token() == LT &&
            (mode & TYPE) != 0 &&
            (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeArguments(t);
        } else {
            return t;
        }

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgumentsOpt(JCExpression t)");
		}       
    }
    
    List<JCExpression> typeArgumentsOpt() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgumentsOpt()");
		
        return typeArgumentsOpt(TYPE);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgumentsOpt()");
		}
    }

    List<JCExpression> typeArgumentsOpt(int useMode) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"typeArgumentsOpt(int useMode)");
        DEBUG.P("useMode="+myMode(useMode));
        DEBUG.P("mode="+myMode(mode));
        DEBUG.P("S.token()="+S.token());

        if (S.token() == LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeArguments();
        }
        return null;
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"typeArgumentsOpt(int useMode)");
        }
    }

    /**  TypeArguments  = "<" TypeArgument {"," TypeArgument} ">"
     */
    List<JCExpression> typeArguments() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArguments()");
		DEBUG.P("S.token()="+S.token()+" mode="+myMode(mode));
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LT) {
            S.nextToken();
            //TypeArguments���������� expr=<?>
            
            //ֻ��mode����EXPRʱ((mode & EXPR) == 0)��
            //�����ڡ�<>���з��롰������
            args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            while (S.token() == COMMA) {
                S.nextToken();
                args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            }
            switch (S.token()) {
            case GTGTGTEQ:
                S.token(GTGTEQ);
                break;
            case GTGTEQ:
                S.token(GTEQ);
                break;
            case GTEQ:
                S.token(EQ);
                break;
            case GTGTGT:
                S.token(GTGT);
                break;
            case GTGT:
                S.token(GT);
                break;
            default:
                accept(GT);
                break;
            }
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LT));
        }
        return args.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArguments()");
		}
    }

    /** TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     */
     
     /*
     ��Java Language Specification, Third Edition
	 18.1. The Grammar of the Java Programming Language
	 �еĶ�������:
     TypeArgument:
      Type
      ? [( extends | super ) Type]
     ����������﷨�Ǵ���ġ�
     "?" EXTENDS Type {"&" Type} Ӧ�ĳ� "?" EXTENDS Type
     */
    JCExpression typeArgument() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArgument()");
		
        if (S.token() != QUES) return type();
		//����JCWildcard�����Ŀ�ʼλ��pos�Ǵ�"?"�����token�Ŀ�ʼλ�������
        int pos = S.pos();
        S.nextToken();
        if (S.token() == EXTENDS) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.EXTENDS));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == SUPER) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.SUPER));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == IDENTIFIER) {
			/*����:
			class MemberClassH<T> {}
			MemberClassH<? mh;
			*/
            //error recovery
            reportSyntaxError(S.prevEndPos(), "expected3",
                    keywords.token2string(GT),
                    keywords.token2string(EXTENDS),
                    keywords.token2string(SUPER));
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            JCExpression wc = toP(F.at(pos).Wildcard(t, null));
            JCIdent id = toP(F.at(S.pos()).Ident(ident()));
            return F.at(pos).Erroneous(List.<JCTree>of(wc, id));
        } else {
			/*���������������:
			class MemberClassH<T> {}
			MemberClassH<? <;

			��ô����������ﲢ��������������UNBOUND���͵�JCWildcard��
			���ǽ����Ϸ���"<"�ַ�����������������ĵ��������д���
			����ͨ��typeArguments()�����������ʱ����typeArguments()���
			"default:
                accept(GT);"��δ�����ͻᱨ��"��Ҫ >"�����Ĵ�����ʾ
			*/
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            return toP(F.at(pos).Wildcard(t, null));
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArgument()");
		}
    }

    JCTypeApply typeArguments(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeArguments(JCExpression t)");
		
        int pos = S.pos();
        List<JCExpression> args = typeArguments();
        return toP(F.at(pos).TypeApply(t, args));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeArguments(JCExpression t)");
		}
    }
    
    /*
    bracketsOpt��bracketsOptCont������������������һ��JCArrayTypeTree
    ��:int a[]����Ӧһ��elemtypeΪint��JCArrayTypeTree��
    ��:int a[][]����Ӧһ��elemtypeΪint�������JCArrayTypeTree��
    ��ά����ͨ��bracketsOpt��bracketsOptCont�����������������ʵ��
    
    int a[][]��JCArrayTypeTree��ʾΪ"
    JCArrayTypeTree = {
    	JCExpression elemtype = {
    		JCArrayTypeTree = {
    			JCExpression elemtype = int;
    		}
    	}
    }
    
    int a[][]��int[][] a�����ֱ�ʾ��ʽ����һ����
    */
    
    /** BracketsOpt = {"[" "]"}
     */
    private JCExpression bracketsOpt(JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"bracketsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == LBRACKET) {
            int pos = S.pos();
            S.nextToken();
            t = bracketsOptCont(t, pos);
            F.at(pos);
        }
        DEBUG.P("t="+t);
        return t;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"bracketsOpt(JCExpression t)");
		}    
    }

    private JCArrayTypeTree bracketsOptCont(JCExpression t, int pos) {
        accept(RBRACKET);
        t = bracketsOpt(t);
        return toP(F.at(pos).TypeArray(t));
    }

    /** BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    JCExpression bracketsSuffix(JCExpression t) {
    	DEBUG.P(this,"bracketsSuffix(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
		//��:Class c=int[][].class;
        if ((mode & EXPR) != 0 && S.token() == DOT) {
            mode = EXPR;
            int pos = S.pos();
            S.nextToken();
            accept(CLASS);
            if (S.pos() == errorEndPos) {
                // error recovery
                Name name = null;
                if (S.token() == IDENTIFIER) {//��:Class c=int[][].classA;
                    name = S.name();
                    S.nextToken();
                } else {//��:Class c=int[][].char;//���Դ������δ����ֻ��һ��
                    name = names.error;
                }
				DEBUG.P("name="+name);
                t = F.at(pos).Erroneous(List.<JCTree>of(toP(F.at(pos).Select(t, name))));
            } else {
                t = toP(F.at(pos).Select(t, names._class));
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE; //ע������ ��:public int[][] i1={{1,2},{3,4}};
        } else {
			//��:Class c=int[][];
			//��:Class c=int[][].123;
            syntaxError(S.pos(), "dot.class.expected");
        }
        
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
        DEBUG.P(0,this,"bracketsSuffix(JCExpression t)");
        return t;
    }

    /** Creator = Qualident [TypeArguments] ( ArrayCreatorRest | ClassCreatorRest )
     */
    JCExpression creator(int newpos, List<JCExpression> typeArgs) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"creator(2)");
		
        switch (S.token()) {
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs == null)
                return arrayCreatorRest(newpos, basicType());
            break;
        default:
        }
        JCExpression t = qualident();
        int oldmode = mode;
        mode = TYPE;
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            t = toP(F.at(pos).Select(t, ident()));
            if (S.token() == LT) {
                checkGenerics();
                t = typeArguments(t);
            }
        }
        mode = oldmode;
        DEBUG.P("S.token()="+S.token());
        DEBUG.P("typeArgs="+typeArgs);
        if (S.token() == LBRACKET) {
            JCExpression e = arrayCreatorRest(newpos, t);
            if (typeArgs != null) {
                int pos = newpos;
                if (!typeArgs.isEmpty() && typeArgs.head.pos != Position.NOPOS) {
                    // note: this should always happen but we should
                    // not rely on this as the parser is continuously
                    // modified to improve error recovery.
                    pos = typeArgs.head.pos;
                }
                setErrorEndPos(S.prevEndPos());
				//�������key������properties�ļ���û��
				/*����:
				class MemberClassG<T> {<T> MemberClassG(T t){}}
				{ MemberClassG[] mg=new <Long>MemberClassG<String>[]{};}
				*/
                reportSyntaxError(pos, "cannot.create.array.with.type.arguments");
                return toP(F.at(newpos).Erroneous(typeArgs.prepend(e)));
            }
            return e;
        } else if (S.token() == LPAREN) {
            return classCreatorRest(newpos, null, typeArgs, t);
        } else {
            reportSyntaxError(S.pos(), "expected2",
                               keywords.token2string(LPAREN),
                               keywords.token2string(LBRACKET));
            t = toP(F.at(newpos).NewClass(null, typeArgs, t, List.<JCExpression>nil(), null));
            return toP(F.at(newpos).Erroneous(List.<JCTree>of(t)));
        }
        
    	}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"creator(2)");
		}
    }

    /** InnerCreator = Ident [TypeArguments] ClassCreatorRest
     */
    JCExpression innerCreator(int newpos, List<JCExpression> typeArgs, JCExpression encl) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"innerCreator(3)");
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("encl="+encl);
		
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        return classCreatorRest(newpos, encl, typeArgs, t);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"innerCreator(3)");
		}
    }

    /** ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    JCExpression arrayCreatorRest(int newpos, JCExpression elemtype) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"arrayCreatorRest(2)");
        DEBUG.P("newpos="+newpos);
        DEBUG.P("elemtype="+elemtype);
        
        accept(LBRACKET);
        if (S.token() == RBRACKET) {
            accept(RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (S.token() == LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                //��:int a[]=new int[];
                //src/my/test/ParserTest.java:6: ȱ������ά��
                //int a[]=new int[];
                //                 ^

                return syntaxError(S.pos(), "array.dimension.missing");
            }
        } else {
            //��ָ��������ά����Ͳ����ô�����'{}'��������г�ʼ����
            //�����������������﷨:
            //int a[]=new int[2]{1,2};
            //int b[][]=new int[2][3]{{1,2,3},{4,5,6}};
            
            ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
            //��:int a[]=new int[8][4];
            dims.append(expression());
            accept(RBRACKET);
            while (S.token() == LBRACKET) {
                int pos = S.pos();
                S.nextToken();
				//int b[][]=new int[2][];      //�޴�
				//int c[][][]=new int[2][][3]; //�д�
				//��һά����Ĵ�С����ָ����������......ά֮��Ŀ�����[][][]
                if (S.token() == RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims.append(expression());
                    accept(RBRACKET);
                }
            }
            DEBUG.P("dims.toList()="+dims.toList());
            DEBUG.P("elemtype="+elemtype);
            return toP(F.at(newpos).NewArray(elemtype, dims.toList(), null));
        }
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"arrayCreatorRest(2)");
        }
    }

    /** ClassCreatorRest = Arguments [ClassBody]
     */
    JCExpression classCreatorRest(int newpos,
                                  JCExpression encl,
                                  List<JCExpression> typeArgs,
                                  JCExpression t)
    {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"classCreatorRest(4)");
		DEBUG.P("encl="+encl);
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("t="+t);
		
        List<JCExpression> args = arguments();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
            int pos = S.pos();
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
            body = toP(F.at(pos).AnonymousClassDef(mods, defs));
        }
        return toP(F.at(newpos).NewClass(encl, typeArgs, t, args, body));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"classCreatorRest(4)");
		}
    }

    /** ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    JCExpression arrayInitializer(int newpos, JCExpression t) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"arrayInitializer(2)");
		
        accept(LBRACE);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE) {
        	//arrayInitializer()��variableInitializer()�����໥����
        	//����ʵ�ֶ�ά����(��{{1,2},{3,4}}�ĳ�ʼ��
            elems.append(variableInitializer());
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE) break;
                elems.append(variableInitializer());
            }
        }
        accept(RBRACE);
        return toP(F.at(newpos).NewArray(t, List.<JCExpression>nil(), elems.toList()));
    	
    	}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"arrayInitializer(2)");
		}  
    }

    /** VariableInitializer = ArrayInitializer | Expression
     */
    public JCExpression variableInitializer() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableInitializer()");
		        
        return S.token() == LBRACE ? arrayInitializer(S.pos(), null) : expression();

		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableInitializer()");
		}    
    }

    /** ParExpression = "(" Expression ")"
     */
    JCExpression parExpression() {
    	DEBUG.P(this,"parExpression()");
        accept(LPAREN);
        JCExpression t = expression();
        accept(RPAREN);
        DEBUG.P(0,this,"parExpression()");
        return t;
    }

    /** Block = "{" BlockStatements "}"
     */
    JCBlock block(int pos, long flags) {
    	DEBUG.P(this,"block(int pos, long flags)");
		DEBUG.P("pos="+pos+" flags="+flags+" modifiers=("+Flags.toString(flags)+")");
		
        accept(LBRACE);
        List<JCStatement> stats = blockStatements();
        
        JCBlock t = F.at(pos).Block(flags, stats);
        while (S.token() == CASE || S.token() == DEFAULT) {
        	/*
        	���´���:
        	{
				case;
			}
			������ʾ:������ case���򡰵��� default��
			*/
            syntaxError("orphaned", keywords.token2string(S.token()));
            switchBlockStatementGroups();
        }
        // the Block node has a field "endpos" for first char of last token, which is
        // usually but not necessarily the last char of the last token.
        t.endpos = S.pos();
        accept(RBRACE);
        
        DEBUG.P(1,this,"block(int pos, long flags)");
        return toP(t);
    }

    public JCBlock block() {
        return block(S.pos(), 0);
    }

    /** BlockStatements = { BlockStatement }
     *  BlockStatement  = LocalVariableDeclarationStatement
     *                  | ClassOrInterfaceOrEnumDeclaration
     *                  | [Ident ":"] Statement
     *  LocalVariableDeclarationStatement
     *                  = { FINAL | '@' Annotation } Type VariableDeclarators ";"
     */
    @SuppressWarnings("fallthrough")
    List<JCStatement> blockStatements() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"blockStatements()");
		
//todo: skip to anchor on error(?)
        int lastErrPos = -1;
        ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
        while (true) {
            int pos = S.pos();
            DEBUG.P("S.token()="+S.token());
            switch (S.token()) {
            case RBRACE: case CASE: case DEFAULT: case EOF:
                return stats.toList();
            case LBRACE: case IF: case FOR: case WHILE: case DO: case TRY:
            case SWITCH: case SYNCHRONIZED: case RETURN: case THROW: case BREAK:
            case CONTINUE: case SEMI: case ELSE: case FINALLY: case CATCH:
                stats.append(statement());
                break;
            case MONKEYS_AT:
            case FINAL: {
				//ö�����Ͳ���Ϊ��������(�����������case ENUM: case ASSERT:��BUG)
				//enum MyEnum {}              //�д�
				//final enum MyEnum {}        //�д�
				//@MyAnnotation enum MyEnum {}//�޴�
            	DEBUG.P("MONKEYS_AT �� FINAL��ͷ��");
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                if (S.token() == INTERFACE ||
                    S.token() == CLASS ||
                    allowEnums && S.token() == ENUM) {
                    stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                } else {
                    JCExpression t = type();
                    stats.appendList(variableDeclarators(mods, t,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                }
                break;
            }
            case ABSTRACT: case STRICTFP: {
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                break;
            }
            case INTERFACE:
            case CLASS:
                stats.append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                               S.docComment()));
                break;
            case ENUM:
            case ASSERT:
                if (allowEnums && S.token() == ENUM) {
                    log.error(S.pos(), "local.enum");//ö�����Ͳ���Ϊ��������
                    stats.
                        append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                                 S.docComment()));
                    break;
                } else if (allowAsserts && S.token() == ASSERT) {
                    stats.append(statement());
                    break;
                }
                /* fall through to default */
            default:
            	DEBUG.P("default");
                Name name = S.name(); //ֻ�Ա�ǩ�������
                DEBUG.P("name="+name);
                JCExpression t = term(EXPR | TYPE);
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("lastmode="+myMode(lastmode));
                
                if (S.token() == COLON && t.tag == JCTree.IDENT) {//��ǩ���
                    S.nextToken();
                    JCStatement stat = statement();
                    stats.append(F.at(pos).Labelled(name, stat));
                } else if ((lastmode & TYPE) != 0 &&
                           (S.token() == IDENTIFIER ||
                            S.token() == ASSERT ||
                            S.token() == ENUM)) { //����MONKEYS_AT �� FINAL��ͷ�ı��ر���
                    pos = S.pos();
                    JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
                    F.at(pos);
                    stats.appendList(variableDeclarators(mods, t,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                } else {
			/*
			�Ϸ��ı��ʽ���:
			++a��--a��a++��a--��
			a=b��
			a|=b��a^=b��a&=b��
			a<<=b��a>>=b��a>>>=b��a+=b��a-=b��a*=b��a/=b��a%=b��
			a(),new a()
			*/
                    // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                    stats.append(to(F.at(pos).Exec(checkExprStat(t))));
                    accept(SEMI);
                }
            } //switch����

            // error recovery
            if (S.pos() == lastErrPos)
                return stats.toList();
            if (S.pos() <= errorEndPos) {
                skip(false, true, true, true);
                lastErrPos = S.pos();
            }

            // ensure no dangling /** @deprecated */ active
            S.resetDeprecatedFlag();
        } //while����
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"blockStatements()");
		}
    }

    /** Statement =
     *       Block
     *     | IF ParExpression Statement [ELSE Statement]
     *     | FOR "(" ForInitOpt ";" [Expression] ";" ForUpdateOpt ")" Statement
     *     | FOR "(" FormalParameter : Expression ")" Statement
     *     | WHILE ParExpression Statement
     *     | DO Statement WHILE ParExpression ";"
     *     | TRY Block ( Catches | [Catches] FinallyPart )
     *     | SWITCH ParExpression "{" SwitchBlockStatementGroups "}"
     *     | SYNCHRONIZED ParExpression Block
     *     | RETURN [Expression] ";"
     *     | THROW Expression ";"
     *     | BREAK [Ident] ";"
     *     | CONTINUE [Ident] ";"
     *     | ASSERT Expression [ ":" Expression ] ";"
     *     | ";"
     *     | ExpressionStatement
     *     | Ident ":" Statement
     */
    @SuppressWarnings("fallthrough")
    public JCStatement statement() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"statement()");

        int pos = S.pos();
        switch (S.token()) {
        case LBRACE:
            return block();
        case IF: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement thenpart = statement();
            JCStatement elsepart = null;
            if (S.token() == ELSE) {
                S.nextToken();
                elsepart = statement();
            }
            return F.at(pos).If(cond, thenpart, elsepart);
        }
        case FOR: {
            S.nextToken();
            accept(LPAREN);
            List<JCStatement> inits = S.token() == SEMI ? List.<JCStatement>nil() : forInit();
            DEBUG.P("inits.length()="+inits.length());
            if (inits.length() == 1 &&
                inits.head.tag == JCTree.VARDEF &&
                ((JCVariableDecl) inits.head).init == null &&
                S.token() == COLON) {
                checkForeach();
                JCVariableDecl var = (JCVariableDecl)inits.head;
                accept(COLON);
                JCExpression expr = expression();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForeachLoop(var, expr, body);
            } else {
                accept(SEMI);
                JCExpression cond = S.token() == SEMI ? null : expression();
                accept(SEMI);
                List<JCExpressionStatement> steps = S.token() == RPAREN ? List.<JCExpressionStatement>nil() : forUpdate();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForLoop(inits, cond, steps, body);
            }
        }
        case WHILE: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement body = statement();
            return F.at(pos).WhileLoop(cond, body);
        }
        case DO: {
            S.nextToken();
            JCStatement body = statement();
            accept(WHILE);
            JCExpression cond = parExpression();
            JCDoWhileLoop t = to(F.at(pos).DoLoop(body, cond));
            accept(SEMI);
            return t;
        }
        case TRY: {
            S.nextToken();
            JCBlock body = block();
            ListBuffer<JCCatch> catchers = new ListBuffer<JCCatch>();
            JCBlock finalizer = null;
            if (S.token() == CATCH || S.token() == FINALLY) {
                while (S.token() == CATCH) catchers.append(catchClause());
                if (S.token() == FINALLY) {
                    S.nextToken();
                    finalizer = block();
                }
            } else {
                log.error(pos, "try.without.catch.or.finally");
            }
            return F.at(pos).Try(body, catchers.toList(), finalizer);
        }
        case SWITCH: {
            S.nextToken();
            JCExpression selector = parExpression();
            accept(LBRACE);
            List<JCCase> cases = switchBlockStatementGroups();
            JCSwitch t = to(F.at(pos).Switch(selector, cases));
            accept(RBRACE);
            return t;
        }
        case SYNCHRONIZED: {
            S.nextToken();
            JCExpression lock = parExpression();
            JCBlock body = block();
            return F.at(pos).Synchronized(lock, body);
        }
        case RETURN: {
            S.nextToken();
            JCExpression result = S.token() == SEMI ? null : expression();
            JCReturn t = to(F.at(pos).Return(result));
            accept(SEMI);
            return t;
        }
        case THROW: {
            S.nextToken();
            JCExpression exc = expression();
            JCThrow t = to(F.at(pos).Throw(exc));
            accept(SEMI);
            return t;
        }
        case BREAK: {
            S.nextToken();
            /*
            bin\mysrc\my\test\Test.java:80: �Ӱ汾 1.4 ��ʼ��'assert' ��һ���ؼ��֣���������
			����ʶ��
			����ʹ�� -source 1.3 ����Ͱ汾�Ա㽫 'assert' ������ʶ����
			                        break assert;
			                              ^
			1 ����
			*/
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCBreak t = to(F.at(pos).Break(label));
            accept(SEMI);
            return t;
        }
        case CONTINUE: {
            S.nextToken();
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCContinue t =  to(F.at(pos).Continue(label));
            accept(SEMI);
            return t;
        }
        case SEMI:
            S.nextToken();
            return toP(F.at(pos).Skip());
        case ELSE:
            return toP(F.Exec(syntaxError("else.without.if")));
        case FINALLY:
            return toP(F.Exec(syntaxError("finally.without.try")));
        case CATCH:
            return toP(F.Exec(syntaxError("catch.without.try")));
        case ASSERT: {
            if (allowAsserts && S.token() == ASSERT) {
                S.nextToken();
                JCExpression assertion = expression();
                JCExpression message = null;
                if (S.token() == COLON) {
                    S.nextToken();
                    message = expression();
                }
                JCAssert t = to(F.at(pos).Assert(assertion, message));
                accept(SEMI);
                return t;
            }
            /* else fall through to default case */
        }
        case ENUM:
        default:
            Name name = S.name();
            JCExpression expr = expression();
            if (S.token() == COLON && expr.tag == JCTree.IDENT) {
                S.nextToken();
                JCStatement stat = statement();
                return F.at(pos).Labelled(name, stat);
            } else {
                // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                JCExpressionStatement stat = to(F.at(pos).Exec(checkExprStat(expr)));
                accept(SEMI);
                return stat;
            }
        }

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"statement()");
		}        
    }

    /** CatchClause     = CATCH "(" FormalParameter ")" Block
     */
    JCCatch catchClause() {
    	DEBUG.P(this,"catchClause()");
        int pos = S.pos();
        accept(CATCH);
        accept(LPAREN);
        JCVariableDecl formal =
            variableDeclaratorId(optFinal(Flags.PARAMETER),
                                 qualident());
        accept(RPAREN);
        JCBlock body = block();
        
        DEBUG.P(0,this,"catchClause()");
        return F.at(pos).Catch(formal, body);
    }

    /** SwitchBlockStatementGroups = { SwitchBlockStatementGroup }
     *  SwitchBlockStatementGroup = SwitchLabel BlockStatements
     *  SwitchLabel = CASE ConstantExpression ":" | DEFAULT ":"
     */
    List<JCCase> switchBlockStatementGroups() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"switchBlockStatementGroups()");
		
        ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
        while (true) {
            int pos = S.pos();
            switch (S.token()) {
            case CASE: {
                S.nextToken();
                JCExpression pat = expression();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(pat, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case DEFAULT: {
                S.nextToken();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(null, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case RBRACE: case EOF:
                return cases.toList();
            default:
                S.nextToken(); // to ensure progress
                syntaxError(pos, "expected3",
                    keywords.token2string(CASE),
                    keywords.token2string(DEFAULT),
                    keywords.token2string(RBRACE));
            }
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"switchBlockStatementGroups()");
		}
    }

    /** MoreStatementExpressions = { COMMA StatementExpression }
     */
    <T extends ListBuffer<? super JCExpressionStatement>> T moreStatementExpressions(int pos,
                                                                    JCExpression first,
                                                                    T stats) {
        DEBUG.P(this,"moreStatementExpressions(3)");
        
        // This Exec is a "StatementExpression"; it subsumes no terminating token
        stats.append(toP(F.at(pos).Exec(checkExprStat(first))));
        while (S.token() == COMMA) {
            S.nextToken();
            pos = S.pos();
            JCExpression t = expression();
            // This Exec is a "StatementExpression"; it subsumes no terminating token
            stats.append(toP(F.at(pos).Exec(checkExprStat(t))));
        }
        
        DEBUG.P(0,this,"moreStatementExpressions(3)");
        return stats;
    }

    /** ForInit = StatementExpression MoreStatementExpressions
     *           |  { FINAL | '@' Annotation } Type VariableDeclarators
     */
    List<JCStatement> forInit() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"forInit()");
		
        ListBuffer<JCStatement> stats = lb();
        int pos = S.pos();
        if (S.token() == FINAL || S.token() == MONKEYS_AT) {
            return variableDeclarators(optFinal(0), type(), stats).toList();
        } else {
            JCExpression t = term(EXPR | TYPE);
            if ((lastmode & TYPE) != 0 &&
                (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM))
                return variableDeclarators(modifiersOpt(), t, stats).toList();
            else
                return moreStatementExpressions(pos, t, stats).toList();
        }
        
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"forInit()");
		}
    }

    /** ForUpdate = StatementExpression MoreStatementExpressions
     */
    List<JCExpressionStatement> forUpdate() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"forUpdate()");
		
        return moreStatementExpressions(S.pos(),
                                        expression(),
                                        new ListBuffer<JCExpressionStatement>()).toList();
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"forUpdate()");
		}
    }

    /** AnnotationsOpt = { '@' Annotation }
     */
    List<JCAnnotation> annotationsOpt() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"annotationsOpt()");
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() != MONKEYS_AT) return List.nil(); // optimization
        ListBuffer<JCAnnotation> buf = new ListBuffer<JCAnnotation>();
        while (S.token() == MONKEYS_AT) {
            int pos = S.pos();
            S.nextToken();
            buf.append(annotation(pos));
        }
        return buf.toList();
        
		}finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"annotationsOpt()");
		}
    }

    /** ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"(����һ��@�ǲ��е�)
     *           | "@" Annotation
     */
    JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }
    JCModifiers modifiersOpt(JCModifiers partial) {
    	DEBUG.P(this,"modifiersOpt(1)");	
    	
    	//flags�Ǹ���Modifierͨ����λ������(|)���õ�
    	//��com.sun.tools.javac.code.Flags������һλ(bit)��ʾһ��Modifier
    	//��flags��long���ͣ����Կɱ�ʾ64����ͬ��Modifier
    	//��flags=0x01ʱ��ʾFlags.PUBLIC,��flags=0x03ʱ��ʾFlags.PUBLIC��Flags.PRIVATE
    	//��flags����Flags.toString(long flags)�����Ϳ���֪��flags�����ĸ�(��Щ)Modifier
        long flags = (partial == null) ? 0 : partial.flags;

        //��Scanner��Javadoc��ɨ�赽��@deprecatedʱS.deprecatedFlag()����true
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        DEBUG.P("(whileǰ) flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        
        ListBuffer<JCAnnotation> annotations = new ListBuffer<JCAnnotation>();
        if (partial != null) annotations.appendList(partial.annotations);
        int pos = S.pos();
        int lastPos = Position.NOPOS;
    loop:
        while (true) {
            // <editor-fold defaultstate="collapsed">
            long flag;
			/*
			��Flags���ж�����12��Standard Java flags��
			���������switch���������INTERFACE��
			������ΪINTERFACE(����ENUM)���治�������������η��ˣ�
			��S.token()==INTERFACEʱ���˳�whileѭ���������׷��INTERFACE���η���־
			*/
            switch (S.token()) {
	            case PRIVATE     : flag = Flags.PRIVATE; break;
	            case PROTECTED   : flag = Flags.PROTECTED; break;
	            case PUBLIC      : flag = Flags.PUBLIC; break;
	            case STATIC      : flag = Flags.STATIC; break;
	            case TRANSIENT   : flag = Flags.TRANSIENT; break;
	            case FINAL       : flag = Flags.FINAL; break;
	            case ABSTRACT    : flag = Flags.ABSTRACT; break;
	            case NATIVE      : flag = Flags.NATIVE; break;
	            case VOLATILE    : flag = Flags.VOLATILE; break;
	            case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
	            case STRICTFP    : flag = Flags.STRICTFP; break;
	            case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
	            default: break loop;
            }
            //���η��ظ�,������ʾ��Ϣ��com\sun\tools\javac\resources\compiler.properties����
            if ((flags & flag) != 0) log.error(S.pos(), "repeated.modifier");
            //��������û���жϳ�������У�ֻ����Log�м�¼�´���������
            //DEBUG.P("Log.nerrors="+log.nerrors);
            
            lastPos = S.pos();
            S.nextToken();
           
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();//��鵱ǰ��-source�汾�Ƿ�֧��ע��
                
                //�ǡ�@interface���﷨ע��ʶ��(@interface����ע�����͵Ķ���)
                //��@interface���﷨��com.sun.tools.javac.util.Version����������������
                //JDK1.6���й���Annotations���ĵ���technotes/guides/language/annotations.html
                if (S.token() != INTERFACE) {
					//lastPos��@�Ŀ�ʼλ��
                    JCAnnotation ann = annotation(lastPos);
					DEBUG.P("pos="+pos);
					DEBUG.P("ann.pos="+ann.pos);
                    // if first modifier is an annotation, set pos to annotation's.
                    if (flags == 0 && annotations.isEmpty())
                        pos = ann.pos;
                    annotations.append(ann);
                    lastPos = ann.pos;

                    //ע������,�������checkNoMods(mods.flags)��Ӱ��
                    flag = 0;
                }
            }
            flags |= flag;
            // </editor-fold>
        }
        switch (S.token()) {
	        case ENUM: flags |= Flags.ENUM; break;
	        case INTERFACE: flags |= Flags.INTERFACE; break;
	        default: break;
        }
        
        DEBUG.P("(while��)  flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        DEBUG.P("JCAnnotation count="+annotations.size());

        /* A modifiers tree with no modifier tokens or annotations
         * has no text position. */
        if (flags == 0 && annotations.isEmpty())
            pos = Position.NOPOS;
            
        JCModifiers mods = F.at(pos).Modifiers(flags, annotations.toList());
        
        if (pos != Position.NOPOS)
            storeEnd(mods, S.prevEndPos());//storeEnd()ֻ��һ���շ���,����EndPosParser����д
            
        DEBUG.P(1,this,"modifiersOpt(1)");	
        return mods;
    }

    /** Annotation              = "@" Qualident [ "(" AnnotationFieldValues ")" ]
     * @param pos position of "@" token
     */
    JCAnnotation annotation(int pos) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"annotation(int pos)");
        DEBUG.P("pos="+pos);


        // accept(AT); // AT consumed by caller
        checkAnnotations();
        JCTree ident = qualident();
        List<JCExpression> fieldValues = annotationFieldValuesOpt();
        JCAnnotation ann = F.at(pos).Annotation(ident, fieldValues);
        storeEnd(ann, S.prevEndPos());
        return ann;
        
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"annotation(int pos)");
        }
    }

    List<JCExpression> annotationFieldValuesOpt() {
        return (S.token() == LPAREN) ? annotationFieldValues() : List.<JCExpression>nil();
    }

    /** AnnotationFieldValues   = "(" [ AnnotationFieldValue { "," AnnotationFieldValue } ] ")" */
    List<JCExpression> annotationFieldValues() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"annotationFieldValues()");

        accept(LPAREN);
        ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
        if (S.token() != RPAREN) {
            buf.append(annotationFieldValue());
            while (S.token() == COMMA) {
                S.nextToken();
                buf.append(annotationFieldValue());
            }
        }
        accept(RPAREN);
        return buf.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"annotationFieldValues()");
		}        
    }

    /** AnnotationFieldValue    = AnnotationValue
     *                          | Identifier "=" AnnotationValue
     */
    JCExpression annotationFieldValue() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"annotationFieldValue()");
		
        if (S.token() == IDENTIFIER) {
            mode = EXPR;
            JCExpression t1 = term1();
            if (t1.tag == JCTree.IDENT && S.token() == EQ) {
                int pos = S.pos();
                accept(EQ);
                return toP(F.at(pos).Assign(t1, annotationValue()));
            } else {
                return t1;
            }
        }
        return annotationValue();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"annotationFieldValue()");
		} 
    }

    /* AnnotationValue          = ConditionalExpression
     *                          | Annotation
     *                          | "{" [ AnnotationValue { "," AnnotationValue } ] "}"
     */
    JCExpression annotationValue() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"annotationValue()");
		
        int pos;
        //JDK1.6���й���ע���ֶ�ȡֵ���ĵ���technotes/guides/language/annotations.html
        switch (S.token()) {
        case MONKEYS_AT:  //ע���ֶε�ֵ��ע�͵����
            pos = S.pos();
            S.nextToken();
            return annotation(pos);
        case LBRACE:  //ע���ֶε�ֵ����������
            pos = S.pos();
            accept(LBRACE);
            ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
            if (S.token() != RBRACE) {
                buf.append(annotationValue());
                while (S.token() == COMMA) {
                    S.nextToken();
                    if (S.token() == RPAREN) break;
                    buf.append(annotationValue());
                }
            }
            accept(RBRACE);
            
            //JCNewArray���﷨��������:
            //new type dimensions initializers ��
            //new type dimensions [ ] initializers
            //��com.sun.source.tree.NewArrayTree
            return toP(F.at(pos).NewArray(null, List.<JCExpression>nil(), buf.toList()));
        default:
            mode = EXPR;
            return term1();
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"annotationValue()");
		} 
    }

	/*
	<T extends ListBuffer<? super JCVariableDecl>> T vdefs�������?
	��˼��:������T vdefs���ġ�type argument�������ͱ�����ListBuffer��������,
	����ListBuffer��������ġ�parameterized type������JCVariableDecl���䳬�ࡣ
	
	���Ӳο�forInit()�����е����´���Ƭ��:
	ListBuffer<JCStatement> stats......
	variableDeclarators(......, stats)

	���С�type argument��ָ����stats������ָ��ListBuffer<JCStatement>��ʵ�������ã�
	ListBuffer�ġ�parameterized type��ָ����JCStatement����JCStatement
	����JCVariableDecl�ĳ��ࡣ
	*/

    /** VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    public <T extends ListBuffer<? super JCVariableDecl>> T variableDeclarators(JCModifiers mods,
                                                                         JCExpression type,
                                                                         T vdefs)
    {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclarators(3)");
		
        return variableDeclaratorsRest(S.pos(), mods, type, ident(), false, null, vdefs);

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclarators(3)");
		}         
    }

    /** VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    <T extends ListBuffer<? super JCVariableDecl>> T variableDeclaratorsRest(int pos,
                                                                     JCModifiers mods,
                                                                     JCExpression type,
                                                                     Name name,
                                                                     boolean reqInit,
                                                                     String dc,
                                                                     T vdefs) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclaratorsRest(7)");
		
        vdefs.append(variableDeclaratorRest(pos, mods, type, name, reqInit, dc));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
			DEBUG.P("S.endPos()="+S.endPos());
            storeEnd((JCTree)vdefs.elems.last(), S.endPos());
            S.nextToken();
            vdefs.append(variableDeclarator(mods, type, reqInit, dc));
        }
        return vdefs;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclaratorsRest(7)");
		}          
    }

    /** VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    JCVariableDecl variableDeclarator(JCModifiers mods, JCExpression type, boolean reqInit, String dc) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclarator(4)");
		
        return variableDeclaratorRest(S.pos(), mods, type, ident(), reqInit, dc);
       
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclarator(4)");
		}      
    }

    /** VariableDeclaratorRest = BracketsOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt "=" VariableInitializer
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    JCVariableDecl variableDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name,
                                  boolean reqInit, String dc) {
        try {//�Ҽ��ϵ�
		DEBUG.P(this,"variableDeclaratorRest(6)");
		DEBUG.P("pos="+pos);
		DEBUG.P("mods="+mods);
		DEBUG.P("type="+type);
		DEBUG.P("name="+name);
		//�ӿ��ж���ĳ�Ա������Ҫ��ʼ��
		//reqInit��ʱ����isInterface��ֵ
		DEBUG.P("reqInit="+reqInit);
		DEBUG.P("dc="+dc);
		
        type = bracketsOpt(type); //����:String s1[]
        JCExpression init = null;
        if (S.token() == EQ) {
            S.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(S.pos(), "expected", keywords.token2string(EQ));
        //���ڽӿ��ж���ĳ�Ա���������û��ָ�����η���
        //��Parser�׶�Ҳ�����Զ�����
        //DEBUG.P("mods="+mods);
        JCVariableDecl result =
            toP(F.at(pos).VarDef(mods, name, type, init));
        attach(result, dc);
        return result;

        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"variableDeclaratorRest(6)");
		}       
    }

    /** VariableDeclaratorId = Ident BracketsOpt
     */
    JCVariableDecl variableDeclaratorId(JCModifiers mods, JCExpression type) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"variableDeclaratorId(2)");
		
        int pos = S.pos();
        Name name = ident();
        if ((mods.flags & Flags.VARARGS) == 0)
		//mothodName(N[] n[],S s)�����﷨Ҳ���ᱨ��
		//mothodName(N... n[],S s)�����﷨�ͻᱨ��
		//mothodName(N[8] n[9],S s)�����﷨Ҳ�ᱨ��
		//��Ϊ���������е��������Ͳ����ǲ���ָ�������С��
            type = bracketsOpt(type);
        //�����β�û�г�ʼ�����֣�����VarDef�����ĵ�4������Ϊnull
        return toP(F.at(pos).VarDef(mods, name, type, null));

        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"variableDeclaratorId(2)");
        }  
    }
    //���������ķ���ʾ�ò���׼ȷ
    /** CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     */
    //�����ע����LL(1)�ķ����ܵ�ȫò, ˵��CompilationUnit =��Ҳ����,
    //����ͬ����һ��û���κ����ݵ�Դ�ļ�Ҳ���ᱨ��һ��
    public JCTree.JCCompilationUnit compilationUnit() {
    	DEBUG.P(this,"compilationUnit() ��ʽ��ʼ�﷨����......");
    	DEBUG.P("startPos="+S.pos());
    	DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("errorEndPos="+errorEndPos);
        DEBUG.P("startToken="+S.token());
        
        int pos = S.pos();
        JCExpression pid = null;//��Ӧ�ķ��е�Qualident
        //��ǰtoken��Ӧ��javadoc(��DocCommentScanner.processComment(1))
        String dc = S.docComment();
        DEBUG.P("dc="+dc);

		//��Ӧ�ķ��е�{ "@" Annotation }�������ǰ�ע�ͣ�
		//Ҳ�����ǵ�һ��������������η�
        JCModifiers mods = null;
        
        List<JCAnnotation> packageAnnotations = List.nil();
        
        if (S.token() == MONKEYS_AT)
            mods = modifiersOpt();
        /*
        ֻ����package-info.java�ļ��в����а�ע��(��û���ر�ָ��������£���ע�͡�ָ����Annotation)
        ������д�����ʾ���������ע��Ӧ���ļ� package-info.java �С�
        ��Ӧcompiler.properties�е�"pkg.annotations.sb.in.package-info.java"
        �������﷨�����׶μ�飬������com.sun.tools.javac.comp.Enter�м��
        */
        if (S.token() == PACKAGE) {
            //����ڡ�package��ǰ��JavaDoc,��������@deprecated��
            //���Ǻ���û��@Annotation������modifiers���ǺϷ��ġ�
            if (mods != null) {
            	/*
            	����Ƿ�����ʹ�����η�
            	���package-info.java�ļ���Դ������������:
            	@Deprecated public
                package my.test;

                �ͻᱨ��:
                bin\mysrc\my\test\package-info.java:2: �˴�������ʹ�����η� public
                package my.test;
                ^
                1 ����				
                */
                checkNoMods(mods.flags);
                packageAnnotations = mods.annotations;
                mods = null;
            }
            S.nextToken();
            pid = qualident();
            accept(SEMI);
        }
        //defs�д�Ÿ�import���������(class,interface��)������ص�JTree
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
       	boolean checkForImports = true;
        while (S.token() != EOF) {
            DEBUG.P("S.pos()="+S.pos()+"  errorEndPos="+errorEndPos);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (S.token() == EOF)
                    break;
            }
            
            //�����ע��Ӧ���ļ� package-info.java ��,��package-info.java��û��import�ģ�
            //��package-info.java�ļ������а�ע�ͣ�����mods==null(???)
            //(�������ʺŵ�ע�ͱ���Ŀǰ��δ��ȫ������)
			//��Ϊ��һ��������֮ǰ����û��import����ʱ��Ϊ�ǵ�һ�ν���whileѭ��
			//checkForImportsΪtrue������mods���ܲ�Ϊnull(�纬��public��)
            if (checkForImports && mods == null && S.token() == IMPORT) {
                defs.append(importDeclaration());
            } else {
				//��û��ָ��package��import���ʱ��������������֮ǰ����@��
				//�磺@MyAnnotation public ClassA {}����mods!=null
                JCTree def = typeDeclaration(mods);
                
                //��JCExpressionStatement��JCErroneous����װ������
                if (def instanceof JCExpressionStatement)
                    def = ((JCExpressionStatement)def).expr;
                defs.append(def);

				//���ﱣ֤����������֮������import���
                if (def instanceof JCClassDecl)
                    checkForImports = false;
				//���������������������η���
				//������ͬһ�ļ��������������������Ϊnull��
				//��ΪtypeDeclaration(mods)ʱ������modifiersOpt(mods)
                mods = null;
            }
        }
        //F.at(pos)���pos����int pos = S.pos();ʱ��pos,һֱû��
        JCTree.JCCompilationUnit toplevel = F.at(pos).TopLevel(packageAnnotations, pid, defs.toList());
        attach(toplevel, dc);

		DEBUG.P("defs.elems.isEmpty()="+defs.elems.isEmpty());
        if (defs.elems.isEmpty())
            storeEnd(toplevel, S.prevEndPos());
        if (keepDocComments) toplevel.docComments = docComments;
        
        //���е�����﷨������ɣ�������һ�ó����﷨��
		//DEBUG.P("toplevel="+toplevel);
		DEBUG.P("toplevel.startPos="+getStartPos(toplevel));
		DEBUG.P("toplevel.endPos  ="+getEndPos(toplevel));
        DEBUG.P(3,this,"compilationUnit()");
        //DEBUG.P("Parser stop",true);
        return toplevel;
    }

    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     */
    JCTree importDeclaration() {
    	DEBUG.P(this,"importDeclaration()");
        int pos = S.pos();//���һ����import���token�Ŀ�ʼλ��
		DEBUG.P("pos="+pos);
        S.nextToken();
        boolean importStatic = false;
        if (S.token() == STATIC) {
            checkStaticImports();
            importStatic = true;
            S.nextToken();
        }

		//����ǡ�import my.test;������ô����õ���pid�Ŀ�ʼλ����my���token��pos
		//pid�Ľ���λ����my���token��endpos��
		//��ӦnextToken(157,159)=|my|�е�(157,159)
        JCExpression pid = toP(F.at(S.pos()).Ident(ident()));
        do {
            int pos1 = S.pos();
            accept(DOT);
            if (S.token() == STAR) {
                pid = to(F.at(pos1).Select(pid, names.asterisk));//���롰.*"�����
                S.nextToken();
                break;
            } else {
				DEBUG.P("pos1="+pos1);
				//����ǡ�import my.test;������ô����õ���pid��һ��JCFieldAccess
				//���Ŀ�ʼλ���ǡ�.����pos������λ����test���token��endpos
                pid = toP(F.at(pos1).Select(pid, ident()));
            }
        } while (S.token() == DOT);
        accept(SEMI);
        DEBUG.P(2,this,"importDeclaration()");
		//����ǡ�import my.test;������ô����õ���pid��һ��JCImport
		//���Ŀ�ʼλ���ǡ�import����pos������λ����";"���token��endpos
        return toP(F.at(pos).Import(pid, importStatic));
    }

    /** TypeDeclaration = ClassOrInterfaceOrEnumDeclaration
     *                  | ";"
     */
    JCTree typeDeclaration(JCModifiers mods) {
        try {//�Ҽ��ϵ�
        DEBUG.P(this,"typeDeclaration(1)");
        if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        else DEBUG.P("mods=null");
        DEBUG.P("S.token()="+S.token()+"  S.pos()="+S.pos());

        int pos = S.pos();

		//�����ġ�;"��ǰ�治�������η�
        if (mods == null && S.token() == SEMI) {
            S.nextToken();
            return toP(F.at(pos).Skip());
        } else {
            String dc = S.docComment();
			DEBUG.P("dc="+dc);
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }


        }finally{//�Ҽ��ϵ�
        DEBUG.P(2,this,"typeDeclaration(1)");
        }
    }

    /** ClassOrInterfaceOrEnumDeclaration = ModifiersOpt
     *           (ClassDeclaration | InterfaceDeclaration | EnumDeclaration)
     *  @param mods     Any modifiers starting the class or interface declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCStatement classOrInterfaceOrEnumDeclaration(JCModifiers mods, String dc) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"classOrInterfaceOrEnumDeclaration(2)");
    	if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
    	else DEBUG.P("mods=null");
    	DEBUG.P("S.token()="+S.token()+"  dc="+dc);
    	
    	
        if (S.token() == CLASS) {
            return classDeclaration(mods, dc);
        } else if (S.token() == INTERFACE) {
			//����ͬʱ�����ӿ�������ע������������
			//��Ϊ��modifiersOpt(mods)ʱ����������@,
			//����nextToken()������INTERFACE��
			//��flags����INTERFACE���˳�modifiersOpt(mods)
            return interfaceDeclaration(mods, dc);
        } else if (allowEnums) {
            if (S.token() == ENUM) {
                return enumDeclaration(mods, dc);
            } else {
                int pos = S.pos();
                DEBUG.P("pos="+pos);
                List<JCTree> errs;
                if (S.token() == IDENTIFIER) {
                    errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                    DEBUG.P("S.pos()="+S.pos());
                    //��Ȼ�������syntaxError()�ڲ�Ҳ������setErrorEndPos()
                    //����S.pos()>�����int pos,���Դ������λ����S.pos().
                    setErrorEndPos(S.pos());
                } else {
                    errs = List.<JCTree>of(mods);
                }
                //��JCExpressionStatement��JCErroneous����װ������
                return toP(F.Exec(syntaxError(pos, errs, "expected3",
                                              keywords.token2string(CLASS),
                                              keywords.token2string(INTERFACE),
                                              keywords.token2string(ENUM))));
            }
        } else {
            if (S.token() == ENUM) {
                log.error(S.pos(), "enums.not.supported.in.source", source.name);
                allowEnums = true;
                return enumDeclaration(mods, dc);
            }
            int pos = S.pos();
            List<JCTree> errs;
            if (S.token() == IDENTIFIER) {
                errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                setErrorEndPos(S.pos());
            } else {
                errs = List.<JCTree>of(mods);
            }
            return toP(F.Exec(syntaxError(pos, errs, "expected2",
                                          keywords.token2string(CLASS),
                                          keywords.token2string(INTERFACE))));
        }
        
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"classOrInterfaceOrEnumDeclaration(2)");
        }
    }

    /** ClassDeclaration = CLASS Ident TypeParametersOpt [EXTENDS Type]
     *                     [IMPLEMENTS TypeList] ClassBody
     *  @param mods    The modifiers starting the class declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCClassDecl classDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"classDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());

        int pos = S.pos(); //��Ӧclass���token����ʼλ��(pos)
        accept(CLASS);
		//��Ϊ������һ����ʶ����
		//�������ڵ���Scanner���nextToken����ʱ�ֵ�����scanIdent()��
		//ͨ��scanIdent()�������ӽ���Name.Table.names����ֽ��������ˡ�
        Name name = ident();

        List<JCTypeParameter> typarams = typeParametersOpt();//����<>
        DEBUG.P("typarams="+typarams);
        DEBUG.P("typarams.size="+typarams.size());
        

        JCTree extending = null;
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = type();
        }
        DEBUG.P("extending="+extending);
        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }
        DEBUG.P("implementing="+implementing);
        List<JCTree> defs = classOrInterfaceBody(name, false);
		DEBUG.P("defs.size="+defs.size());
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, typarams, extending, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"classDeclaration(2)");
        return result;
    }

    /** InterfaceDeclaration = INTERFACE Ident TypeParametersOpt
     *                         [EXTENDS TypeList] InterfaceBody
     *  @param mods    The modifiers starting the interface declaration
     *  @param dc       The documentation comment for the interface, or null.
     */
    JCClassDecl interfaceDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"interfaceDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());
        int pos = S.pos();
        accept(INTERFACE);
        Name name = ident();

        List<JCTypeParameter> typarams = typeParametersOpt();

        List<JCExpression> extending = List.nil();
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = typeList();
        }
        List<JCTree> defs = classOrInterfaceBody(name, true);
		DEBUG.P("defs.size="+defs.size());
        //�ӿ�û��implements��ע�������4,5������
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, typarams, null, extending, defs));
        attach(result, dc);
        DEBUG.P(2,this,"interfaceDeclaration(2)");
        return result;
    }

    /** EnumDeclaration = ENUM Ident [IMPLEMENTS TypeList] EnumBody
     *  @param mods    The modifiers starting the enum declaration
     *  @param dc       The documentation comment for the enum, or null.
     */
    JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"enumDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());
        int pos = S.pos();
        accept(ENUM);
        Name name = ident();

        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }

        List<JCTree> defs = enumBody(name);
        JCModifiers newMods = //��modifiersOpt()�Ѽ�Flags.ENUM
            F.at(mods.pos).Modifiers(mods.flags|Flags.ENUM, mods.annotations);
        //ö����û��TypeParametersҲû��EXTENDS TypeList
        JCClassDecl result = toP(F.at(pos).
            ClassDef(newMods, name, List.<JCTypeParameter>nil(),
                null, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"enumDeclaration(2)");
        return result;
    }

    /** EnumBody = "{" { EnumeratorDeclarationList } [","]
     *                  [ ";" {ClassBodyDeclaration} ] "}"
     */
    List<JCTree> enumBody(Name enumName) {
    	DEBUG.P(this,"enumBody(Name enumName)");
        accept(LBRACE);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE && S.token() != SEMI) {
            defs.append(enumeratorDeclaration(enumName));
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE || S.token() == SEMI) break;
                defs.append(enumeratorDeclaration(enumName));
            }
            if (S.token() != SEMI && S.token() != RBRACE) {
                defs.append(syntaxError(S.pos(), "expected3",
                                keywords.token2string(COMMA),
                                keywords.token2string(RBRACE),
                                keywords.token2string(SEMI)));
                S.nextToken();
            }
        }
        if (S.token() == SEMI) {
            S.nextToken();
            while (S.token() != RBRACE && S.token() != EOF) {
                defs.appendList(classOrInterfaceBodyDeclaration(enumName,
                                                                false));
                if (S.pos() <= errorEndPos) {
                    // error recovery
                   skip(false, true, true, false);
                }
            }
        }
        accept(RBRACE);
        DEBUG.P(0,this,"enumBody(Name enumName)");
        return defs.toList();
    }
    
    //�ο�jdk1.6.0docs/technotes/guides/language/enums.html
    /** EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    JCTree enumeratorDeclaration(Name enumName) {
    	DEBUG.P(this,"enumeratorDeclaration(Name enumName)");
        String dc = S.docComment();
        int flags = Flags.PUBLIC|Flags.STATIC|Flags.FINAL|Flags.ENUM;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        int pos = S.pos();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? Position.NOPOS : pos).Modifiers(flags, annotations);
        
        /*��Java Language Specification, Third Edition
		 18.1. The Grammar of the Java Programming Language
		 �������¶���:
		 EnumConstant:
      	 Annotations Identifier [Arguments] [ClassBody]
      	 ����������﷨AnnotationsOpt [TypeArguments] IDENTIFIER�Ǵ����
      	 
      	 ���ơ�<?>SUPER("? super ")��������ö�ٳ����Ǵ����(�Ƿ��ı��ʽ��ʼ)
      	 */
        List<JCExpression> typeArgs = typeArgumentsOpt();//���Ƿ���null
        int identPos = S.pos();
        Name name = ident();
        int createPos = S.pos();
        List<JCExpression> args = (S.token() == LPAREN)
            ? arguments() : List.<JCExpression>nil();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
        	/*���´���Ƭ��:
        		public static enum MyBoundKind {
			    @Deprecated EXTENDS("? extends ") {
			    	 String toString() {
			    	 	return "extends"; 
			    	 }
			    },
			*/
            JCModifiers mods1 = F.at(Position.NOPOS).Modifiers(Flags.ENUM | Flags.STATIC);
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            body = toP(F.at(identPos).AnonymousClassDef(mods1, defs));
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        JCIdent ident = F.at(Position.NOPOS).Ident(enumName);
        //ÿ��ö�ٳ������൱���Ǵ�ö�����͵�һ��ʵ��
        JCNewClass create = F.at(createPos).NewClass(null, typeArgs, ident, args, body);
        if (createPos != Position.NOPOS)
            storeEnd(create, S.prevEndPos());
        ident = F.at(Position.NOPOS).Ident(enumName);//ע�����������治��ͬһ��JCIdent��ʵ��
        JCTree result = toP(F.at(pos).VarDef(mods, name, ident, create));
        attach(result, dc);
        
        DEBUG.P(0,this,"enumeratorDeclaration(Name enumName)");
        return result;
    }

    /** TypeList = Type {"," Type}
     */
    List<JCExpression> typeList() {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"typeList()");

        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(type());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(type());
        }
        return ts.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeList()");
		}
    }

    /** ClassBody     = "{" {ClassBodyDeclaration} "}"
     *  InterfaceBody = "{" {InterfaceBodyDeclaration} "}"
     */
    List<JCTree> classOrInterfaceBody(Name className, boolean isInterface) {
    	DEBUG.P(this,"classOrInterfaceBody(2)");
    	DEBUG.P("className="+className);
    	DEBUG.P("isInterface="+isInterface);

        accept(LBRACE);
        if (S.pos() <= errorEndPos) {
            // error recovery
            skip(false, true, false, false);
            if (S.token() == LBRACE)
                S.nextToken();
        }
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        while (S.token() != RBRACE && S.token() != EOF) {
            defs.appendList(classOrInterfaceBodyDeclaration(className, isInterface));
            if (S.pos() <= errorEndPos) {
               // error recovery
               skip(false, true, true, false);
           }
        }
        accept(RBRACE);
        DEBUG.P(2,this,"classOrInterfaceBody(2)");
        return defs.toList();
    }

    /** ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      **********************������6���ǲ��е�**********************
     *      ( Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | TypeParameters (Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | TypeParameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      )
     *      **********************������6���ǲ��е�**********************
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    List<JCTree> classOrInterfaceBodyDeclaration(Name className, boolean isInterface) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"classOrInterfaceBodyDeclaration(2)");
 		DEBUG.P("S.token()="+S.token());

        if (S.token() == SEMI) {//���ﲻ��������JCSkip��ֻ������������(���)���ŵ�";"����JCSkip
            S.nextToken();
            return List.<JCTree>of(F.at(Position.NOPOS).Block(0, List.<JCStatement>nil()));
        } else {
            String dc = S.docComment();
            int pos = S.pos();
            JCModifiers mods = modifiersOpt();
            
            //�ڲ�CLASS,INTERFACE,ENUM
            if (S.token() == CLASS ||
                S.token() == INTERFACE ||
				//�����-source 1.4 -target 1.4�����ڲ�enum���ͣ��������λ�û����
                allowEnums && S.token() == ENUM) {
                return List.<JCTree>of(classOrInterfaceOrEnumDeclaration(mods, dc));
				//����(����static����(STATIC�ؼ�����modifiersOpt()���ѷ�����))
            } else if (S.token() == LBRACE && !isInterface &&
                       (mods.flags & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                       mods.annotations.isEmpty()) {
                       //����ǰ������ע��,ֻ����static
                return List.<JCTree>of(block(pos, mods.flags));
            } else {
                pos = S.pos();
                //ֻ��Method��Constructor֮ǰ����TypeParameter
                List<JCTypeParameter> typarams = typeParametersOpt();
                DEBUG.P("mods.pos="+mods.pos);
                
                // Hack alert:  if there are type arguments(ע����typeParameters) but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.length() > 0 && mods.pos == Position.NOPOS) {
                    mods.pos = pos;
                }
                Token token = S.token();
                Name name = S.name();//���췽��(Constructor)������ �� �ֶ������� �� �����ķ���ֵ��������
                pos = S.pos();
                JCExpression type;//�ֶε����� �� �����ķ���ֵ������
                
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("name="+name);
                
                boolean isVoid = S.token() == VOID;
                if (isVoid) {
                	//typetagΪvoid��JCPrimitiveTypeTree
                    type = to(F.at(pos).TypeIdent(TypeTags.VOID));
                    S.nextToken(); 
                } else {
                    type = type();
                }
                //���Constructor,��������Constructor�����ƣ���term3()������JCTree.JCIdent
                if (S.token() == LPAREN && !isInterface && type.tag == JCTree.IDENT) {
                	
                	//isInterface���������ȫ����ȥ������Ϊͨ��ǰһ��if����
                	//isInterface��ֵ�϶�Ϊfalse
                    if (isInterface || name != className)
                    	//���췽��(Constructor)�����ƺ�������һ��ʱ
                    	//�ᱨ��ֻ�Ǳ�����Ϣ��:������������Ч����Ҫ�������͡�
                        log.error(pos, "invalid.meth.decl.ret.type.req");
                    return List.of(methodDeclaratorRest(
                        pos, mods, null, names.init, typarams,
                        isInterface, true, dc));
                } else {
                    pos = S.pos();
                    name = ident(); //�ֶ����򷽷���������ȡ��һ��token

                    if (S.token() == LPAREN) { //����
                        return List.of(methodDeclaratorRest(
                            pos, mods, type, name, typarams,
                            isInterface, isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty()) { //�ֶ���
						//�ڽӿ��ж�����ֶ���Ҫ��ʾ�ĳ�ʼ��(isInterface=true)
                        List<JCTree> defs =
                            variableDeclaratorsRest(pos, mods, type, name, isInterface, dc,
                                                    new ListBuffer<JCTree>()).toList();
                        storeEnd(defs.last(), S.endPos());
                        accept(SEMI);
                        return defs;
                    } else {
                        pos = S.pos();
                        List<JCTree> err = isVoid
                            ? List.<JCTree>of(toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null)))
                            : null;
                            
                        /*
                        ��:
                        bin\mysrc\my\test\Test.java:32: ��Ҫ '('
						        public <M extends T,S> int myInt='\uuuuu5df2';
						                                        ^
						1 ����
						*/
                        return List.<JCTree>of(syntaxError(S.pos(), err, "expected", keywords.token2string(LPAREN)));
                    }
                }
            }
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(2,this,"classOrInterfaceBodyDeclaration(2)");
		}   
    }

    /** MethodDeclaratorRest =
     *      FormalParameters BracketsOpt [Throws TypeList] ( MethodBody | [DEFAULT AnnotationValue] ";")
     *  VoidMethodDeclaratorRest =
     *      FormalParameters [Throws TypeList] ( MethodBody | ";")
     *  InterfaceMethodDeclaratorRest =
     *      FormalParameters BracketsOpt [THROWS TypeList] ";"
     *  VoidInterfaceMethodDeclaratorRest =
     *      FormalParameters [THROWS TypeList] ";"
     *  ConstructorDeclaratorRest =
     *      "(" FormalParameterListOpt ")" [THROWS TypeList] MethodBody
     */
    JCTree methodDeclaratorRest(int pos,
                              JCModifiers mods,
                              JCExpression type,
                              Name name,
                              List<JCTypeParameter> typarams,
                              boolean isInterface, boolean isVoid,
                              String dc) {    
        DEBUG.P(this,"methodDeclaratorRest(6)");
        DEBUG.P("isVoid="+isVoid);          
        List<JCVariableDecl> params = formalParameters();//���Ƿ����Ĳ���
        if(params!=null) DEBUG.P("params.size="+params.size());  
        
        
        /*
        ����������﷨Ҳ����(����ֵ������Ļ�,[]���Է���������')'����):
	    public int myMethod()[] {
			return new int[0];
		}
		*/
        if (!isVoid) type = bracketsOpt(type);
        
        
         
        List<JCExpression> thrown = List.nil();
        if (S.token() == THROWS) {
            S.nextToken();
            thrown = qualidentList();
        }
        JCBlock body = null;
        JCExpression defaultValue;
        //DEBUG.P("S.token() ="+S.token());
        
		//����ӿ��еķ����з����岢�����﷨����ʱ���
		//interface MemberInterfaceB {
		//	void methodA(){};
		//}
        if (S.token() == LBRACE) {
            body = block();
            defaultValue = null;
        } else {
        	/*
        	ע�����Ͷ����е�"default"
        	��jdk1.6.0docs/technotes/guides/language/annotations.html������:
        	public @interface RequestForEnhancement {
			    int    id();
			    String synopsis();
			    String engineer() default "[unassigned]"; 
			    String date()    default "[unimplemented]"; 
			}
			*/
            if (S.token() == DEFAULT) {
                accept(DEFAULT);
                defaultValue = annotationValue();
            } else {
                defaultValue = null;
            }
            accept(SEMI);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, false, false);
                if (S.token() == LBRACE) {
                    body = block();
                }
            }
        }
        JCMethodDecl result =
            toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                    params, thrown,
                                    body, defaultValue));
        DEBUG.P(2,this,"methodDeclaratorRest(6)");                            
        attach(result, dc);
        return result;
    }

    /** QualidentList = Qualident {"," Qualident}
     */
    List<JCExpression> qualidentList() {
    	/*�������ֻ���ڷ���throws��䣬��Ϊthrows���������������
    	java.lang.Throwable�������࣬�����������޷��̳�Throwable�ģ�
    	����throws�����涼��Qualident {"," Qualident}����implements
    	��������ԽӶ������(��Ƿ���)�࣬������typeList()�ķ�����
    	implements��䡣


		Ҳ����˵��throws����ͷ�����������Ƿ����࣬���Ƿ�����Ļ���
		Ҳ�Ͳ�����������ͷ����<...>�����ķ��Ŵ���û��<...>�����ķ��Ŵ�
		Ҳ����ζ������ȫ��Qualident
    	
    	��������:
    	bin\mysrc\my\test\Test.java:29: �������޷��̳� java.lang.Throwable
		class MyException1<T> extends Throwable {}
		                              ^
		bin\mysrc\my\test\Test.java:30: �������޷��̳� java.lang.Throwable
		class MyException2<T> extends Exception {}
		                              ^
		bin\mysrc\my\test\Test.java:31: �������޷��̳� java.lang.Throwable
		class MyException3<T> extends Error {}
		                              ^
		3 ����
    	*/
    	DEBUG.P(this,"qualidentList()");
        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(qualident());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(qualident());
        }
        DEBUG.P(0,this,"qualidentList()");
        return ts.toList();
    }

    /** TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    List<JCTypeParameter> typeParametersOpt() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"typeParametersOpt()");
    	
        if (S.token() == LT) {
            checkGenerics();
            ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
            S.nextToken();
            typarams.append(typeParameter());
            while (S.token() == COMMA) {
                S.nextToken();
                typarams.append(typeParameter());
            }
            accept(GT);
            return typarams.toList();
        } else {
            return List.nil();
        }
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeParametersOpt()");
		}
    }
    
    /*ע��TypeParameter��TypeArgument�Ĳ��
     *	TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type
     *               | "?" SUPER Type
    
    �Աȷ����������β���ʵ�������TypeParameter��TypeArgument
    */
    
    /** TypeParameter = TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident
     */
    JCTypeParameter typeParameter() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"typeParameter()");
    	
        int pos = S.pos();
        Name name = ident();
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        if (S.token() == EXTENDS) {
            S.nextToken();
            bounds.append(type());
            while (S.token() == AMP) {
                S.nextToken();
                bounds.append(type());
            }
        }
		//���ֻ��<T>����ôbounds.toList()��һ��new List<JCExpression>(null,null)
        return toP(F.at(pos).TypeParameter(name, bounds.toList()));
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"typeParameter()");
		}
    }

    /** FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    List<JCVariableDecl> formalParameters() { //ָ��һ�������������������Ĳ���
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"formalParameters()");
    	
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        JCVariableDecl lastParam = null;
        accept(LPAREN);
        DEBUG.P("S.token()="+S.token());
        if (S.token() != RPAREN) {
            params.append(lastParam = formalParameter());
            //Vararrgs�������ڵĻ������Ƿ����������������Ĳ��������һ��
            while ((lastParam.mods.flags & Flags.VARARGS) == 0 && S.token() == COMMA) {
                S.nextToken();
                params.append(lastParam = formalParameter());
            }
        }
        accept(RPAREN);
        return params.toList();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"formalParameters()");
		}
    }

    JCModifiers optFinal(long flags) {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//���������еĲ���ֻ����final��deprecated(��JAVADOC)��ָ��
		//ParserTest(/** @deprecated */ final int i){}
		//ע����������ı������ǲ�һ����
		//ParserTest(final /** @deprecated */ int i){} //�д�(����ָ�﷨���󣬶�������deprecated) mods.flags=final parameter
		//ParserTest(/** @deprecated */ final int i){} //�޴� mods.flags=final deprecated parameter
		//��Ϊ��modifiersOpt()���ȿ��Ƿ���DEPRECATED�ٽ���whileѭ����
		//��final���ȣ�����whileѭ��nextToken�����˷����Ƿ���DEPRECATED��
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }

    /** FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    JCVariableDecl formalParameter() {
    	try {//�Ҽ��ϵ�
    	DEBUG.P(this,"formalParameter()");
    	
        JCModifiers mods = optFinal(Flags.PARAMETER);
        JCExpression type = type();
        if (S.token() == ELLIPSIS) { //���һ���β���varargs�����
            checkVarargs();
            mods.flags |= Flags.VARARGS;
            type = to(F.at(S.pos()).TypeArray(type));
            S.nextToken();
        }
        return variableDeclaratorId(mods, type);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"formalParameter()");
		}        
    }

/* ---------- auxiliary methods -------------- */

    /** Check that given tree is a legal expression statement.
     */
    protected JCExpression checkExprStat(JCExpression t) {
    	/*
    	�Ϸ��ı��ʽ���:
    	++a��--a��a++��a--��
    	a=b��
    	a|=b��a^=b��a&=b��
    	a<<=b��a>>=b��a>>>=b��a+=b��a-=b��a*=b��a/=b��a%=b��
    	a(),new a()
    	*/
        switch(t.tag) {
        case JCTree.PREINC: case JCTree.PREDEC:
        case JCTree.POSTINC: case JCTree.POSTDEC:
        case JCTree.ASSIGN:
        case JCTree.BITOR_ASG: case JCTree.BITXOR_ASG: case JCTree.BITAND_ASG:
        case JCTree.SL_ASG: case JCTree.SR_ASG: case JCTree.USR_ASG:
        case JCTree.PLUS_ASG: case JCTree.MINUS_ASG:
        case JCTree.MUL_ASG: case JCTree.DIV_ASG: case JCTree.MOD_ASG:
        case JCTree.APPLY: case JCTree.NEWCLASS:
        case JCTree.ERRONEOUS:
            return t;
        default:
            log.error(t.pos, "not.stmt");
            return F.at(t.pos).Erroneous(List.<JCTree>of(t));
        }
    }

    /** Return precedence of operator represented by token,
     *  -1 if token is not a binary operator. @see TreeInfo.opPrec
     */
    static int prec(Token token) {
        int oc = optag(token);
        return (oc >= 0) ? TreeInfo.opPrec(oc) : -1;
    }

    /** Return operation tag of binary operator represented by token,
     *  -1 if token is not a binary operator.
     */
    static int optag(Token token) {
        switch (token) {
        case BARBAR:
            return JCTree.OR;
        case AMPAMP:
            return JCTree.AND;
        case BAR:
            return JCTree.BITOR;
        case BAREQ:
            return JCTree.BITOR_ASG;
        case CARET:
            return JCTree.BITXOR;//ָλ����(^)
        case CARETEQ:
            return JCTree.BITXOR_ASG;
        case AMP:
            return JCTree.BITAND;
        case AMPEQ:
            return JCTree.BITAND_ASG;
        case EQEQ:
            return JCTree.EQ;
        case BANGEQ:
            return JCTree.NE;
        case LT:
            return JCTree.LT;
        case GT:
            return JCTree.GT;
        case LTEQ:
            return JCTree.LE;
        case GTEQ:
            return JCTree.GE;
        case LTLT:
            return JCTree.SL;
        case LTLTEQ:
            return JCTree.SL_ASG;
        case GTGT:
            return JCTree.SR;
        case GTGTEQ:
            return JCTree.SR_ASG;
        case GTGTGT:
            return JCTree.USR;
        case GTGTGTEQ:
            return JCTree.USR_ASG;
        case PLUS:
            return JCTree.PLUS;
        case PLUSEQ:
            return JCTree.PLUS_ASG;
        case SUB:
            return JCTree.MINUS;
        case SUBEQ:
            return JCTree.MINUS_ASG;
        case STAR:
            return JCTree.MUL;
        case STAREQ:
            return JCTree.MUL_ASG;
        case SLASH:
            return JCTree.DIV;
        case SLASHEQ:
            return JCTree.DIV_ASG;
        case PERCENT:
            return JCTree.MOD;
        case PERCENTEQ:
            return JCTree.MOD_ASG;
        case INSTANCEOF:
            return JCTree.TYPETEST;
        default:
            return -1;
        }
    }

    /** Return operation tag of unary operator represented by token,
     *  -1 if token is not a binary operator.//binary��ĳ�unary
     */
    static int unoptag(Token token) {
        switch (token) {
        case PLUS:
            return JCTree.POS;
        case SUB:
            return JCTree.NEG;
        case BANG: //�߼���
            return JCTree.NOT;
        case TILDE: //��λȡ��(ע����һ����������磺~34����20~34�ǷǷ���)
            return JCTree.COMPL;
        case PLUSPLUS:
            return JCTree.PREINC;//����++���Ƿ���ǰ���Ƿ��ں󣬶�����PREINC
        case SUBSUB:
            return JCTree.PREDEC;//����--���Ƿ���ǰ���Ƿ��ں󣬶�����PREDEC
        default:
            return -1;
        }
    }

    /** Return type tag of basic type represented by token,
     *  -1 if token is not a basic type identifier.
     */
    static int typetag(Token token) {
        switch (token) {
        case BYTE:
            return TypeTags.BYTE;
        case CHAR:
            return TypeTags.CHAR;
        case SHORT:
            return TypeTags.SHORT;
        case INT:
            return TypeTags.INT;
        case LONG:
            return TypeTags.LONG;
        case FLOAT:
            return TypeTags.FLOAT;
        case DOUBLE:
            return TypeTags.DOUBLE;
        case BOOLEAN:
            return TypeTags.BOOLEAN;
        default:
            return -1;
        }
    }

    void checkGenerics() {
        if (!allowGenerics) {
            log.error(S.pos(), "generics.not.supported.in.source", source.name);
            allowGenerics = true;
        }
    }
    void checkVarargs() {
        if (!allowVarargs) {
            log.error(S.pos(), "varargs.not.supported.in.source", source.name);
            allowVarargs = true;
        }
    }
    void checkForeach() {
        if (!allowForeach) {
            log.error(S.pos(), "foreach.not.supported.in.source", source.name);
            allowForeach = true;
        }
    }
    void checkStaticImports() {
        if (!allowStaticImport) {
            log.error(S.pos(), "static.import.not.supported.in.source", source.name);
            allowStaticImport = true;
        }
    }
    void checkAnnotations() {
        if (!allowAnnotations) {
            log.error(S.pos(), "annotations.not.supported.in.source", source.name);
            allowAnnotations = true;
        }
    }
}
