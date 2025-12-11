package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class MagentaBulletOld extends Bullet {
    public MagentaBulletOld(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
    }

    public MagentaBulletOld(BulletManager bulletManager, byte bullId, int damage, Player pl, int x, int y, int vx, int vy, byte force) {
        super();
    }
}