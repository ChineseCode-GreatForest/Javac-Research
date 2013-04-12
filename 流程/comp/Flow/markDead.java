    /** Record that statement is unreachable.
     */
    void markDead() {
		DEBUG.P(this,"markDead()");
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);
		DEBUG.P("inits  ǰ="+inits);
		DEBUG.P("uninitsǰ="+uninits);
		
		inits.inclRange(firstadr, nextadr);
		uninits.inclRange(firstadr, nextadr);
		
		DEBUG.P("inits  ��="+inits);
		DEBUG.P("uninits��="+uninits);
		
		alive = false;
		DEBUG.P("alive="+alive);
		DEBUG.P(0,this,"markDead()");
    }