package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemLaser extends Bullet {
    private final int baseDamage;

    public ItemLaser(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 14, damage, player, x, y, vx, vy, 10, 50);
        this.baseDamage = damage;
        this.canSuperType = false;
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            bulletManager.addBullet(new ItemLaserDelay(bulletManager, baseDamage, player, x, y));
        }
    }
}