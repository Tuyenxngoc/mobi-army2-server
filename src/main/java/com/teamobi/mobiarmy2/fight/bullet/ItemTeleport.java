package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemTeleport extends Bullet {
    public ItemTeleport(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, (byte) 5, damage, player, x, y, vx, vy, msg, g100);
        this.canCollide = false;
        this.canSuperType = false;
        this.canPassThroughPlayers = true;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            player.setXY(x, y);
        }
    }
}