package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemTuSat extends Bullet {

    public ItemTuSat(BulletManager bullMNG, byte bullId, long satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.x, pl.y - 12, 0, 0, 0, 0);
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
            fightManager.mapManager.handleCollision(X, Y, this);
        }
    }

}
