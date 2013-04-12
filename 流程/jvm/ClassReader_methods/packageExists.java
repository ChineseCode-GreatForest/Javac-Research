/************************************************************************
 * Loading Packages
 ***********************************************************************/

    /** Check to see if a package exists, given its fully qualified name.
     */
    public boolean packageExists(Name fullname) {
    	try {//�Ҽ��ϵ�
		DEBUG.P(this,"packageExists(Name fullname)");
		DEBUG.P("fullname="+fullname);

        return enterPackage(fullname).exists();
        
        }finally{//�Ҽ��ϵ�
		DEBUG.P(0,this,"packageExists(Name fullname)");
		}
    }