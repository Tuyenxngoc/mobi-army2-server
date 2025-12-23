package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;

public class UFOPet extends Boss {

    public UFOPet(FightManager fightManager, short x, short y, short maxHp) {
        super(fightManager, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}
