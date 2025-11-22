package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ChickyTrung extends Bullet {
    public ChickyTrung(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
    }

    @Override
    public void update() {
        if (frame == 0) {
            y += 8;
        }
        super.update();
    }
}