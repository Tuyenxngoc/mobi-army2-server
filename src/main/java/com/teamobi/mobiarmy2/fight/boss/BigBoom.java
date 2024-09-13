package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class BigBoom extends Boss {

    public BigBoom(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 12, "Big boom", x, y, maxHp);
    }

    @Override
    public void turnAction() {
       fightManager.nextTurn();
    }

}
