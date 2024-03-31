package com.teamobi.mobiarmy2.army2.fight.boss;

import com.teamobi.mobiarmy2.army2.fight.Boss;
import com.teamobi.mobiarmy2.army2.fight.FightManager;

import java.io.IOException;

public class GiftBoxFalling extends Boss {

    public GiftBoxFalling(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 30;
        super.height = 30;
        this.idNV = 23;
        this.XPExist = 0;
    }

    @Override
    public void turnAction() {
    }

}
