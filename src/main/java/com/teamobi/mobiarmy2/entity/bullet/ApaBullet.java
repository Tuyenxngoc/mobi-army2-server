package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ApaBullet extends Bullet {
    public ApaBullet(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
    }

    public ApaBullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int x, int y, int vx, int vy, int msg, int g100, byte forcee, byte forc2) {
        super();
    }
}