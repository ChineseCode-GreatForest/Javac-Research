    //ʵ����Iterable<String>�ӿڵ����������foreach���ĵط�(JDK>=1.5������)
    private static class PathIterator implements Iterable<String> {
		private int pos = 0;
		private final String path;
		private final String emptyPathDefault;
	
		//���ֺ�";"(windows)��ð��":"(unix/linux)�����·���ֿ� 
		public PathIterator(String path, String emptyPathDefault) {
			DEBUG.P(this,"PathIterator(2)");
			DEBUG.P("path="+path);
			DEBUG.P("emptyPathDefault="+emptyPathDefault);
				
			this.path = path;
			this.emptyPathDefault = emptyPathDefault;
				
			DEBUG.P(0,this,"PathIterator(2)");
		}

		public PathIterator(String path) { this(path, null); }
		public Iterator<String> iterator() {
			return new Iterator<String>() {//�����������ʵ����Iterator<E>�ӿ�
				public boolean hasNext() {
					return pos <= path.length();
				}
				public String next() {
					try {//�Ҽ��ϵ�
					DEBUG.P(this,"next()");
						
					int beg = pos;
					//File.pathSeparator·���ָ���,windows�Ƿֺ�";",unix/linux��ð��":"
					int end = path.indexOf(File.pathSeparator, beg);
						
					DEBUG.P("beg="+beg+" end="+end);
						
					if (end == -1)
						end = path.length();
					pos = end + 1;
						
					DEBUG.P("beg="+beg+" end="+end);
						
					//(beg == end)·���ָ�������ǰ����������������ֵ����(�硰:dir1::dir2:��)
					//���û��emptyPathDefault==null��
					//��ôpath.substring(beg, end)����һ���մ�("")�����ÿմ�����File��ʵ��ʱ
					//���File��ʵ��������ǵ�ǰĿ¼�����԰�emptyPathDefault��ɡ�.���Ƕ����
					//��computeUserClassPath()���һ�����
					if (beg == end && emptyPathDefault != null)
						return emptyPathDefault;
					else
						return path.substring(beg, end);
						
					} finally {
					DEBUG.P(0,this,"next()");
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
    }