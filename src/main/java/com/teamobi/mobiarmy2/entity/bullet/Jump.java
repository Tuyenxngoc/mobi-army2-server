package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class Jump extends Bullet {
    public Jump(BulletManager bullMNG, byte bullId, int satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void update() {
        this.isCollected = true;
        this.XArray.add(x);
        this.YArray.add(y);
        this.y += 2;
        this.XArray.add(x);
        this.YArray.add(y);
        if (this.canCollide) {
            bulletManager.getFightManager().getMapManger().collision(x, y, this);
        }
    }
}
