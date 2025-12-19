package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemVoiRong extends Bullet {
    public ItemVoiRong(BulletManager bulletManager, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 13, 0, player, x, y, vx, vy, 50, 120);
        this.canSuperType = false;
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            // Logic for when the item is collected can be added here
        }
    }
}