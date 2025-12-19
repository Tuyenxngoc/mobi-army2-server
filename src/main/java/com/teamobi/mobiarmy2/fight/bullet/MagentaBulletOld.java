package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class MagentaBulletOld extends Bullet {
    public MagentaBulletOld(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, byte force) {
        super(bulletManager, (byte) 59, damage, player, x, y, vx, vy, 0, 0);
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}