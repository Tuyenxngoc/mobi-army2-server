package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemMuaDan extends Bullet {
    public ItemMuaDan(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.isMaxY || this.isCollected) {
            this.isCollected = true;
            for (int i = 0; i < 10; i++) {
                XArray.add(x);
                YArray.add(y);
            }
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 18, this.y - 20, 2, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 19, this.y - 20, -3, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 16, this.y - 23, 3, -2, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 17, this.y - 23, 4, -2, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 14, this.y - 26, 3, -3, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 15, this.y - 26, -4, -3, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 11, this.y - 28, 3, -4, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 12, this.y - 28, -4, -4, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 8, this.y - 30, 2, -5, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 9, this.y - 30, -3, -5, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x + 5, this.y - 31, 1, -6, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 6, this.y - 31, -2, -6, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.x - 2, this.y - 31, 1, -7, 15, 60));
        }
    }
}
