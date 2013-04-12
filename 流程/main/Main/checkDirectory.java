    // where
        private boolean checkDirectory(String optName) {
			try {//�Ҽ��ϵ�
			DEBUG.P(this,"checkDirectory(1)");
			DEBUG.P("optName="+optName);

            String value = options.get(optName);
			DEBUG.P("value="+value);
            if (value == null)
                return true;

            File file = new File(value);

			DEBUG.P("file.exists()="+file.exists());
            if (!file.exists()) {
				//javac -d bin\directory_not_found_test
				//���ָ����Ŀ¼�����ڣ���ʾ���´���:
				//javac: directory not found: bin\directory_not_found_test
				//�÷�: javac <options> <source files>
				//-help �����г����ܵ�ѡ��
				//ע:com\sun\tools\javac\resources\javac_zh_CN.properties�ļ�
				//û�ж���"err.dir.not.found"�����Գ��ֵ���ʾ��Ӣ�ĵģ�
				//���Ǵ�com\sun\tools\javac\resources\javac.properties�ļ���ȡ����Ϣ
                error("err.dir.not.found", value);
                return false;
            }

			DEBUG.P("file.isDirectory()="+file.isDirectory());
            if (!file.isDirectory()) {
				//javac -d args.txt
				//���ָ������һ�����ڵ��ļ�����ʾ���´���:
				//javac: ����Ŀ¼: args.txt
				//�÷�: javac <options> <source files>
				//-help �����г����ܵ�ѡ��
                error("err.file.not.directory", value);
                return false;
            }
            return true;

			}finally{//�Ҽ��ϵ�
			DEBUG.P(0,this,"checkDirectory(1)");
			}
        }