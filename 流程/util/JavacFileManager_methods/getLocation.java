    public Iterable<? extends File> getLocation(Location location) {
    	try {//�Ҽ��ϵ�
        DEBUG.P(this,"getLocation(1)");
        DEBUG.P("location="+location);
		
    	nullCheck(location);
        paths.lazy();
        if (location == CLASS_OUTPUT) {
            return (getClassOutDir() == null ? null : List.of(getClassOutDir()));
        } else if (location == SOURCE_OUTPUT) {
            return (getSourceOutDir() == null ? null : List.of(getSourceOutDir()));
        } else
            return paths.getPathForLocation(location);

        }finally{//�Ҽ��ϵ�
        DEBUG.P(0,this,"getLocation(1)");
        }
    }