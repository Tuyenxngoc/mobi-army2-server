package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

public class BalloonFanBack extends Boss {
    public BalloonFanBack(IFightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 20, "Balloon Fan Back", x, y, (short) 10, (short) 19, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonFanBack!");
    }
}