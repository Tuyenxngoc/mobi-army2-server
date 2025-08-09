package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.model.Boss;

public class BalloonGun extends Boss {
    public BalloonGun(FightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 18, "Balloon Gun", x, y, (short) 21, (short) 20, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonGun!");

    }
}