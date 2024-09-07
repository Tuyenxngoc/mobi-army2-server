package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;

/**
 * @author tuyen
 */
public abstract class Boss extends Player {

    public Boss(IFightManager fightManager, User user, byte index, short x, short y, byte[] items, short[] abilities, short teamPoints, boolean[] clanItems) {
        super(fightManager, user, index, x, y, items, abilities, teamPoints, clanItems);
    }

    public abstract void turnAction();

}
