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
        this.collect = true;
        this.XArray.add((short) X);
        this.YArray.add((short) Y);
        this.Y += 2;
        this.XArray.add((short) X);
        this.YArray.add((short) Y);
        ((SmallBoom) pl).bomAction();
        if (this.isCanCollision) {
            bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        }
    }

}
