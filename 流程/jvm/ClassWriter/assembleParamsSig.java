    void assembleParamsSig(List<Type> typarams) {
	DEBUG.P(this,"assembleParamsSig(1)");
	DEBUG.P("typarams="+typarams);
	DEBUG.P("sigbufǰ="+sigbuf.toName(names));

	/*��<T extends Exception & InterfaceA & InterfaceB,V extends InterfaceA & InterfaceB >
	�����������:
	com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)
	-------------------------------------------------------------------------
	typarams=T29132923,V23503403
	sigbufǰ=
	sigbuf��=<T:Ljava/lang/Exception;:Lmy/test/InterfaceA;:Lmy/test/InterfaceB;V::Lmy/test/InterfaceA;:Lmy/test/InterfaceB;>
	com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)  END
	-------------------------------------------------------------------------
	*/

        sigbuf.appendByte('<');
        for (List<Type> ts = typarams; ts.nonEmpty(); ts = ts.tail) {
            TypeVar tvar = (TypeVar)ts.head;
            sigbuf.appendName(tvar.tsym.name);
            List<Type> bounds = types.getBounds(tvar);
            if ((bounds.head.tsym.flags() & INTERFACE) != 0) {
                sigbuf.appendByte(':');
            }
            for (List<Type> l = bounds; l.nonEmpty(); l = l.tail) {
                sigbuf.appendByte(':');
                assembleSig(l.head);
            }
        }
        sigbuf.appendByte('>');

	DEBUG.P("sigbuf��="+sigbuf.toName(names));
	DEBUG.P(0,this,"assembleParamsSig(1)");
    }