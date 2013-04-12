    protected void lazy() {
		DEBUG.P(this,"lazy()");
		DEBUG.P("inited="+inited);
		
		//�ڳ�ʼ��ʱִ��(Ҳ������parser֮ǰ)
		if (!inited) {
			//�Ƿ����Xlint:�е�pathѡ��,һ��Ϊû��
			//�������-Xlint:pathʱ�����·�����д�ʱ���ᷢ������
			warn = lint.isEnabled(Lint.LintCategory.PATH);
			
			pathsForLocation.put(PLATFORM_CLASS_PATH, computeBootClassPath());
			
			DEBUG.P(this,"computeUserClassPath()");
			pathsForLocation.put(CLASS_PATH, computeUserClassPath());
			DEBUG.P(2,this,"computeUserClassPath()");
			
			DEBUG.P(this,"computeSourcePath()");
			pathsForLocation.put(SOURCE_PATH, computeSourcePath());
			DEBUG.P(2,this,"computeSourcePath()");

			inited = true;
		}
		
		DEBUG.P(0,this,"lazy()");
    }