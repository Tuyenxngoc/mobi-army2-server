package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;
import com.teamobi.mobiarmy2.fight.boss.BigBoom;

public class BigBoomBum extends Bullet {
    public BigBoomBum(BulletManager bulletManager, int damage, Player player) {
        super(bulletManager, (byte) 31, damage, player, player.getX(), player.getY() - 20, 0, 0, 0, 0);
    }

    @Override
    public void update() {
        isCollected = true;
        trajectory.add(new Point(x, y));
        y -= 2;
        trajectory.add(new Point(x, y));

        ((BigBoom) player).bomAction();
        bulletManager.handleCollision(x, y, this);
    }
}