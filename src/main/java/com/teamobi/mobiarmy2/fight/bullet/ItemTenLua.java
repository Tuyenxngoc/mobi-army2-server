package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemTenLua extends Bullet {
    private final int baseDamage;
    private final byte force;

    public ItemTenLua(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, byte force) {
        super(bulletManager, (byte) 26, damage, player, x, y, vx, vy, 30, 60);
        this.baseDamage = damage;
        this.force = force;
        this.canSuperType = false;
    }

    @Override
    public void update() {
        super.update();
        if (force == frame || isCollected) {
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, baseDamage, player, x + 18, y - 20, 2, -1, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, baseDamage, player, x + 17, y - 20, -3, -1, 15, 60));
            bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, baseDamage, player, x + 16, y - 23, 3, -2, 15, 60));
        }
    }
}