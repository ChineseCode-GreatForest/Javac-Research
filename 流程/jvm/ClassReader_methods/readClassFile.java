    /** Read a class file.
     */
    private void readClassFile(ClassSymbol c) throws IOException {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"readClassFile(1)");
		DEBUG.P("c="+c);

        int magic = nextInt();
        
        DEBUG.P("magic="+magic+" JAVA_MAGIC="+JAVA_MAGIC);
        
        if (magic != JAVA_MAGIC)
            throw badClassFile("illegal.start.of.class.file");

        int minorVersion = nextChar();
        int majorVersion = nextChar();
        int maxMajor = Target.MAX().majorVersion;
        int maxMinor = Target.MAX().minorVersion;
        
        DEBUG.P("minorVersion="+minorVersion+" majorVersion="+majorVersion);
        DEBUG.P("maxMinor="+maxMinor+" maxMajor="+maxMajor);
        DEBUG.P("bp="+bp);
        DEBUG.P("checkClassFile="+checkClassFile);
        
        if (majorVersion > maxMajor ||
            majorVersion * 1000 + minorVersion <
            Target.MIN().majorVersion * 1000 + Target.MIN().minorVersion)
        {
            if (majorVersion == (maxMajor + 1)) 
                log.warning("big.major.version",
                            currentClassFile,
                            majorVersion,
                            maxMajor);
            else
                throw badClassFile("wrong.version",
                                   Integer.toString(majorVersion),
                                   Integer.toString(minorVersion),
                                   Integer.toString(maxMajor),
                                   Integer.toString(maxMinor));
        }
        else if (checkClassFile &&
                 majorVersion == maxMajor &&
                 minorVersion > maxMinor)
        {
        	//Դ��©��"ccf"
        	//printCCF("found.later.version",
            //         Integer.toString(minorVersion));
        	
        	//�Ҽ��ϵ�
            printCCF("ccf.found.later.version",
                     Integer.toString(minorVersion));
        }
        indexPool();
        DEBUG.P("bp="+bp);
        DEBUG.P("signatureBuffer.length="+signatureBuffer.length);
        if (signatureBuffer.length < bp) {
        	//����bp��ֵ��1,2,4,8,16,32,64,128,256,512,1024,2048....��
        	//���������е�λ�ã�����ȡһ��>=bp����Сֵ��ΪsignatureBuffer��
        	//����(���е����ڴ����������ӵķ�ʽ����һ���̶�����������)
        	//��bp=916����ôsignatureBuffer��СΪ1024
            int ns = Integer.highestOneBit(bp) << 1;
            signatureBuffer = new byte[ns];
        }
        DEBUG.P("signatureBuffer.length="+signatureBuffer.length);
        readClass(c);
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"readClassFile(1)");
		}
    }