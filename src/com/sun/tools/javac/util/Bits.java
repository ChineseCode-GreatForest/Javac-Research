/*
 * @(#)Bits.java	1.22 07/03/21
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

package com.sun.tools.javac.util;

/** A class for extensible, mutable bit sets.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Bits.java	1.22 07/03/21")
public class Bits {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Bits);//�Ҽ��ϵ�

    private final static int wordlen = 32;
    private final static int wordshift = 5;
    private final static int wordmask = wordlen - 1;

    private int[] bits;

    /** Construct an initially empty set.
     */
    public Bits() {
        this(new int[1]);
    }

    /** Construct a set consisting initially of given bit vector.
     */
    public Bits(int[] bits) {
        this.bits = bits;
    }

    /** Construct a set consisting initially of given range.
     */
    public Bits(int start, int limit) {
        this();
		inclRange(start, limit);
    }

    private void sizeTo(int len) {
		if (bits.length < len) {
			int[] newbits = new int[len];
			System.arraycopy(bits, 0, newbits, 0, bits.length);
			bits = newbits;
		}
    }

    /** This set = {}.
     */
    public void clear() {
        for (int i = 0; i < bits.length; i++) bits[i] = 0;
    }

    /** Return a copy of this set.
     */
    public Bits dup() {
        int[] newbits = new int[bits.length];
        System.arraycopy(bits, 0, newbits, 0, bits.length);
        return new Bits(newbits);
    }

    /** Include x in this set.
     */
    public void incl(int x) {
		DEBUG.P(this,"incl(int x)");
		//DEBUG.P("x="+x);
		
		assert x >= 0;
		//��Ϊ��int[] bits��������(int,32λ)���飬����ÿ������Ԫ��bits[i]���Ա�ʾ
		//32(��0-31����)����������������������32ʱ����������bits�ĳ���(��1)
		sizeTo((x >>> wordshift) + 1);//�൱��x/32+1
		//DEBUG.P("("+x+" >>> "+wordshift+") + 1 = "+((x >>> wordshift) + 1));

		/*
		����:bits�ĳ���Ϊ1,
		bits[0]=00000000000000000000000000000011
		���x=33,��ô((x >>> wordshift) + 1)��((33/32)+1)=2, bits�ĳ��ȱ�Ϊ2
		bits[x >>> wordshift]���ö�Ӧbits[1],������bits��Ԫ��������(int,32λ),
		����bits[1]�ڳ�ʼ��ʱ0,
		Ҳ����bits[1]��00000000000000000000000000000000(32��0),
		(x & wordmask)=(33 & 31)=(100001 & 11111)=000001=1,
		1<<1=1,
		bits[1] | 1 =00000000000000000000000000000000 | 1
					=00000000000000000000000000000001
		*/
		bits[x >>> wordshift] = bits[x >>> wordshift] |
			(1 << (x & wordmask));
			
		DEBUG.P("x="+x+" bits["+(x >>> wordshift)+"] = "+bits[x >>> wordshift]);
		
		DEBUG.P(0,this,"incl(int x)");
    }
    
    /*
    i<<n��ʾi*(2��n�η�)
    i>>n��ʾi/(2��n�η�)
    i>>>n���i�ǷǸ��������i>>nһ������������߿ճ���λ��0����
    ���i>>>n�Ľ��һ���ǷǸ���
    */


    /** Include [start..limit) in this set.
     */
    public void inclRange(int start, int limit) {
		sizeTo((limit >>> wordshift) + 1);
		for (int x = start; x < limit; x++)
			bits[x >>> wordshift] = bits[x >>> wordshift] |
			(1 << (x & wordmask));
	}

		/** Exclude x from this set.
		 */
	public void excl(int x) {
		DEBUG.P(this,"excl(int x)");
		//DEBUG.P("x="+x);
		
		assert x >= 0;
		sizeTo((x >>> wordshift) + 1);
		bits[x >>> wordshift] = bits[x >>> wordshift] &
			~(1 << (x & wordmask));//��λ
			
		DEBUG.P("x="+x+" bits["+(x >>> wordshift)+"] = "+bits[x >>> wordshift]);
		DEBUG.P(0,this,"excl(int x)");
    }

    /** Is x an element of this set?
     */
    public boolean isMember(int x) {
    	//��wordlen = 32��wordshift = 5��
    	//����bits.length << wordshift�ȼ���bits.length * wordlen
        return
            0 <= x && x < (bits.length << wordshift) &&
            (bits[x >>> wordshift] & (1 << (x & wordmask))) != 0;
    }

    /** this set = this set & xs.
     */
    public Bits andSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] & xs.bits[i];
		return this;
    }

    /** this set = this set | xs.
     */
    public Bits orSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] | xs.bits[i];
		return this;
    }

    /** this set = this set \ xs.
     */
	//���ϲ�����
    public Bits diffSet(Bits xs) {
		DEBUG.P(this,"diffSet(Bits xs)");
		DEBUG.P("thisǰ="+this);
		DEBUG.P("xs    ="+xs);
		
		for (int i = 0; i < bits.length; i++) {
			if (i < xs.bits.length) {
				DEBUG.P("");
				DEBUG.P("bits["+i+"]ǰ="+bits[i]);
				DEBUG.P("xs.bits["+i+"]="+xs.bits[i]);
				DEBUG.P("~xs.bits["+i+"]="+(~xs.bits[i]));
				bits[i] = bits[i] & ~xs.bits[i];
				DEBUG.P("bits["+i+"]��="+bits[i]);
			}
		}
		DEBUG.P("");
		DEBUG.P("this��="+this);
		DEBUG.P(0,this,"diffSet(Bits xs)");
		return this;
    }

    /** this set = this set ^ xs.
     */
    public Bits xorSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] ^ xs.bits[i];
		return this;
    }

    /** Count trailing zero bits in an int. Algorithm from "Hacker's
     *  Delight" by Henry S. Warren Jr. (figure 5-13)
     */
    private static int trailingZeroBits(int x) {
		assert wordlen == 32;
		if (x == 0) return 32;
		int n = 1;
		if ((x & 0xffff) == 0) { n += 16; x >>>= 16; }
		if ((x & 0x00ff) == 0) { n +=  8; x >>>=  8; }
		if ((x & 0x000f) == 0) { n +=  4; x >>>=  4; }
		if ((x & 0x0003) == 0) { n +=  2; x >>>=  2; }
		return n - (x&1);
    }

    /** Return the index of the least bit position >= x that is set.
     *  If none are set, returns -1.  This provides a nice way to iterate
     *  over the members of a bit set:
     *  <pre>
     *  for (int i = bits.nextBit(0); i>=0; i = bits.nextBit(i+1)) ...
     *  </pre>
     */
    /*
    ��x���λ��(������0��ʼ������x)��ʼ����bit����
    �����ҵ��ĵ�1��bitλΪ1���Ǹ�bitλ��bit���е�������
    
    ��:bits=(����=32)10010011100000011010001101000010
    
    ���x=0(Ҳ���Ǵ�bit������Ϊ0��λ�ÿ�ʼ����),
    ���ȴ�bit����10010011100000011010001101000010��
    ���һλ��ʼ���ң������һλ��0������1����������ǰ�ң�����Ϊ1��bitλ��1��
    ��ʱ���ҵ���1��bitλΪ1��bitλ��ֹͣ��ǰ���ҡ�
    ���Ե�x=0ʱ��nextBit()��������λ��Ϊ1��
    
    ���x=9(Ҳ���Ǵ�bit������Ϊ9��λ�ÿ�ʼ����),
    ���ȴ�bit����10010011100000011010001101000010��
    ����Ϊ9��ʼ����(1101000010)��������Ϊ9��bitλ��1��
    ��ʱ���ҵ���1��bitλΪ1��bitλ��ֹͣ��ǰ���ҡ�
    ���Ե�x=9ʱ��nextBit()��������λ��Ϊ9��
    
    ���x=17(Ҳ���Ǵ�bit������Ϊ17��λ�ÿ�ʼ����),
    ���ȴ�bit����10010011100000011010001101000010��
    ����Ϊ17��ʼ����(011010001101000010)��������Ϊ17��bitλ��0��
    ����1����������ǰ�ң�һֱ�ҵ�����Ϊ23��bitλʱ��
    ���ҵ���1��bitλΪ1��bitλ��ֹͣ��ǰ���ҡ�
    ���Ե�x=17ʱ��nextBit()��������λ��Ϊ23��
    */
    public int nextBit(int x) {
		/*	
		int windex = x >>> wordshift;
		if (windex >= bits.length) return -1;
		int word = bits[windex] & ~((1 << (x & wordmask))-1);
		while (true) {
			if (word != 0)
			return (windex << wordshift) + trailingZeroBits(word);
			windex++;
			if (windex >= bits.length) return -1;
			word = bits[windex];
		}*/
		
		int nextBitIndex=-1;
		try {//�Ҽ��ϵ�
		DEBUG.P(this,"nextBit(int x)");
		DEBUG.P("x="+x);
		DEBUG.P("bits="+this);

		int windex = x >>> wordshift;
		if (windex >= bits.length) return -1;
		int word = bits[windex] & ~((1 << (x & wordmask))-1);
		while (true) {
			if (word != 0) {
				nextBitIndex=(windex << wordshift) + trailingZeroBits(word);
				return nextBitIndex;
			}
			windex++;
			if (windex >= bits.length) return -1;
			word = bits[windex];
		}
		
		}finally{//�Ҽ��ϵ�
		DEBUG.P("nextBitIndex="+nextBitIndex);
		DEBUG.P(0,this,"nextBit(int x)");
		}
    }

    /** a string representation of this set.
     */
    public String toString() {
    	/*
    	char[] digits = new char[bits.length * wordlen];
        for (int i = 0; i < bits.length * wordlen; i++)
            digits[i] = isMember(i) ? '1' : '0';
        return new String(digits);
        */
        int len=bits.length * wordlen;
        char[] digits = new char[len];
        for (int i = 0; i < len; i++)
            //digits[i] = isMember(i) ? '1' : '0';//���ǰ���λ��ǰ��˳������
            digits[len-i-1] = isMember(i) ? '1' : '0';//���ǰ���λ��ǰ��˳������
        
        return "(����="+digits.length+")"+new String(digits);//�ҸĶ���һ��
    }

    /** Test Bits.nextBit(int). */
    public static void main(String[] args) {
		java.util.Random r = new java.util.Random();
		Bits bits = new Bits();
		int dupCount = 0;
		for (int i=0; i<125; i++) {
			int k;
			do {
				k = r.nextInt(250);
			} while (bits.isMember(k));
			System.out.println("adding " + k);
			bits.incl(k);
		}
		int count = 0;
		for (int i = bits.nextBit(0); i >= 0; i = bits.nextBit(i+1)) {
			System.out.println("found " + i);
			count ++;
		}
		if (count != 125) throw new Error();
    }
}
