package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemLaserDelay extends Bullet {

    public ItemLaserDelay(BulletManager bullMNG, byte id, long satThuong, Player pl, int X, int Y) {
        super(bullMNG, id, satThuong, pl, X, Y, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        super.nextXY();
    }

}
