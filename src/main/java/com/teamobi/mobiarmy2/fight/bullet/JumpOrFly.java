package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class JumpOrFly extends Bullet {
    public JumpOrFly(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 36, damage, player, x, y, vx, vy, 0, 80);
    }
}