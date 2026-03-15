package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;

public class BalloonGun extends Boss {
    public BalloonGun(FightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 18, "Balloon Gun", x, y, (short) 21, (short) 20, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonGun!");
    }
}