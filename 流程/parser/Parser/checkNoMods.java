    /** Diagnose a modifier flag from the set, if any. */
    void checkNoMods(long mods) {
    	DEBUG.P(this,"checkNoMods(long mods)");
    	DEBUG.P("mods="+Flags.toString(mods).trim());
    	
        if (mods != 0) {
            /*
            ֻȡmods��׷�0λ,����λ����0:
            for(int mods=1;mods<6;mods++) {
                System.out.println("ʮ����: "+mods+" & -"+mods+" = "+(mods & -mods));
                System.out.println("������: "+Integer.toBinaryString(mods)+" & "+Integer.toBinaryString(-mods)+" = "+Integer.toBinaryString(mods & -mods));
                System.out.println();
            }
            ���:(��Ϊ�෴�������������ɰ�����λȡ����1����ԭ��õ��Է�)
            ʮ����: 1 & -1 = 1
            ������: 1 & 11111111111111111111111111111111 = 1

            ʮ����: 2 & -2 = 2
            ������: 10 & 11111111111111111111111111111110 = 10

            ʮ����: 3 & -3 = 1
            ������: 11 & 11111111111111111111111111111101 = 1

            ʮ����: 4 & -4 = 4
            ������: 100 & 11111111111111111111111111111100 = 100

            ʮ����: 5 & -5 = 1
            ������: 101 & 11111111111111111111111111111011 = 1
            */
            long lowestMod = mods & -mods;
            DEBUG.P("lowestMod="+Flags.toString(lowestMod).trim());
            log.error(S.pos(), "mod.not.allowed.here",
                      Flags.toString(lowestMod).trim());
        }
        DEBUG.P(0,this,"checkNoMods(long mods)");
    }