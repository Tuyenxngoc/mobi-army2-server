package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Reward;

public class GiftBoxFalling extends Boss {
    public GiftBoxFalling(IFightManager fightManager, short x, short y) {
        super(fightManager, (byte) 23, "Gift Box", x, y, (short) 30, (short) 30, (short) 1, 0);
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from GiftBoxFalling!");
    }

    public Reward getRandomReward() {
        return null;
    }
}