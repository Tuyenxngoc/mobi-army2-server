package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemTeleport extends Bullet {
    public ItemTeleport(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
        super.canPassThroughPlayers = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.isCollected) {
            bulletManager.getFightManager().getPlayerTurn().setXY(X, Y);
        }
    }
}
