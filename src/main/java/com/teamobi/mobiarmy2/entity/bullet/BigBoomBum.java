package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class BigBoomBum extends Bullet {
    public BigBoomBum(BulletManager bullMNG, byte bullId, int satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        isCollected = true;
        XArray.add(X);
        YArray.add(Y);
        Y += 2;
        XArray.add(X);
        YArray.add(Y);
        bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        player.setDead(true);
        player.setWidth((short) 0);
        player.setHeight((short) 0);
    }
}
