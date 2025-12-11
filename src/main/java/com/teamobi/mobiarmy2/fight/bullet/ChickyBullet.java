package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ChickyBullet extends Bullet {
    private byte force2;
    private int baseDamage;

    public ChickyBullet(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100, byte force2) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
        this.force2 = force2;
        this.baseDamage = damage;
    }

    @Override
    public void update() {
        super.update();
        if (force2 == frame) {
            bulletManager.addBullet(new ChickyTrung(bulletManager, (byte) 20, baseDamage, player, x, y, 0, 0, 10, 30));
        }
    }
}