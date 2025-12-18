package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

public class ItemKhoangDat extends Bullet {
    private final int maxFrame;

    public ItemKhoangDat(BulletManager bulletManager, Player player, byte force) {
        super(bulletManager, (byte) 30, 0, player, player.getX(), player.getY() + 8, 0, 2, 0, 0);
        this.maxFrame = force * 2;
        this.canSuperType = false;
    }

    @Override
    public void update() {
        frame++;

        trajectory.add(new Point(x, y));

        if (bulletManager.getFightMapManager().isCollision(x, y)) {
            bulletManager.handleCollision(x, y, this);
        }

        y += vy;

        if (frame == maxFrame) {
            isCollected = true;
        }
    }
}