package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemKhoangDat extends Bullet {

    private final int nFrame;

    public ItemKhoangDat(BulletManager bullMNG, byte bullId, Player pl, int X, int Y, byte force) {
        super(bullMNG, bullId, 0, pl, X, Y, 0, 0, 0, 0);
        this.nFrame = force * 2;
    }

    @Override
    public void nextXY() {
        this.Y += 2;
        this.frame++;
        this.XArray.add(X);
        this.YArray.add(Y);
        if (bulletManager.getFightManager().getMapManger().isCollision(X, Y)) {
            this.bulletManager.getFightManager().getMapManger().collision(X, Y, this);
        }
        if (this.frame == nFrame) {
            this.collect = true;
        }
    }

}
