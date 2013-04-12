/*
 * @(#)Scanner.java	1.75 07/03/21
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

import java.io.*;
import java.nio.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.channels.*;
import java.util.regex.*;

import com.sun.tools.javac.util.*;

import com.sun.tools.javac.code.Source;

import static com.sun.tools.javac.parser.Token.*;
import static com.sun.tools.javac.util.LayoutCharacters.*;

/** The lexical analyzer maps an input stream consisting of
 *  ASCII characters and Unicode escapes into a token sequence.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Scanner.java	1.75 07/03/21")
public class Scanner implements Lexer {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Scanner);//�Ҽ��ϵ�
	
    //Դ����ԭ����false
    private static boolean scannerDebug = my.Debug.DocCommentScanner;
    //private static boolean scannerDebug = true;
    
    //private static boolean scannerDebug = false;

    /** A factory for creating scanners. */
    public static class Factory {
	/** The context key for the scanner factory. */
	public static final Context.Key<Scanner.Factory> scannerFactoryKey =
	    new Context.Key<Scanner.Factory>();

	/** Get the Factory instance for this context. */
	public static Factory instance(Context context) {
            try {//�Ҽ��ϵ�
            DEBUG.P(Factory.class,"instance(1)");
            
            //���JavaCompiler.processAnnotations=trueʱ��
            //��instance��DocCommentScanner.Factory���ʵ��
	    Factory instance = context.get(scannerFactoryKey);
            DEBUG.P("instance="+instance);
	    if (instance == null)
		instance = new Factory(context);
	    return instance;
            
            }finally{//�Ҽ��ϵ�
            DEBUG.P(0,Factory.class,"instance(1)");
            }
	}

	final Log log;
	final Name.Table names;
	final Source source;
	final Keywords keywords;

	/** Create a new scanner factory. */
	protected Factory(Context context) {
            DEBUG.P(this,"Factory(1)");
            
	    context.put(scannerFactoryKey, this);
	    this.log = Log.instance(context);
	    this.names = Name.Table.instance(context);
	    this.source = Source.instance(context);
	    this.keywords = Keywords.instance(context);
            
	    DEBUG.P(0,this,"Factory(1)");
	}

        public Scanner newScanner(CharSequence input) {
        	try {//�Ҽ��ϵ�
        	DEBUG.P(this,"newScanner(1)");
        	//DEBUG.P("input instanceof CharBuffer="+(input instanceof CharBuffer));
        	/*
        	ΪʲôҪ(input instanceof CharBuffer)�أ�
        	��Ϊÿ��Ҫ�����Դ�ļ���������װ����һ
        	��JavacFileManager.RegularFileObject���ʵ�� ,
        	RegularFileObject��ʵ����JavaFileObject�ӿ�,JavaFileObject�ӿڵ�
        	�����ӿ���FileObject����FileObject�ӿ�����һ������(���ڶ�ȡ�ļ�����):
        	java.lang.CharSequence getCharContent(boolean ignoreEncodingErrors)
                                      throws java.io.IOException
                                      
            ��JavacFileManager.RegularFileObject���Ӧ��ʵ�ַ���Ϊ:
            public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors)
                                   throws java.io.IOException
                                   
            �Ƚ����������ķ���ֵ���������ܾ����е�֣���ʵ���ǺϷ��ģ�
            ��Ϊjava.nio.CharBuffer��ʵ����java.lang.CharSequence�ӿ�                   
        	*/
            if (input instanceof CharBuffer) {
                return new Scanner(this, (CharBuffer)input);
            } else {
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
            }
            
            }finally{//�Ҽ��ϵ�
            DEBUG.P(0,this,"newScanner(1)");
            }
        }

        public Scanner newScanner(char[] input, int inputLength) {
            try {//�Ҽ��ϵ�
            DEBUG.P(this,"newScanner(2)");
            
            return new Scanner(this, input, inputLength);
            
            }finally{//�Ҽ��ϵ�
            DEBUG.P(0,this,"newScanner(2)");
            }
        }
    }

    /* Output variables; set by nextToken():
     */

    /** The token, set by nextToken().
     */
    private Token token;

    /** Allow hex floating-point literals.
     */
    private boolean allowHexFloats;

    /** The token's position, 0-based offset from beginning of text.
     */
    private int pos;

    /** Character position just after the last character of the token.
     */
    private int endPos;

    /** The last character position of the previous token.
     */
    private int prevEndPos;
    
    /*����˵��:pos��endPos��prevEndPos�����ߵ�����
    ����Ҫ�����Դ���뿪ͷ���£�
    package my.test;
    
    ����scannerDebug=true������������:
    nextToken(0,7)=|package|  	tokenName=PACKAGE|  	prevEndPos=0
    processWhitespace(7,8)=| |
	nextToken(8,10)=|my|   		tokenName=IDENTIFIER|  	prevEndPos=7
	nextToken(10,11)=|.|  		tokenName=DOT|  		prevEndPos=10
	nextToken(11,15)=|test|  	tokenName=IDENTIFIER|  	prevEndPos=11
	nextToken(15,16)=|;|  		tokenName=SEMI|  		prevEndPos=15
	
	���е�(0,7)��(8,10)��(10,11)��(11,15)��(15,16)���Ǵ���(pos,endPos)��
	endPos�������λ���ϵ��ַ������ǵ�ǰToken�����һ���ַ���������һ
	��Token����ʼ�ַ����߿հס����С�ע���ĵ����ȡ�
	
	���⣬prevEndPos����ָ��ǰһ��Token��endPos��prevEndPos����ָ��
	�հס����С�ע���ĵ���endPos��
	��processWhitespace(7,8)��endPos��8�����Ǵ�ʱprevEndPos=7
	*/


    /** The position where a lexical error occurred;
     */
    private int errPos = Position.NOPOS;

    /** The name of an identifier or token:
     */
    private Name name;

    /** The radix of a numeric literal token.
     */
    private int radix;

    /** Has a @deprecated been encountered in last doc comment?
     *  this needs to be reset by client.
     */
    protected boolean deprecatedFlag = false;

    /** A character buffer for literals.
     */
    private char[] sbuf = new char[128];//�ַ����棬�ᾭ�����
    private int sp;

    /** The input buffer, index of next chacter to be read,
     *  index of one past last character in buffer.
     */
    private char[] buf;//���Դ��������
    private int bp;
    private int buflen;
    private int eofPos;

    /** The current character.
     */
    private char ch;

    /** The buffer index of the last converted unicode character
     */
    private int unicodeConversionBp = -1;

    /** The log to be used for error reporting.
     */
    private final Log log;

    /** The name table. */
    private final Name.Table names;

    /** The keyword table. */
    private final Keywords keywords;

    /** Common code for constructors. */
    private Scanner(Factory fac) {
	this.log = fac.log;
	this.names = fac.names;
	this.keywords = fac.keywords;
	//16���Ƹ�����ֻ��>=JDK1.5�ſ�����
	this.allowHexFloats = fac.source.allowHexFloats();
    }

    private static final boolean hexFloatsWork = hexFloatsWork();
    private static boolean hexFloatsWork() {
        try {
            Float.valueOf("0x1.0p1");
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /** Create a scanner from the input buffer.  buffer must implement
     *  array() and compact(), and remaining() must be less than limit().
     */
    protected Scanner(Factory fac, CharBuffer buffer) {
	this(fac, JavacFileManager.toArray(buffer), buffer.limit());
    }

    /**
     * Create a scanner from the input array.  This method might
     * modify the array.  To avoid copying the input array, ensure
     * that {@code inputLength < input.length} or
     * {@code input[input.length -1]} is a white space character.
     * 
     * @param fac the factory which created this Scanner
     * @param input the input, might be modified
     * @param inputLength the size of the input.
     * Must be positive and less than or equal to input.length.
     */
    protected Scanner(Factory fac, char[] input, int inputLength) {
        this(fac);

        DEBUG.P(this,"Scanner(3) Դ�ļ�����Ԥ��......");
		//2009-3-14�޸ģ����ڵĽ��Ͳ���ȫ��ȷ���뿴B50���е�ע�ͣ�
		//��com.sun.tools.javac.file.JavacFileManager===>toArray(1)


        //input�ַ������д�ŵ�������Դ�ļ�����ȫһ������Դ�ļ�����10��
        //null�ַ�(10���Ƶ���0),����Դ�ļ�������ϵ�,
        //inputLength��Դ�ļ����ݵĳ���,input.lengthһ�����inputLength+10
    	//DEBUG.P(new String(input)+"");
    	//DEBUG.P("---------------------------");
    	//DEBUG.P("buffer.limit="+inputLength);
    	//DEBUG.P("input.length="+input.length);
        
        DEBUG.P("Դ�ļ�����="+inputLength);
    	DEBUG.P("�����󳤶�="+input.length);
    	
        eofPos = inputLength;
        //�������ֻ��Ϊ�˷�����buf������EOI�����е����⴦��
	//���char[] input������CharBuffer bufferת�������ľͻ���inputLength == input.length
        if (inputLength == input.length) {
                        //�鿴java.lang.Character��isWhitespace()����,null�ַ��᷵��false
            if (input.length > 0 && Character.isWhitespace(input[input.length - 1])) {
                inputLength--;
            } else {
                char[] newInput = new char[inputLength + 1];
                System.arraycopy(input, 0, newInput, 0, input.length);
                input = newInput;
            }
        }
        buf = input;
        buflen = inputLength;
        buf[buflen] = EOI;//EOI��com.sun.tools.javac.util.LayoutCharacters����
        bp = -1;
        scanChar();

        DEBUG.P("scan first char="+ch);
	DEBUG.P("bp="+bp);
	DEBUG.P("endPos="+endPos);
	DEBUG.P("errPos="+errPos);
	DEBUG.P("pos="+pos);
	DEBUG.P("prevEndPos="+prevEndPos);
	DEBUG.P("sp="+sp);
	DEBUG.P("unicodeConversionBp="+unicodeConversionBp); 
        DEBUG.P(0,this,"Scanner(3)");
    }

    /** Report an error at the given position using the provided arguments.
     */
    private void lexError(int pos, String key, Object... args) {
	DEBUG.P(this,"lexError(3)");
    	DEBUG.P("key="+key);
        DEBUG.P("pos="+pos+" bp="+bp);

        log.error(pos, key, args);
        token = ERROR;
        errPos = pos;

        DEBUG.P(0,this,"lexError(3)");
    }

    /** Report an error at the current token position using the provided
     *  arguments.
     */
    private void lexError(String key, Object... args) {
        DEBUG.P(this,"lexError(2)");
    	DEBUG.P("key="+key);
        DEBUG.P("pos="+pos+" bp="+bp);
        if(args!=null && args.length>0) DEBUG.P("args[0]="+args[0]);
        
	lexError(pos, key, args);
        
        DEBUG.P(0,this,"lexError(2)");
    }

    /** Convert an ASCII digit from its base (8, 10, or 16)
     *  to its value.
     */
    private int digit(int base) {//��16���Ƶ�A��ת����10
	char c = ch;
	int result = Character.digit(c, base);
	if (result >= 0 && c > 0x7f) {
		//��:int aaa= 12\u06604; //�Ƿ��ķ� ASCII ����
		//��Character���isDigit����
	    lexError(pos+1, "illegal.nonascii.digit");
	    ch = "0123456789abcdef".charAt(result);
	}
	return result;
    }

    /** Convert unicode escape; bp points to initial '\' character
     *  (Spec 3.3).
     */
    private void convertUnicode() {
	//ch��������ַ�����buf[bp]
        try {//�Ҽ��ϵ�
	DEBUG.P(this,"convertUnicode()");
        DEBUG.P("ch="+ch+" bp="+bp+" unicodeConversionBp="+unicodeConversionBp);
        //ע�⣬��Ϊ'\\'Ҳ������'\\u005C'��ʾ(Ҳ����б�ߵ�Unicode����005C)��
	//������'\\u005Cu0012'�Ͳ�����\\u0012�ˣ���ʱ�Ƚ���'\\u005C'��ʹ��
	//ch='\\'��unicodeConversionBp = bp
	if (ch == '\\' && unicodeConversionBp != bp) {
	    bp++; ch = buf[bp];
	   	/*
	    (ע:ע�����\\u����������\�����ֻ��һ��\����unicode�ַ�����������������ط�һ��
	    unicode�ַ�ֻ������\\u��ͷ,������\\U(��д��U)��ͷ
	    */
	    if (ch == 'u') {//��\������ԽӲ�ֻһ��u
		do {
		    bp++; ch = buf[bp];
		} while (ch == 'u');
		//ÿһ��unicodeռ4��16�����ַ���
		//��Ϊ����whileʱ�Ѷ���һ��������ֻ��3
		int limit = bp + 3;
		if (limit < buflen) {
		    int d = digit(16);
		    int code = d;
		    while (bp < limit && d >= 0) {
			bp++; ch = buf[bp];
			d = digit(16);
			DEBUG.P("d1="+d);
			code = (code << 4) + d;
			//�Ӹ�λ����λ���μ���10����ֵ,
			//��Ϊһ��16�����ַ���4���������ַ���ʾ������ÿ������4λ��
			//�൱��10����ֵÿ�γ���16
			/*
			����:
			unicod��:   \uA971
			10������:   10*16*16*16 + 9*16*16 + 7*16 + 1
			            =(10*16 + 9)*16*16 + 7*16 + 1
			            =((10*16 + 9)*16 + 7)*16 + 1
			            =((10<<4 + 9)<<4 + 7)<<4 + 1
			            
			���ö�Ӧ��ʽ:(code << 4) + d;
			*/
		    }
		    DEBUG.P("d2="+d);
		    if (d >= 0) {
			ch = (char)code;
			unicodeConversionBp = bp;
			return;
		    }
		}
			//�Ƿ��� Unicode ת��,�������g�ǷǷ���
			//public int myInt='\\uuuuugfff';
            //                         ^  
		lexError(bp, "illegal.unicode.esc");
	    } else {
	    //���'\'�ַ����治��'u'��˵������Unicode��������һλ
		bp--;
		ch = '\\';
	    }
	}
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"convertUnicode()");
        } 
    }

    /** Read next character.
     */
    private void scanChar() {
        //try {//�Ҽ��ϵ�
	//DEBUG.P(this,"scanChar()");
        
	ch = buf[++bp];
	if (ch == '\\') {
	    convertUnicode();
	}
        
        //}finally{//�Ҽ��ϵ�
        //DEBUG.P(0,this,"scanChar()");
        //}
    }

    /** Read next character in comment, skipping over double '\' characters.
     */
    private void scanCommentChar() {
	//DEBUG.P(this,"scanCommentChar()");
	scanChar();
	//DEBUG.P("ch="+ch);
	//DEBUG.P("bp="+bp);
	//DEBUG.P("unicodeConversionBp="+unicodeConversionBp);
	if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		bp++;
	    } else {
		convertUnicode();
	    }
	}
	//DEBUG.P(0,this,"scanCommentChar()");
    }

    /** Append a character to sbuf.
     */
    private void putChar(char ch) {
	if (sp == sbuf.length) {
	    char[] newsbuf = new char[sbuf.length * 2];
	    System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
	    sbuf = newsbuf;
	}
	sbuf[sp++] = ch;
    }

    /** For debugging purposes: print character.
     */
    private void dch() {
        System.err.print(ch); System.out.flush();
    }

    /** Read next character in character or string literal and copy into sbuf.
     */
    private void scanLitChar() {
        if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		bp++;
		putChar('\\');
		scanChar();
	    } else {
		scanChar();
		switch (ch) {
		case '0': case '1': case '2': case '3':
		case '4': case '5': case '6': case '7':
		    char leadch = ch;
		    int oct = digit(8);
		    scanChar();
		    if ('0' <= ch && ch <= '7') {
			oct = oct * 8 + digit(8);
			scanChar();
			//��\��ʾ8���Ƶ��ַ�ʱ����8���Ƶ��ַ�ռ3λʱ��Ϊ�ε�һλleadch <= '3' ????
			//(2007-06-15 10:37���������)
			//��JLS3 3.10.6. Escape Sequences for Character and String Literals
			//��\��ʾ8���Ƶ��ַ�ʱ��ֻ�ܱ�ʾ\u0000 �� \u00ff���ַ�
			//\377�պö�Ӧ\u00ff
			if (leadch <= '3' && '0' <= ch && ch <= '7') {
			    oct = oct * 8 + digit(8);
			    scanChar();
			}
		    }
		    putChar((char)oct);
		    break;
		case 'b':
		    putChar('\b'); scanChar(); break;
		case 't':
		    putChar('\t'); scanChar(); break;
		case 'n':
		    putChar('\n'); scanChar(); break;
		case 'f':
		    putChar('\f'); scanChar(); break;
		case 'r':
		    putChar('\r'); scanChar(); break;
		case '\'':
		    putChar('\''); scanChar(); break;
		case '\"':
		    putChar('\"'); scanChar(); break;
		case '\\':
		    putChar('\\'); scanChar(); break;
		default:
                    //�Ƿ�ת���ַ� ���磺char c='\w';
 		    lexError(bp, "illegal.esc.char");
		}
	    }
	} else if (bp != buflen) {
            putChar(ch); scanChar();
        }
    }

    /** Read fractional part of hexadecimal floating point number.
     */
    private void scanHexExponentAndSuffix() {
    	//16���Ƹ���ָ������(ע:p(��P)������ָ��,����ʡ��,�����float������f(��F)Ҳ�Ǳ����)
        if (ch == 'p' || ch == 'P') {
            // <editor-fold defaultstate="collapsed">
            putChar(ch);
            scanChar();
            
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }

            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');

                if (!allowHexFloats) {
                    //����:0x.1p-1f����ָ��ѡ��:-source 1.4ʱ
                    //����:�� -source 5 ֮ǰ����֧��ʮ�����Ƹ�������ֵ
                    lexError("unsupported.fp.lit");
                    allowHexFloats = true;
                }
                else if (!hexFloatsWork)
                    //�� VM ��֧��ʮ�����Ƹ�������ֵ
                    lexError("unsupported.cross.fp.lit");
            } else
                //��:0x.1p-wf���ַ�w��������0-9������������:��������ֵ������
                //�������:0x.1p-2wf����Ȼ�ַ�w��������0-9�����������ﱨ��
                //����ֻ���+-�ź�����ַ��Ƿ�������
                lexError("malformed.fp.lit");
            // </editor-fold>
        } else {
            //��:0x.1-1f�������ַ�p(��P)������������:��������ֵ������
            lexError("malformed.fp.lit");
        }
        if (ch == 'f' || ch == 'F') {
            putChar(ch);
            scanChar();
            token = FLOATLITERAL;
        } else {
            /*
            �����������û��ָ����׺f(��F)����ô������������˫���ȵģ�
            ��ʱ���������ֵ��һ��float���͵��ֶΣ��������������ط�(Check����)���飬
            �磺public float myFloat2=0x.1p-2;

            ������ʾ����:

            bin\mysrc\my\test\ScannerTest.java:9: ������ʧ����
            �ҵ��� double
            ��Ҫ�� float
                            public float myFloat2=0x.1p-2;
                                                                      ^
            1 ����
            */
            if (ch == 'd' || ch == 'D') {
                putChar(ch);
                scanChar();
            }
            token = DOUBLELITERAL;
        }
    }

    /** Read fractional part of floating point number.
     */
    private void scanFraction() {
        while (digit(10) >= 0) {
            putChar(ch);
            scanChar();
        }
        
        int sp1 = sp;
        if (ch == 'e' || ch == 'E') {
            putChar(ch);
            scanChar();
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }
	   		
            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');
                return;
            }
            //��:1.2E+w���ַ�w��������0-9������������:��������ֵ������
            lexError("malformed.fp.lit");
            sp = sp1;
        }
    }

    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanFractionAndSuffix() {
	this.radix = 10;
	scanFraction();
	if (ch == 'f' || ch == 'F') {
	    putChar(ch);
	    scanChar();
            token = FLOATLITERAL;
	} else {
	    if (ch == 'd' || ch == 'D') {
		putChar(ch);
		scanChar();
	    }
	    token = DOUBLELITERAL;
	}
    }

    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanHexFractionAndSuffix(boolean seendigit) {
	this.radix = 16;
	assert ch == '.';
	putChar(ch);
	scanChar();
        //DEBUG.P("ch="+ch+" digit(16)="+digit(16));
        while (digit(16) >= 0) {
	    seendigit = true;
	    putChar(ch);
            scanChar();
        }
	if (!seendigit)
	    lexError("invalid.hex.number");//ʮ���������ֱ����������һλʮ��������,������:0x.p-1f;
	else
	    scanHexExponentAndSuffix();
    }

    /** Read a number.
     *  @param radix  The radix of the number; one of 8, 10, 16.
     */
     //�ڴʷ������׶β�����������Ƿ�Ϸ���������Parser��literal�����м��
     //������8���Ʊ�ʾ����int i=078;
    private void scanNumber(int radix) {
	this.radix = radix;
	// for octal, allow base-10 digit in case it's a float literal
	int digitRadix = (radix <= 10) ? 10 : 16;
	boolean seendigit = false;
	while (digit(digitRadix) >= 0) {
	    seendigit = true;
	    putChar(ch);
	    scanChar();
	}
	if (radix == 16 && ch == '.') {
	    scanHexFractionAndSuffix(seendigit);
	} else if (seendigit && radix == 16 && (ch == 'p' || ch == 'P')) {
            //��:0x1p-1f�����
	    scanHexExponentAndSuffix();
	} else if (radix <= 10 && ch == '.') {
            //����������������float f=00001.2f;(�����ж��0��ͷ)
	    putChar(ch);
	    scanChar();
	    scanFractionAndSuffix();
	} else if (radix <= 10 &&
		   (ch == 'e' || ch == 'E' ||
		    ch == 'f' || ch == 'F' ||
		    ch == 'd' || ch == 'D')) {
		//��: 2e2f��2f��2d
	    scanFractionAndSuffix();
	} else {
	    if (ch == 'l' || ch == 'L') {
		scanChar();
		token = LONGLITERAL;
	    } else {
		token = INTLITERAL;
	    }
	}
    }

    /** Read an identifier.
     */
    private void scanIdent() {
	    DEBUG.P("scanIdent()=>ch="+ch);
	boolean isJavaIdentifierPart;
	char high;
	do {
            //ÿ�ζ���if�ж�һ�±�ÿ�ζ�����putChar(ch)Ч�ʸ���
	    if (sp == sbuf.length) putChar(ch); else sbuf[sp++] = ch;
	    // optimization, was: putChar(ch);

	    scanChar();
	    DEBUG.P("ch="+ch);
	    switch (ch) {
	    case 'A': case 'B': case 'C': case 'D': case 'E':
	    case 'F': case 'G': case 'H': case 'I': case 'J':
	    case 'K': case 'L': case 'M': case 'N': case 'O':
	    case 'P': case 'Q': case 'R': case 'S': case 'T':
	    case 'U': case 'V': case 'W': case 'X': case 'Y':
	    case 'Z':
	    case 'a': case 'b': case 'c': case 'd': case 'e':
	    case 'f': case 'g': case 'h': case 'i': case 'j':
	    case 'k': case 'l': case 'm': case 'n': case 'o':
	    case 'p': case 'q': case 'r': case 's': case 't':
	    case 'u': case 'v': case 'w': case 'x': case 'y':
	    case 'z':
	    case '$': case '_':
	    case '0': case '1': case '2': case '3': case '4':
	    case '5': case '6': case '7': case '8': case '9':
            case '\u0000': case '\u0001': case '\u0002': case '\u0003':
            case '\u0004': case '\u0005': case '\u0006': case '\u0007':
            case '\u0008': case '\u000E': case '\u000F': case '\u0010':
            case '\u0011': case '\u0012': case '\u0013': case '\u0014':
            case '\u0015': case '\u0016': case '\u0017':
            case '\u0018': case '\u0019': case '\u001B':
            case '\u007F':
		break;
            case '\u001A': // EOI is also a legal identifier part
	    //��Դ�ļ�ֻ��һ�С�int a\u001A��ʱ
	    /*
		scanIdent()=>ch=a
		com.sun.tools.javac.parser.DocCommentScanner===>convertUnicode()
		-------------------------------------------------------------------------
		ch=\ bp=5 unicodeConversionBp=-1
		d1=0
		d1=1
		d1=10
		d2=10
		com.sun.tools.javac.parser.DocCommentScanner===>convertUnicode()  END
		-------------------------------------------------------------------------

		ch=
		bp >= buflen=false
		ch=
		bp >= buflen=true
		name=a
		token=IDENTIFIER
		nextToken(4,11)=|a\u001A|  tokenName=|IDENTIFIER|  prevEndPos=3
		nextToken(11,11)=||  tokenName=|EOF|  prevEndPos=11
	*/
	    DEBUG.P("bp >= buflen="+(bp >= buflen));
                if (bp >= buflen) {
                    name = names.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
		    DEBUG.P("name="+name);
                    DEBUG.P("token="+token);
                    return;
                }
                break;
	    default:
                if (ch < '\u0080') {
                    // all ASCII range chars already handled, above
                    isJavaIdentifierPart = false;
                } else {//�����������ı�������������ı�����һ����HighSurrogate
		    high = scanSurrogates();
                    if (high != 0) {
	                if (sp == sbuf.length) {
                            putChar(high);
                        } else {
                            sbuf[sp++] = high;
                        }
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(
                            Character.toCodePoint(high, ch));
                    } else {
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(ch);
                    }
                }
                //���isJavaIdentifierPartΪfalse�������ʶ��ʶ�����
		if (!isJavaIdentifierPart) {
                    //��ʶ��ʶ�������name����
		    name = names.fromChars(sbuf, 0, sp);
		    token = keywords.key(name);
                    DEBUG.P("name="+name);
                    DEBUG.P("token="+token);
		    return;
		}
	    }
	} while (true);
    }

    /** Are surrogates supported?
     */
    final static boolean surrogatesSupported = surrogatesSupported();
    private static boolean surrogatesSupported() {
        try {
            Character.isHighSurrogate('a');
            return true;
        } catch (NoSuchMethodError ex) {
            return false;
        }
    }

    /** Scan surrogate pairs.  If 'ch' is a high surrogate and
     *  the next character is a low surrogate, then put the low
     *  surrogate in 'ch', and return the high surrogate.
     *  otherwise, just return 0.
     */
    //�����ע�Ͳ�ȫ����������
    //�����ǰ��ch����HighSurrogate������0��
    //�����ǰ��ch��HighSurrogate�������ж���һ���ַ��Ƿ���LowSurrogate��
    //�ǵĻ��ͷ���high�����ǵĻ���ch��Ϊhigh����󷵻�0
    private char scanSurrogates() {
        try {//�Ҽ��ϵ�
        DEBUG.P(this,"scanSurrogates()");
        DEBUG.P("surrogatesSupported="+surrogatesSupported);
    	DEBUG.P("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
        DEBUG.P("Character.isHighSurrogate(ch)="+Character.isHighSurrogate(ch));
        DEBUG.P("Character.isJavaIdentifierStart(ch)="+Character.isJavaIdentifierStart(ch));
        DEBUG.P("Character.isJavaIdentifierPart(ch)="+Character.isJavaIdentifierPart(ch));
        
        if (surrogatesSupported && Character.isHighSurrogate(ch)) {
            char high = ch;

            scanChar();
            
            DEBUG.P("next ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
            DEBUG.P("Character.isLowSurrogate(ch)="+Character.isLowSurrogate(ch));

            if (Character.isLowSurrogate(ch)) {
                return high;
            }

            ch = high;
        }

        return 0;
        
        } finally {
        DEBUG.P(0,this,"scanSurrogates()");
        }
    }

    /** Return true if ch can be part of an operator.
     */
    private boolean isSpecial(char ch) {
        switch (ch) {
        case '!': case '%': case '&': case '*': case '?':
        case '+': case '-': case ':': case '<': case '=':
        case '>': case '^': case '|': case '~':
	case '@':
            return true;
        default:
            return false;
        }
    }

    /** Read longest possible sequence of special characters and convert
     *  to token.
     */
    private void scanOperator() {
	while (true) {
	    putChar(ch);
	    Name newname = names.fromChars(sbuf, 0, sp);
	    
	    //DEBUG.P("newname="+newname);
	    //���һ���ַ�����Ϊһ�������Ĳ�������һ���֣������ܵİ����ӵ��������У�
	    //������������ַ�ʹ��ԭ���Ĳ����������һ����ʶ���ˣ���ô������һ��
            //��:������ǰ�����Ĳ�����Ϊ��!="�����Ŷ����ַ���*������ˡ�!=*"������һ
            //����ʶ��(IDENTIFIER)�ˣ���ʱ�͵�������һ�񣬻�ԭ�ɡ�!="
            if (keywords.key(newname) == IDENTIFIER) {
                sp--;
                break;
	    }
	    
            name = newname;
            token = keywords.key(newname);
	    scanChar();
	    if (!isSpecial(ch)) break;
	}
    }

    /**
     * Scan a documention comment; determine if a deprecated tag is present.
     * Called once the initial /, * have been skipped, positioned at the second *
     * (which is treated as the beginning of the first line).
     * Stops positioned at the closing '/'.
     */
    @SuppressWarnings("fallthrough")
    private void scanDocComment() {
        try {//�Ҽ��ϵ�
	DEBUG.P(this,"scanDocComment()");
	DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
        
	boolean deprecatedPrefix = false;

	forEachLine:
	while (bp < buflen) {

	    // Skip optional WhiteSpace at beginning of line
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    // Skip optional consecutive Stars
	    while (bp < buflen && ch == '*') {
		scanCommentChar();
		if (ch == '/') {
		    return;
		}
	    }
	
	    // Skip optional WhiteSpace after Stars
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    deprecatedPrefix = false;
	    // At beginning of line in the JavaDoc sense.
	    if (bp < buflen && ch == '@' && !deprecatedFlag) {
		scanCommentChar();
		if (bp < buflen && ch == 'd') {
		    scanCommentChar();
		    if (bp < buflen && ch == 'e') {
			scanCommentChar();
			if (bp < buflen && ch == 'p') {
			    scanCommentChar();
			    if (bp < buflen && ch == 'r') {
				scanCommentChar();
				if (bp < buflen && ch == 'e') {
				    scanCommentChar();
				    if (bp < buflen && ch == 'c') {
					scanCommentChar();
					if (bp < buflen && ch == 'a') {
					    scanCommentChar();
					    if (bp < buflen && ch == 't') {
						scanCommentChar();
						if (bp < buflen && ch == 'e') {
						    scanCommentChar();
						    if (bp < buflen && ch == 'd') {
							deprecatedPrefix = true;
							scanCommentChar();
						    }}}}}}}}}}}
            
            //DEBUG.P("deprecatedPrefix="+deprecatedPrefix);
            //DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
            
	    if (deprecatedPrefix && bp < buflen) {
		if (Character.isWhitespace(ch)) {
		    deprecatedFlag = true;
		} else if (ch == '*') {
		    scanCommentChar();
		    if (ch == '/') {
			deprecatedFlag = true;
			return;
		    }
		}
	    }
            
            //DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);

	    // Skip rest of line
	    while (bp < buflen) {
		switch (ch) {
		case '*':
		    scanCommentChar();
		    if (ch == '/') {
			return;
		    }
		    break;
		case CR: // (Spec 3.4)
		    scanCommentChar();
		    if (ch != LF) {
                        continue forEachLine;
		    }
                    //��Ϊ����û��break��䣬�����ֿ���ִ��continue��
                    //�Ӷ����������case LF��������ִ�в��ˣ����Ա������ᾯ��
                    //���棺[fallthrough] �����޷�ʵ�� case
                    //fallthrough ������˼��:ʧ��,���
                    
		    /* fall through to LF case */
		case LF: // (Spec 3.4)
		    scanCommentChar();
		    continue forEachLine;
		default:
		    scanCommentChar();
		}
	    } // rest of line
	} // forEachLine
	return;//��������
        
        }finally{//�Ҽ��ϵ�
        DEBUG.P("deprecatedFlag="+deprecatedFlag);
        DEBUG.P(0,this,"scanDocComment()");
        } 
    }

    /** The value of a literal token, recorded as a string.
     *  For integers, leading 0x and 'l' suffixes are suppressed.
     */
    public String stringVal() {
	return new String(sbuf, 0, sp);
    }

    /** Read token.
     */
    public void nextToken() {

	try {
	    prevEndPos = endPos;
	    sp = 0;
	
	    while (true) {//     
	    //������processWhiteSpace()��processLineTerminator()����
	    //�����󣬼�������ɨ���ַ�
		pos = bp;
		switch (ch) {
                //���ƥ��һ��case����û��break����ô�����������case�����Ƿ�ƥ�䶼ִ��
                //�Ǹ�case�������䣬ֱ����breakΪֹ�����磺��ch=' '(�ո�)ʱ������ִ��
                //DEBUG.P("�ո�"),DEBUG.P("Tab"),DEBUG.P("��ҳ")�������doѭ��
		case ' ': //DEBUG.P("�ո�");// (Spec 3.6)
                //(ע��:����NetBeans��дjavaԴ����ʱ������Tab��ʱ��Ĭ������4���ո�)
		case '\t': //DEBUG.P("Tab");// (Spec 3.6)
		case FF: //DEBUG.P("��ҳ");// (Spec 3.6)   //form feed��ָ��ҳ
                    // <editor-fold defaultstate="collapsed">
		    do {
			scanChar();
		    } while (ch == ' ' || ch == '\t' || ch == FF);
		    endPos = bp;
                    //�������ŵ���Unicode�ַ�ʱ����ӡ��������Ϣ�Ͳ���ȷ����
                    //��ΪҪ��ʾһ��Unicode�ַ�����Ҫ6λ��bpҲҪ���ټ�6��
                    //�Ӷ�pos��endPos(Ҳ����bp)֮����ַ������˱�ʾUnicode�ַ���λ��
                    //������룺int \uD800\uDC00;
                    //���:processWhitespace(1952,1958)=| \\uD80|
                    //(ע�������ֻ��һ��\���Ҷ����һ��\��������ת�壬��convertUnicode())
		    //��unicodeConversionBp == bpʱ˵����ǰ��ch����Unicode�ַ���
		    if(unicodeConversionBp == bp) {//�Ҽ��ϵ�
			    endPos = bp+1;
			    DEBUG.P("ch="+ch);
		    }
		    processWhiteSpace();
		    break;
		case LF: // (Spec 3.4)   //����,�е�ϵͳ���ɵ��ļ�����û�лس���
                    //DEBUG.P("����");
		    scanChar();
		    endPos = bp;
		    processLineTerminator();
		    break;
		case CR: // (Spec 3.4)   //�س�,�س�����������з�
                    //DEBUG.P("�س�");
		    scanChar();
		    if (ch == LF) {
                        //DEBUG.P("����");
			scanChar();
		    }
		    endPos = bp;
		    processLineTerminator();
		    break;
                    // </editor-fold>
		//����java��ʶ��(������)������ĸ�����֮һ
		case 'A': case 'B': case 'C': case 'D': case 'E':
		case 'F': case 'G': case 'H': case 'I': case 'J':
		case 'K': case 'L': case 'M': case 'N': case 'O':
		case 'P': case 'Q': case 'R': case 'S': case 'T':
		case 'U': case 'V': case 'W': case 'X': case 'Y':
		case 'Z':
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'f': case 'g': case 'h': case 'i': case 'j':
		case 'k': case 'l': case 'm': case 'n': case 'o':
		case 'p': case 'q': case 'r': case 's': case 't':
		case 'u': case 'v': case 'w': case 'x': case 'y':
		case 'z':
		case '$': case '_':
		    scanIdent();
		    return;
		case '0': //16��8�����������
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == 'x' || ch == 'X') {
			scanChar();
			if (ch == '.') {
                            //����Ϊfalse��ʾ��С����֮ǰû������
			    scanHexFractionAndSuffix(false);
			} else if (digit(16) < 0) {
                            //��: 0x��0xw ����:ʮ���������ֱ����������һλʮ��������
			    lexError("invalid.hex.number");
			} else {
			    scanNumber(16);
			}
		    } else {
			putChar('0');
			scanNumber(8);
		    }
		    return;
                    // </editor-fold>
		case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		    scanNumber(10);
		    return;
		case '.':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if ('0' <= ch && ch <= '9') { //����:float f=.0f;
			putChar('.');
			scanFractionAndSuffix();
		    } else if (ch == '.') {  //����Ƿ���ʡ�Է���(...)
			putChar('.'); putChar('.');
			scanChar();
			if (ch == '.') {
			    scanChar();
			    putChar('.');
			    token = ELLIPSIS;
			} else {  //������Ϊ�Ǹ������
			    lexError("malformed.fp.lit");
			}
		    } else {
			token = DOT;
		    }
		    return;
                    // </editor-fold>
		case ',':
		    scanChar(); token = COMMA; return;
		case ';':
		    scanChar(); token = SEMI; return;
		case '(':
		    scanChar(); token = LPAREN; return;
		case ')':
		    scanChar(); token = RPAREN; return;
		case '[':
		    scanChar(); token = LBRACKET; return;
		case ']':
		    scanChar(); token = RBRACKET; return;
		case '{':
		    scanChar(); token = LBRACE; return;
		case '}':
		    scanChar(); token = RBRACE; return;
		case '/':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == '/') {
                        do {
                            scanCommentChar();
                        } while (ch != CR && ch != LF && bp < buflen);
                        
                        DEBUG.P("bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
                        //�����ע�������һ�У����ٴ���
                        if (bp < buflen) {
                            endPos = bp;
                            processComment(CommentStyle.LINE);
                        }
                        break;
		    } else if (ch == '*') {
                        scanChar();
                        CommentStyle style;

                        if (ch == '*') {
                            style = CommentStyle.JAVADOC;
                            scanDocComment();
                        } else {
                            style = CommentStyle.BLOCK;

                            while (bp < buflen) {
                                if (ch == '*') {
                                    scanChar();
                                    if (ch == '/') break;
                                } else {
                                    scanCommentChar();
                                }
                            }
                        }

                        if (ch == '/') {
                            scanChar();
                            endPos = bp;
                            processComment(style);
                            break;
                        } else {
                            //δ������ע��
                            lexError("unclosed.comment");
                            return;
                        }
		    } else if (ch == '=') {
			name = names.slashequals;
			token = SLASHEQ;
			scanChar();
		    } else {
			name = names.slash;
			token = SLASH;
		    }
		    return;
                    // </editor-fold>
		case '\'':  //�ַ����ַ��������ܿ���
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == '\'') {
                        //����:char c='';
			lexError("empty.char.lit");  //���ַ�����ֵ
		    } else {
			if (ch == CR || ch == LF)
			    lexError(pos, "illegal.line.end.in.char.lit");//�ַ�����ֵ���н�β���Ϸ�
			scanLitChar();
			if (ch == '\'') {
			    scanChar();
			    token = CHARLITERAL;
			} else {
				//�硰 '8p ����δ�������ַ�����ֵ
			    lexError(pos, "unclosed.char.lit");
			}
		    }
		    return;
                    // </editor-fold>
		case '\"':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    while (ch != '\"' && ch != CR && ch != LF && bp < buflen)
			scanLitChar();
		    if (ch == '\"') {
			token = STRINGLITERAL;
			scanChar();
		    } else {
			lexError(pos, "unclosed.str.lit");
		    }
		    return;
                    // </editor-fold>
		default:
                    // <editor-fold defaultstate="collapsed">
		    if (isSpecial(ch)) { //������Ϊ��������ĳһ���ֵ��ַ�
			scanOperator();
		    } else {
		    	//���ﴦ�������ַ�,�����ı���֮���
		    	//��scanIdent()����ͬ�Ĳ���
		    	//ע��������Start����scanIdent()��Part
                        boolean isJavaIdentifierStart;
                        if (ch < '\u0080') {
                            // all ASCII range chars already handled, above
                            isJavaIdentifierStart = false;
                        } else {
                            char high = scanSurrogates();
                            DEBUG.P("high="+high+"(0x"+Integer.toHexString(high).toUpperCase()+") ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
                            if (high != 0) {
                                if (sp == sbuf.length) {
                                    putChar(high);
                                } else {
                                    sbuf[sp++] = high;
                                }

                                isJavaIdentifierStart = Character.isJavaIdentifierStart(
                                Character.toCodePoint(high, ch));
                            } else {
                                // <editor-fold defaultstate="collapsed">
                                /*
                                ���isHighSurrogate(ch)=true��isLowSurrogate(ch)=true
                                ��ôisJavaIdentifierStart(ch)=false �� isJavaIdentifierPart(ch)=false
                                �����ǲ��Դ���:
                                public static void isHighSurrogate() {
                                    char ch='\uD800';//ch >= '\uD800' && ch <= '\uDBFF'
                                    while(ch <= '\uDBFF') {
                                        //System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") "+Character.isJavaIdentifierStart(ch));

                                        if(Character.isJavaIdentifierStart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");

                                        if(Character.isJavaIdentifierPart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
                                        ch++;
                                    }
                                }

                                public static void isLowSurrogate() {
                                    char ch='\uDC00';//ch >= '\uDC00' && ch <= '\uDFFF'
                                    while(ch <= '\uDFFF') {
                                        if(Character.isJavaIdentifierStart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");

                                        if(Character.isJavaIdentifierPart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
                                        ch++;
                                    }
                                }
                                */
                                // </editor-fold>
                                isJavaIdentifierStart = Character.isJavaIdentifierStart(ch);
                            }
                        }

                        if (isJavaIdentifierStart) {
                            scanIdent();
		        } else if (bp == buflen || ch == EOI && bp+1 == buflen) { // JLS 3.5
                            token = EOF;
                            pos = bp = eofPos;
		        } else {
                            //��: public char \u007fmyField12
                            //����:�Ƿ��ַ��� \127
                            lexError("illegal.char", String.valueOf((int)ch));
                            scanChar();
		        }
		    }
		    return;
                    // </editor-fold>
		}//switch
	    }//while
	} finally {
	    endPos = bp;
	    /*
	    if (scannerDebug)
		System.out.println("nextToken(" + pos
				   + "," + endPos + ")=|" +
				   new String(getRawCharacters(pos, endPos))
				   + "|");
            */

            //�Ҷ����tokenName=...(����鿴���Խ��)
        if (scannerDebug)
            System.out.println("nextToken(" + pos
                               + "," + endPos + ")=|" +
                               new String(getRawCharacters(pos, endPos))
                               + "|  tokenName=|"+token+ "|  prevEndPos="+prevEndPos);
	}
    }

    /** Return the current token, set by nextToken().
     */
    public Token token() {
        return token;
    }

    /** Sets the current token.
     */
    public void token(Token token) {
        this.token = token;
    }

    /** Return the current token's position: a 0-based
     *  offset from beginning of the raw input stream
     *  (before unicode translation)
     */
    public int pos() {
        return pos;
    }

    /** Return the last character position of the current token.
     */
    public int endPos() {
        return endPos;
    }

    /** Return the last character position of the previous token.
     */
    public int prevEndPos() {
        return prevEndPos;
    }

    /** Return the position where a lexical error occurred;
     */
    public int errPos() {
        return errPos;
    }

    /** Set the position where a lexical error occurred;
     */
    public void errPos(int pos) {
        errPos = pos;
    }

    /** Return the name of an identifier or token for the current token.
     */
    public Name name() {
        return name;
    }

    /** Return the radix of a numeric literal token.
     */
    public int radix() {
        return radix;
    }

    /** Has a @deprecated been encountered in last doc comment?
     *  This needs to be reset by client with resetDeprecatedFlag.
     */
    public boolean deprecatedFlag() {
        return deprecatedFlag;
    }

    public void resetDeprecatedFlag() {
        deprecatedFlag = false;
    }

    /**
     * Returns the documentation string of the current token.
     */
    public String docComment() {
        return null;
    }

    /**
     * Returns a copy of the input buffer, up to its inputLength.
     * Unicode escape sequences are not translated.
     */
    public char[] getRawCharacters() {
        //�˷�����ʱûʲô�ô�,ֻΪ��ʵ��Lexer�ӿڶ��ӵ�
        //����˵���Ǵ���� 2007-06-18 10:09�Ѹ��� �˷�����DocCommentScanner����Ӧ��
        char[] chars = new char[buflen];
        System.arraycopy(buf, 0, chars, 0, buflen);
        return chars;
    }

    /**
     * Returns a copy of a character array subset of the input buffer.
     * The returned array begins at the <code>beginIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     * Thus the length of the substring is <code>endIndex-beginIndex</code>.
     * This behavior is like 
     * <code>String.substring(beginIndex, endIndex)</code>.
     * Unicode escape sequences are not translated.
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     * @throws IndexOutOfBounds if either offset is outside of the
     *         array bounds
     */
    public char[] getRawCharacters(int beginIndex, int endIndex) {
    	//length���ǹؼ���,���Ե�������
    	//endIndex����endPos,buf[endPos]���ַ��������������Ϊ�´�ɨ������
        int length = endIndex - beginIndex;
        char[] chars = new char[length];
        System.arraycopy(buf, beginIndex, chars, 0, length);
        return chars;
    }

    public enum CommentStyle {
        LINE,
        BLOCK,
        JAVADOC,
    }

    /**
     * Called when a complete comment has been scanned. pos and endPos 
     * will mark the comment boundary.
     */
    protected void processComment(CommentStyle style) {
	if (scannerDebug)
	    System.out.println("processComment(" + pos
			       + "," + endPos + "," + style + ")=|"
                               + new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a complete whitespace run has been scanned. pos and endPos 
     * will mark the whitespace boundary.
     */
    protected void processWhiteSpace() {
	if (scannerDebug)
	    System.out.println("processWhitespace(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a line terminator has been processed.
     */
    protected void processLineTerminator() {
	if (scannerDebug)
	    System.out.println("processTerminator(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /** Build a map for translating between line numbers and
     * positions in the input.
     *
     * @return a LineMap */
    public Position.LineMap getLineMap() {
	return Position.makeLineMap(buf, buflen, false);
    }

}

