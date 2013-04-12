/* ********************************************************************
 * Type completion
 *********************************************************************/

    Type attribImportType(JCTree tree, Env<AttrContext> env) {
        assert completionEnabled;
        try {
            DEBUG.P(this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            DEBUG.P("tree="+tree);
            DEBUG.P("env="+env);
            // To prevent deep recursion, suppress completion of some
            // types.
            completionEnabled = false;
            //��import my.StaticImportTest.MyInnerClass;���ɵ�JCFieldAccess��
            //JCFieldAccess����ÿһ��selector��sym��attribType�󶼲�Ϊnull
            return attr.attribType(tree, env);
        } finally {
            DEBUG.P(0,this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            completionEnabled = true;
        }
    }