package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemLaserDelay extends Bullet {
    public ItemLaserDelay(BulletManager bullMNG, byte id, int satThuong, Player pl, int X, int Y) {
        super(bullMNG, id, satThuong, pl, X, Y, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        super.nextXY();
    }

}
