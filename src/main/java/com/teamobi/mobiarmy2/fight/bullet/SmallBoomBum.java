package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.boss.SmallBoom;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class SmallBoomBum extends Bullet {

    public SmallBoomBum(BulletManager bullMNG, byte bullId, int satThuong, Player pl) {
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
        ((SmallBoom) pl).bomAction();
        if (isCanCollision) {
            bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        }
    }

}
