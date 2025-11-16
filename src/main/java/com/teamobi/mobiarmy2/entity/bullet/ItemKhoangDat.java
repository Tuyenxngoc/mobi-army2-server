package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemKhoangDat extends Bullet {
    private final int nFrame;

    public ItemKhoangDat(BulletManager bullMNG, byte bullId, Player pl, int X, int Y, byte force) {
        super(bullMNG, bullId, 0, pl, X, Y, 0, 0, 0, 0);
        this.nFrame = force * 2;
    }

    @Override
    public void nextXY() {
        this.y += 2;
        this.frame++;
        this.XArray.add(x);
        this.YArray.add(y);
        if (bulletManager.getFightManager().getMapManger().isCollision(x, y)) {
            this.bulletManager.getFightManager().getMapManger().collision(x, y, this);
        }
        if (this.frame == nFrame) {
            this.isCollected = true;
        }
    }
}
