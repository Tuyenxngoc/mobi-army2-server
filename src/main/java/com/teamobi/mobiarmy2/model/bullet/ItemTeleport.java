package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemTeleport extends Bullet {
    public ItemTeleport(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            bulletManager.getFightManager().getPlayerTurn().setXY(X, Y);
        }
    }
}
