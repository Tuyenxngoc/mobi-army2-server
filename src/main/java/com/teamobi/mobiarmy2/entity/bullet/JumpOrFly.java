package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class JumpOrFly extends Bullet {
    public JumpOrFly(BulletManager bulletManager, byte bullId, int damage, Player player, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, player, X, Y, vx, vy, msg, g100);
    }
}