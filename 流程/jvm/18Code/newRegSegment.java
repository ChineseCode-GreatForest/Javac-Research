    /** Start a set of fresh registers.
     */
    public void newRegSegment() {
    DEBUG.P(this,"newRegSegment()");
    DEBUG.P("nextregǰ="+nextreg);
    
	nextreg = max_locals;
	
	DEBUG.P("nextreg��="+nextreg);
	DEBUG.P(0,this,"newRegSegment()");
    }