package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemSaoBang extends Bullet {
    public ItemSaoBang(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, (byte) 23, damage, player, x, y, vx, vy, msg, g100);
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 24, damage, player, x, y - 187, 0, 3, 0, 50));
            for (int i = 1; i < 7; i++) {
                bulletManager.addBullet(new Bullet(bulletManager, (byte) 24, damage, player, x + i * (i % 2 == 0 ? 30 : -30), y - 187, 0, Math.abs(3 - i), 0, 50));
            }
        }
    }
}