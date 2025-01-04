package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemLaserDelay extends Bullet {
    public ItemLaserDelay(BulletManager bullMNG, byte id, int satThuong, Player pl, int X, int Y) {
        super(bullMNG, id, satThuong, pl, X, Y, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        super.nextXY();
    }

}
