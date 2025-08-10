package com.teamobi.mobiarmy2.entity.boss;

import com.teamobi.mobiarmy2.entity.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;

public class BalloonFanBack extends Boss {
    public BalloonFanBack(FightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 20, "Balloon Fan Back", x, y, (short) 10, (short) 19, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonFanBack!");

    }
}