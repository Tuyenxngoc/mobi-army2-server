package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.Player;
import com.teamobi.mobiarmy2.army2.fight.boss.BigBoomHTCC;

public class BigBoomHTCCBum extends Bullet {

    public BigBoomHTCCBum(BulletManager bullMNG, byte bullId, long satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.X, pl.Y - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        this.collect = true;
        this.XArray.add(X);
        this.YArray.add(Y);
        this.Y += 2;
        this.XArray.add(X);
        this.YArray.add(Y);
        ((BigBoomHTCC) pl).bomAction();
        if (this.isCanCollision) {
            fm.mapMNG.collision(X, Y, this);
        }
    }

}
