package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

public class ItemMuaDan extends Bullet {
    public ItemMuaDan(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, (byte) 28, damage, player, x, y, vx, vy, msg, g100);
        this.canCollide = false;
    }


    @Override
    public void update() {
        super.update();
        if (isMaxY) {
            isCollected = true;
        }

        if (isCollected) {
            for (int i = 0; i < 20; i++) {// freeze the position for a moment
                trajectory.add(new Point(x, y));
            }
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 18, y - 20, 2, -1, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 19, y - 20, -3, -1, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 16, y - 23, 3, -2, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 17, y - 23, 4, -2, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 14, y - 26, 3, -3, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 15, y - 26, -4, -3, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 11, y - 28, 3, -4, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 12, y - 28, -4, -4, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 8, y - 30, 2, -5, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 9, y - 30, -3, -5, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x + 5, y - 31, 1, -6, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 6, y - 31, -2, -6, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 29, damage, player, x - 2, y - 31, 1, -7, 15, 60));
        }
    }
}