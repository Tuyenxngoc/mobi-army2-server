package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

public class BalloonEye extends Boss {
    public BalloonEye(IFightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 21, "Balloon Eye", x, y, (short) 13, (short) 14, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonEye!");
    }
}
