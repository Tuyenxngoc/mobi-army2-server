package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class BigBoomBum extends Bullet {

    public BigBoomBum(BulletManager bullMNG, byte bullId, int satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        collect = true;
        XArray.add(X);
        YArray.add(Y);
        Y += 2;
        XArray.add(X);
        YArray.add(Y);
        bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        pl.setDead(true);
        pl.setWidth((short) 0);
        pl.setHeight((short) 0);
    }

}
