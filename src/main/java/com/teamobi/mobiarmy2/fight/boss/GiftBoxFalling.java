package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class GiftBoxFalling extends Boss {
    public GiftBoxFalling(IFightManager fightManager, byte index, short x, short y) {
        super(fightManager, index, (byte) 23, "Gift Box", x, y, (short) 30, (short) 30, (short) 1, 0);
    }

    @Override
    public void turnAction() {

    }
}