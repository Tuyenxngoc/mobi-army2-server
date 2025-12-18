package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

public class ItemLaserDelay extends Bullet {
    public ItemLaserDelay(BulletManager bulletManager, int damage, Player player, int x, int y) {
        super(bulletManager, (byte) 15, damage, player, x, y, 0, 0, 0, 0);
    }

    @Override
    public void update() {
        // Delay 26 + 1 frames
        for (int i = 0; i < 26; i++) {
            trajectory.add(new Point(x, y));
        }
        super.update();
        isCollected = true;
    }
}