package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class BalloonLazer extends Bullet {
    public BalloonLazer(BulletManager bulletManager, int damage, Player player, int x, int y) {
        super(bulletManager, (byte) 45, damage, player, x, y, 0, 0, 0, 0);
    }
}