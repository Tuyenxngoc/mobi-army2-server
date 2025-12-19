package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemBomHenGio extends Bullet {
    public ItemBomHenGio(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 57, damage, player, x, y, vx, vy, 0, 120);
        this.canPassThroughPlayers = true;
        this.canSuperType = false;
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            // Trigger the effect of the ItemBomHenGio when collected
        }
    }
}