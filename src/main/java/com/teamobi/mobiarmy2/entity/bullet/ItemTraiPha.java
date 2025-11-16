package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemTraiPha extends Bullet {
    public ItemTraiPha(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (super.isCollected) {
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x - 8, this.y - 493, -1, 2, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x + 12, this.y - 496, 0, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x - 19, this.y - 505, -2, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x + 18, this.y - 505, 1, 1, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x + 20, this.y - 512, 2, 0, 0, 100));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, this.damage, player, this.x - 20, this.y - 512, -3, 0, 0, 100));
        }
    }
}
