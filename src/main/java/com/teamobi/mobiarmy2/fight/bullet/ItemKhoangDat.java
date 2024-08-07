package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

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
        this.XArray.add((short) X);
        this.YArray.add((short) Y);
        if (this.fightManager.mapManager.isCollision(X, Y)) {
            this.fightManager.mapManager.handleCollision(X, Y, this);
        }
        if (this.frame == nFrame) {
            this.collect = true;
        }
    }

}
