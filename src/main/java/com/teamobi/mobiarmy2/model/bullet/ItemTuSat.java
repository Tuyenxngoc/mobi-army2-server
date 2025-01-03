package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class ItemTuSat extends Bullet {

    public ItemTuSat(BulletManager bullMNG, byte bullId, int satThuong, Player pl) {
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
        if (this.isCanCollision) {
            bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        }
    }

}
