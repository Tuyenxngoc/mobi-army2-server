package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;

public abstract class Boss extends Player {

    public Boss(IFightManager fightManager, User user, byte index, short x, short y, byte[] items) {
        super(fightManager, user, index, x, y, items);
    }

    public abstract void turnAction();

}
