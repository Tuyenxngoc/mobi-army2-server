package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

// Item tự sát
public class SuicideItem extends Bullet {

    public SuicideItem(BulletManager bulletManager, int damage, Player player) {
        super(bulletManager, (byte) 50, damage, player, player.getX(), player.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void update() {
        isCollected = true;
        trajectory.add(new Point(x, y));
        trajectory.add(new Point(x, y));
        bulletManager.handleCollision(x, y, this);
    }
}