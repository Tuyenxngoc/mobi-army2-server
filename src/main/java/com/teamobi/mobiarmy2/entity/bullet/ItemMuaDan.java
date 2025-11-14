package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemMuaDan extends Bullet {
    public ItemMuaDan(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.isMaxY || this.isCollected) {
            this.isCollected = true;
            for (int i = 0; i < 10; i++) {
                XArray.add(X);
                YArray.add(Y);
            }
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 18, this.Y - 20, 2, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 19, this.Y - 20, -3, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 16, this.Y - 23, 3, -2, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 17, this.Y - 23, 4, -2, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 14, this.Y - 26, 3, -3, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 15, this.Y - 26, -4, -3, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 11, this.Y - 28, 3, -4, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 12, this.Y - 28, -4, -4, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 8, this.Y - 30, 2, -5, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 9, this.Y - 30, -3, -5, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X + 5, this.Y - 31, 1, -6, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 6, this.Y - 31, -2, -6, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, this.damage, player, this.X - 2, this.Y - 31, 1, -7, 15, 60));
        }
    }
}
