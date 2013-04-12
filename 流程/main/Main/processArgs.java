    /** Process command line arguments: store all command line options
     *  in `options' table and return all source filenames.
     *  @param flags    The array of command line arguments.
     */
    public List<File> processArgs(String[] flags) { // XXX sb protected
    //String[] flags��ֵ����CommandLine.parse(args)�����,args�������в���
    try {//�Ҽ��ϵ�
    	DEBUG.P(this,"processArgs(1)");
    	DEBUG.P("optionsǰ="+options);
		//DEBUG.P("Options options.size()="+options.size());
        //DEBUG.P("Options options.keySet()="+options.keySet());

        int ac = 0;
        while (ac < flags.length) {
        	DEBUG.P("flags["+ac+"]="+flags[ac]);
        	
            String flag = flags[ac];
            ac++;

            int j;
            // quick hack to speed up file processing: 
            // if the option does not begin with '-', there is no need to check
            // most of the compiler options.
            /*
            ����ĳ�����뼼���Ժ�ǿ��
            ��Ϊjavac�����е�ѡ�����ƶ�����'-'�ַ���ͷ��,recognizedOptions�����д�ŵ�
            ѡ��������һ����HiddenOption(SOURCEFILE)����'-'�ַ���ͷ�⣬��������ѡ��
            ���ƶ�����'-'�ַ���ͷ�ġ������javac�������г��ֲ�����'-'�ַ���ͷ��ѡ���
            ����λ��firstOptionToCheck��recognizedOptions������ĩβ��ʼ,
            (Ҳ����ֱ����recognizedOptions��������һ��ѡ��Ƚ�)
            ��Ҫô��Ҫ�����Դ�ļ���Ҫô�Ǵ����ѡ�
            
            ��������javac�������е�ѡ������'-'�ַ���ͷʱ��
            ����λ��firstOptionToCheck��recognizedOptions�����һ��Ԫ�ؿ�ʼ��ֱ��
            ����������recognizedOptions����(j == recognizedOptions.length)ʱ������
            ȷ���Ǵ����ѡ�
            */
            
			//���flag.length()�ĳ���Ϊ0ʱ������쳣
			//��com.sun.tools.javac.main.CommandLine���е�ע��
            int firstOptionToCheck = flag.charAt(0) == '-' ? 0 : recognizedOptions.length-1;
            
			for (j=firstOptionToCheck; j<recognizedOptions.length; j++)
                if (recognizedOptions[j].matches(flag)) break;

            if (j == recognizedOptions.length) {
                error("err.invalid.flag", flag);
                return null;
            }
            

            Option option = recognizedOptions[j];
            //�ο�JavacOption.hasArg()�е�ע��
			//���⣬һ��ѡ�����ֻ��һ������
            if (option.hasArg()) {
                if (ac == flags.length) {
                	/*��������:
                	F:\Javac>javac -d
					javac: -d ��Ҫ����
					�÷�: javac <options> <source files>
					-help �����г����ܵ�ѡ��
					*/
                    error("err.req.arg", flag);
                    return null;
                }
                String operand = flags[ac];
                ac++;
                
                //�����process()�ڲ����ǰ�flag��operand����һ<K,V>�ԣ�
                //����options��,options���Կ�����һ��Map<K,V>
                //ϸ���뿴com.sun.tools.javac.main.RecognizedOptions���getAll()����
                if (option.process(options, flag, operand))
                    return null;
            } else {
            	//�����process()�ڲ����ǰ�flag��flag����һ<K,V>�ԣ�
                //����options��,options���Կ�����һ��Map<K,V>
                //ϸ���뿴com.sun.tools.javac.main.RecognizedOptions���getAll()����
                if (option.process(options, flag))
                    return null;
            }
        }
        
        //����javac��������ָ���ˡ�-d <Ŀ¼>��ѡ��ʱ��
        //���<Ŀ¼>�Ƿ���ڣ������ڻ���Ŀ¼����ʾ���󲢷���
        if (!checkDirectory("-d"))
            return null;
        //����javac��������ָ���ˡ�-s <Ŀ¼>��ѡ��ʱ��
        //���<Ŀ¼>�Ƿ���ڣ������ڻ���Ŀ¼����ʾ���󲢷���
        if (!checkDirectory("-s"))
            return null;
            
        //�����������û��-source��-targetѡ������Ĭ��ֵ
        String sourceString = options.get("-source");
        Source source = (sourceString != null)
        //������lookup()һ�����᷵��null,��Ϊ������
        //��(recognizedOptions[j].matches(flag))ʱ����д��Ѿ�������
            ? Source.lookup(sourceString)
            : Source.DEFAULT;
        String targetString = options.get("-target");
        //������lookup()һ�����᷵��null,��Ϊ������
        //��(recognizedOptions[j].matches(flag))ʱ����д��Ѿ�������
        Target target = (targetString != null)
            ? Target.lookup(targetString)
            : Target.DEFAULT;
        // We don't check source/target consistency for CLDC, as J2ME
        // profiles are not aligned with J2SE targets; moreover, a
        // single CLDC target may have many profiles.  In addition,
        // this is needed for the continued functioning of the JSR14
        // prototype.

		DEBUG.P("sourceString="+sourceString);
		DEBUG.P("source="+source);
		DEBUG.P("source.requiredTarget()="+source.requiredTarget());
		DEBUG.P("targetString="+targetString);
		DEBUG.P("target="+target);
        //�����"-target jsr14"������ִ������Ĵ���
        if (Character.isDigit(target.name.charAt(0))) {
        	//��target�İ汾��<source�İ汾��
            if (target.compareTo(source.requiredTarget()) < 0) {
                if (targetString != null) {
                    if (sourceString == null) {//ָ��-target��ûָ��-source�����
                    	/*��������:
                    	F:\Javac>javac -target 1.4
						javac: Ŀ��汾 1.4 ��Ĭ�ϵ�Դ�汾 1.5 ��ͻ
						*/
                        warning("warn.target.default.source.conflict",
                                targetString,
                                source.requiredTarget().name);
                    } else {//ָ��-target��ͬʱָ��-source�����
                    	/*��������:
                    	F:\Javac>javac -target 1.4 -source 1.5
						javac: Դ�汾 1.5 ��ҪĿ��汾 1.5
						*/
                        warning("warn.source.target.conflict",
                                sourceString,
                                source.requiredTarget().name);
                    }
                    return null;
                } else {
                	//û��ָ��-targetʱ��targetȡĬ�ϰ汾��(javac1.7Ĭ����1.6)
                	//���Ĭ�ϰ汾�Ż���source�ͣ���target�汾����source����
                    options.put("-target", source.requiredTarget().name);
                }
            } else {
            	//��target�İ汾��>=source�İ汾�����û�û��
            	//javac��������ָ����-target��ѡ��Ҳ�����ʹ��
            	//����ʱ��target�汾Ĭ��Ϊ1.4
                if (targetString == null && !source.allowGenerics()) {
                    options.put("-target", Target.JDK1_4.name);
                }
            }
        }
        return filenames.toList();
        
    }finally{//�Ҽ��ϵ�
	DEBUG.P("");
	DEBUG.P("source="+options.get("-source"));
	DEBUG.P("target="+options.get("-target"));

	DEBUG.P("");
    DEBUG.P("ListBuffer<File> filenames.size()="+filenames.size());
    DEBUG.P("ListBuffer<String> classnames.size()="+classnames.size());
    //DEBUG.P("Options options.size()="+options.size());
    //DEBUG.P("Options options.keySet()="+options.keySet());
    
    DEBUG.P("options��="+options);
	DEBUG.P(0,this,"processArgs(1)");
	}
	
    }