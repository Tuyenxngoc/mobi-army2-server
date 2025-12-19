package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemTraiPha extends Bullet {
    private final int baseDamage;

    public ItemTraiPha(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 16, damage, player, x, y, vx, vy, 0, 100);
        this.baseDamage = damage;
        this.canSuperType = false;
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x - 8, y - 493, -1, 2, 0, 100));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x + 12, y - 496, 0, 1, 0, 100));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x - 19, y - 505, -2, 1, 0, 100));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x + 18, y - 505, 1, 1, 0, 100));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x + 20, y - 512, 2, 0, 0, 100));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 12, baseDamage, player, x - 20, y - 512, -3, 0, 0, 100));
        }
    }
}