    /** Read token.
     */
    public void nextToken() {

	try {
	    prevEndPos = endPos;
	    sp = 0;
	
	    while (true) {
	    //������processWhiteSpace()��processLineTerminator()����
	    //�����󣬼�������ɨ���ַ�
		pos = bp;
		switch (ch) {
		case ' ': // (Spec 3.6)
		case '\t': // (Spec 3.6)
		case FF: // (Spec 3.6)   //form feed��ָ��ҳ
		    do {
			scanChar();
		    } while (ch == ' ' || ch == '\t' || ch == FF);
		    endPos = bp;
		    processWhiteSpace();
		    break;
		case LF: // (Spec 3.4)   //����,�е�ϵͳ���ɵ��ļ�����û�лس���
		    scanChar();
		    endPos = bp;
		    processLineTerminator();
		    break;
		case CR: // (Spec 3.4)   //�س�,�س�����������з�
		    scanChar();
		    if (ch == LF) {
			scanChar();
		    }
		    endPos = bp;
		    processLineTerminator();
		    break;
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
		case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		    scanNumber(10);
		    return;
		case '.':
		    scanChar();
		    if ('0' <= ch && ch <= '9') {
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
		    scanChar();
		    if (ch == '/') {
				do {
					scanCommentChar();
				} while (ch != CR && ch != LF && bp < buflen);
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
		case '\'':  //�ַ����ַ��������ܿ���
		    scanChar();
		    if (ch == '\'') {
			lexError("empty.char.lit");  //���ַ�����ֵ
		    } else {
			if (ch == CR || ch == LF)
			    lexError(pos, "illegal.line.end.in.char.lit");//�ַ�����ֵ���н�β���Ϸ�
			scanLitChar();
			if (ch == '\'') {
			    scanChar();
			    token = CHARLITERAL;
			} else {
			    lexError(pos, "unclosed.char.lit");
			}
		    }
		    return;
		case '\"':
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
		default:
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
					if (high != 0) {
						if (sp == sbuf.length) {
							putChar(high);
                        } else {
							sbuf[sp++] = high;
						}

						isJavaIdentifierStart = Character.isJavaIdentifierStart(
                                    Character.toCodePoint(high, ch));
					} else {
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