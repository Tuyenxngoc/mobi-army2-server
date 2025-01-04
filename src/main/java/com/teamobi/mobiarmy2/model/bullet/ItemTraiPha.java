package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemTraiPha extends Bullet {

    public ItemTraiPha(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X - 8, this.Y - 493, -1, 2, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X + 12, this.Y - 496, 0, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X - 19, this.Y - 505, -2, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X + 18, this.Y - 505, 1, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X + 20, this.Y - 512, 2, 0, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, pl, this.X - 20, this.Y - 512, -3, 0, 0, 100));
        }
    }
}
