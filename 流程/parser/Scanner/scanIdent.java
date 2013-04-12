    /** Read an identifier.
     */
    private void scanIdent() {
	boolean isJavaIdentifierPart;
	char high;
	do {
		//ÿ�ζ���if�ж�һ�±�ÿ�ζ�����putChar(ch)Ч�ʸ���
	    if (sp == sbuf.length) putChar(ch); else sbuf[sp++] = ch;
	    // optimization, was: putChar(ch);

	    scanChar();
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
                if (bp >= buflen) {
                    name = names.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
                    return;
                }
                break;
	    default:
                if (ch < '\u0080') {
                    // all ASCII range chars already handled, above
                    isJavaIdentifierPart = false;
                } else {//�����������ı��������
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
		    return;
		}
	    }
	} while (true);
    }