package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class SmallBoom extends Boss {

    public SmallBoom(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 11, "Small Boom", x, y, (short) 18, (short) 18, maxHp, 10);
    }

    @Override
    public void turnAction() {

    }

    public void bomAction() {
        System.out.println("Bom Action");
    }
}
