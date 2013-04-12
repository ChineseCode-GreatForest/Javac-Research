/*
 * @(#)Context.java	1.23 07/03/21
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

import com.sun.tools.javac.Main;
import java.util.*;

/**
 * Support for an abstract context, modelled loosely after ThreadLocal
 * but using a user-provided context instead of the current thread.
 *
 * <p>Within the compiler, a single Context is used for each
 * invocation of the compiler.  The context is then used to ensure a
 * single copy of each compiler phase exists per compiler invocation.
 *
 * <p>The context can be used to assist in extending the compiler by
 * extending its components.  To do that, the extended component must
 * be registered before the base component.  We break initialization
 * cycles by (1) registering a factory for the component rather than
 * the component itself, and (2) a convention for a pattern of usage
 * in which each base component registers itself by calling an
 * instance method that is overridden in extended components.  A base
 * phase supporting extension would look something like this:
 *
 * <p><pre>
 * public class Phase {
 *     protected static final Context.Key<Phase> phaseKey =
 *	   new Context.Key<Phase>();
 *
 *     public static Phase instance(Context context) {
 *	   Phase instance = context.get(phaseKey);
 *	   if (instance == null)
 *	       // the phase has not been overridden
 *	       instance = new Phase(context);
 *	   return instance;
 *     }
 *
 *     protected Phase(Context context) {
 *	   context.put(phaseKey, this);
 *	   // other intitialization follows...
 *     }
 * }
 * </pre>
 *
 * <p>In the compiler, we simply use Phase.instance(context) to get
 * the reference to the phase.  But in extensions of the compiler, we
 * must register extensions of the phases to replace the base phase,
 * and this must be done before any reference to the phase is accessed
 * using Phase.instance().  An extended phase might be declared thus:
 *
 * <p><pre>
 * public class NewPhase extends Phase {
 *     protected NewPhase(Context context) {
 *	   super(context);
 *     }
 *     public static void preRegister(final Context context) {
 *         context.put(phaseKey, new Context.Factory<Phase>() {
 *	       public Phase make() {
 *		   return new NewPhase(context);
 *	       }
 *         });
 *     }
 * }
 * </pre>
 *
 * <p>And is registered early in the extended compiler like this
 *
 * <p><pre>
 *     NewPhase.preRegister(context);
 * </pre>
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Context.java	1.23 07/03/21")
public class Context {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Context);//�Ҽ��ϵ�
	
    /** The client creates an instance of this class for each key.
     */
    public static class Key<T> {
    	/*���Ǵ�����÷�
    	//T t=new T();
    	//�Ҽ��ϵ�
    	public String toString() {
    		return T.class+"";
    		//return "Key<"+t.getClass().getName()+">";
    	}
    	*/
	// note: we inherit identity equality from Object.
    }

    /**
     * The client can register a factory for lazy creation of the
     * instance.
     */
    public static interface Factory<T> {
	T make();
    };
    
    //�Ҽ��ϵ�
    public String toString() {
    	String lineSeparator=System.getProperty("line.separator");
    	StringBuffer sb=new StringBuffer();
    	//sb.append(lineSeparator);
    	/*
    	if(ht==null) sb.append("ht=null");
    	else {
	    	sb.append("ht.[size=").append(ht.size());	
		    for(Map.Entry<Key,Object> myMapEntry: ht.entrySet())
		        	sb.append(", ").append(myMapEntry);
		    sb.append("]");
		}
		sb.append(System.getProperty("line.separator"));
		
		if(kt==null) sb.append("kt=null");
    	else {
	    	sb.append("kt.[size=").append(kt.size());	
		    for(Map.Entry<Class<?>, Key<?>> myMapEntry: kt.entrySet())
		        	sb.append(", ").append(myMapEntry);
		    sb.append("]");
		}
		*/
		
		if(ht==null) sb.append("Map<Key,Object> ht=null");
    	else {
	    	sb.append("Map<Key,Object> ht.size=").append(ht.size());	
	    	if(ht.size()>0) {
	    	sb.append(lineSeparator);
	    	sb.append("---------------------------------------------");
	    	sb.append(lineSeparator);
		    for(Map.Entry<Key,Object> myMapEntry: ht.entrySet()) {
		    	sb.append("Key   =").append(myMapEntry.getKey());
		    	sb.append(lineSeparator);
                        
                        Object o=myMapEntry.getValue();
                        if(o!=null) {
                            sb.append("Object=").append(o.getClass().getName());
                            if(o instanceof Factory)
                                sb.append(" [instanceof Factory]");
                        }
		    	else sb.append("Object=").append(o);
                                
		    	//if(myMapEntry.getValue()!=null)
		    	//sb.append("Object=").append(myMapEntry.getValue().getClass().getName());
		    	//else sb.append("Object=").append(myMapEntry.getValue());
		    	
		    	sb.append(lineSeparator);
		    	sb.append(lineSeparator);
		    }
		    sb.append("---------------------------------------------");
			}
		}
		sb.append(lineSeparator);
		sb.append(lineSeparator);
		if(kt==null) sb.append("Map<Class<?>, Key<?>> kt=null");
    	else {
	    	sb.append("Map<Class<?>, Key<?>> kt.size=").append(kt.size());	
	    	if(kt.size()>0) {
	    	sb.append(lineSeparator);
	    	sb.append("---------------------------------------------");
	    	sb.append(lineSeparator);
		    for(Map.Entry<Class<?>, Key<?>> myMapEntry: kt.entrySet()) {
		    	sb.append("Class<?>=").append(myMapEntry.getKey());
		    	sb.append(lineSeparator);
		    	sb.append("Key<?>  =").append(myMapEntry.getValue());
		    	sb.append(lineSeparator);
		    	sb.append(lineSeparator);
		    }
		    sb.append("---------------------------------------------");
			}
		}
	    return sb.toString();
    }

    /**
     * The underlying map storing the data.
     * We maintain the invariant that this table contains only
     * mappings of the form
     * Key<T> -> T or Key<T> -> Factory<T> */
    private Map<Key,Object> ht = new HashMap<Key,Object>();

    /** Set the factory for the key in this context. */
    public <T> void put(Key<T> key, Factory<T> fac) {
    DEBUG.P(this,"put(Key<T> key, Factory<T> fac)");
	//DEBUG.P("contextǰ="+toString())
	if(fac!=null)
		DEBUG.P("fac="+fac.getClass().getName());
	else DEBUG.P("fac="+fac);
		    	
	
	checkState(ht);
	Object old = ht.put(key, fac);
	if (old != null)
	    throw new AssertionError("duplicate context value");
	
	DEBUG.P("context��="+toString());
	DEBUG.P(0,this,"put(Key<T> key, Factory<T> fac)");
    }

    /** Set the value for the key in this context. */
    public <T> void put(Key<T> key, T data) {
    DEBUG.P(this,"put(Key<T> key, T data)");
    if(data!=null)
		DEBUG.P("data="+data.getClass().getName());
	else DEBUG.P("data="+data);
	//DEBUG.P("contextǰ="+toString());
	
    /*����:
    Context context = new Context();
    Context.Key<Context.Factory> factoryKey =new Context.Key<Context.Factory>();
	context.put(factoryKey,new Context.Factory<String>(){public String make() {return "";}});
    
    �������������쳣:
    Exception in thread "main" java.lang.AssertionError: T extends Context.Factory
    
    ��ΪContext.Key<T>�Ĳ��������Ͳ�������Context.Key<Context.Factory>
    */    
	if (data instanceof Factory)
	    throw new AssertionError("T extends Context.Factory");
	checkState(ht);
	Object old = ht.put(key, data);
	if (old != null && !(old instanceof Factory) && old != data && data != null)
	    throw new AssertionError("duplicate context value");
	
	DEBUG.P("context��="+toString());
	DEBUG.P(0,this,"put(Key<T> key, T data)");
    }

    /** Get the value for the key in this context. */
    public <T> T get(Key<T> key) {
        try {
        DEBUG.P(this,"get(Key<T> key)");
        //if(key!=null) DEBUG.P("key="+key.getClass().getName());
	//else DEBUG.P("key="+key);
        DEBUG.P("key="+key);
        
	checkState(ht);
	Object o = ht.get(key);
	
        if(o!=null) DEBUG.P("o="+o.getClass().getName());
	else DEBUG.P("o="+o);
        
        DEBUG.P("(o instanceof Factory)="+(o instanceof Factory));
        
	if (o instanceof Factory) {
	    Factory fac = (Factory)o;
	    o = fac.make();
	    if (o instanceof Factory)
		throw new AssertionError("T extends Context.Factory");
            //Ҳ����˵�ڵ���make()ʱ�Ѱ�make()���صĽ������ht(���Ӽ�:JavacFileManager.preRegister())
	    assert ht.get(key) == o;
	}

	/* The following cast can't fail unless there was
	 * cheating elsewhere, because of the invariant on ht.
	 * Since we found a key of type Key<T>, the value must
	 * be of type T.
	 */
	 return Context.<T>uncheckedCast(o);
         
         } finally {
         DEBUG.P(0,this,"get(Key<T> key)"); 
         }
	/*
	ע������ġ�<T>���� ��private static <T> T uncheckedCast(Object o)����
	�ġ�<T>���Ĳ��ǰ�߱�ʾ����get(Key<T> key)�����еġ�T����ʵ�����ͣ�
	�����ʼ:
	-------------------------------------
	Context context = new Context();
	Context.Key<Number> numberKey = new Context.Key<Number>();
	Number number=new Number();
	context.put(numberKey,number);
	
	number=context.get(numberKey);
	------------------------------------
	��ʱ�ѡ�numberKey��������get(Key<T> key)��������
	��Ϊ������numberKey���ǡ�Context.Key<Number>�����ͣ�
	����get(Key<T> key)�еġ�T����ʵ�������ǡ�Number����
	
	���ǡ�get(Key<T> key)��������ġ�Object o = ht.get(key)����
	������o����Object���͵ģ�������á�Context.<T>uncheckedCast(o)����
	������o�����õ�Objectʵ��ת���ɡ�Number�����ͣ�
	������number=context.get(numberKey)���õ��Ľ������ȷ��
	��Context.<T>uncheckedCast(o)���൱�ڰѡ�Object o��ת���ɡ�<T>��(�������Number)
	
	��Context.<T>uncheckedCast(o)�������﷨ȷʵ�ܹŹ֣�
	����Ҫ�����ڡ�private static <T> T uncheckedCast(Object o)�������Ķ���
	��ɵģ����������һ����̬���ͷ���������ֻ�з��������뷺�ͱ�����<T>����أ�
	��������ֻ��( Object o )�����Ҳ��뷺�ͱ�����<T>����أ�
	��Ҫ����uncheckedCast����ʱ������������ĸ�ʽ������:
	Context.<����ֵ����>uncheckedCast(����Object o)
	
	����ֵ���Ϳ����Ƿ��ͱ���
	(���ͱ�����ʵ�������ڱ����ڼ�ֻ���ƶ��������ް󶨣�
	������ʲô����ֻ���������ڼ�ȷ��)
	
	
	�����ʼ:
	Object o=new String("str");
	��ô��������������:
	String str=Context.<String>uncheckedCast(o);(�ȼ���: String str=(String)o)
	
	�ѡ�return Context.<T>uncheckedCast(o);����uncheckedCast�����ĵ��ø�ʽ����
	��<T>����Ӧ��<����ֵ����>������(o)����Ӧ��(����Object o)��
	
	���⡰uncheckedCast�������Ķ�������:
	--------------------------------------
	@SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object o) {
        return (T)o;
    }
    --------------------------------------
    ע�͡�@SuppressWarnings("unchecked")�����ָ����uncheckedCast��������
    ����ʱ�п��ܲ���ת���쳣(java.lang.ClassCastException)��
    ��Ϊ(T)��������δȷ���ģ�
    ����(T)�����Ϳ����ǡ�Number����������(Object o)ʵ����String��ʵ������ʱ��
    ��ʱ��(T)o���͵ȼ��ڡ�(Number)String������Ȼ�ǲ��Եġ�
	*/
    }

    public Context() {}

    private Map<Class<?>, Key<?>> kt = new HashMap<Class<?>, Key<?>>();
    
    private <T> Key<T> key(Class<T> clss) {
        DEBUG.P(this,"key(Class<T> clss)");
        if(clss!=null) DEBUG.P("clss="+clss.getName());
	else DEBUG.P("clss="+clss);
        
	checkState(kt);
	//�ȼ���Key<T> k = Context.<Key<T>>uncheckedCast(kt.get(clss));
	//��Ϊkt.get(clss)���ص�������Key<T>���պ����ʽ��ߵ�����һ��
	Key<T> k = uncheckedCast(kt.get(clss));
        
        DEBUG.P("k="+k);
        
	if (k == null) {
	    k = new Key<T>();
	    kt.put(clss, k);
	}
        
        DEBUG.P(0,this,"key(Class<T> clss)");
	return k;
    }

    public <T> T get(Class<T> clazz) {
        try {
        DEBUG.P(this,"get(Class<T> clazz)");
        if(clazz!=null) DEBUG.P("clazz="+clazz.getName());
	else DEBUG.P("clazz="+clazz);
        
	return get(key(clazz));
        
        } finally {
        DEBUG.P(0,this,"get(Class<T> clazz)");    
        }
    }

    public <T> void put(Class<T> clazz, T data) {
    DEBUG.P(this,"put(Class<T> clazz, T data)");
    if(data!=null)
		DEBUG.P("data="+data.getClass().getName());
	else DEBUG.P("data="+data);
	//DEBUG.P("contextǰ="+toString());
	
	put(key(clazz), data);
	
	//DEBUG.P("context��="+toString());
	DEBUG.P(0,this,"put(Class<T> clazz, T data)");
    }
    public <T> void put(Class<T> clazz, Factory<T> fac) {
    DEBUG.P(this,"put(Class<T> clazz, Factory<T> fac)");
    if(fac!=null)
		DEBUG.P("fac="+fac.getClass().getName());
	else DEBUG.P("fac="+fac);
	//DEBUG.P("contextǰ="+toString());
    
	put(key(clazz), fac);
	
	//DEBUG.P("context��="+toString());
	DEBUG.P(0,this,"put(Class<T> clazz, Factory<T> fac)");
    }

    /**
     * TODO: This method should be removed and Context should be made type safe.
     * This can be accomplished by using class literals as type tokens.
     */
    @SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object o) {
        return (T)o;
    }

    public void dump() {
	for (Object value : ht.values())
	    System.err.println(value == null ? null : value.getClass());
    }

    public void clear() {
	ht = null;
	kt = null;
    }
    
    private static void checkState(Map<?,?> t) {
	if (t == null)
	    throw new IllegalStateException();
    }
}
