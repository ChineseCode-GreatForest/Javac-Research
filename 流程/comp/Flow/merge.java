    /** Merge (intersect) inits/uninits from WhenTrue/WhenFalse sets.
     */
    void merge() {
		DEBUG.P(this,"merge()");
		DEBUG.P("inits  ǰ="+inits);
		DEBUG.P("uninitsǰ="+uninits);

		inits = initsWhenFalse.andSet(initsWhenTrue);
		uninits = uninitsWhenFalse.andSet(uninitsWhenTrue);

		DEBUG.P("inits  ��="+inits);
		DEBUG.P("uninits��="+uninits);
		DEBUG.P(0,this,"merge()");
    }