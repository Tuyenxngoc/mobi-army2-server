package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class JumpOrFly extends Bullet {

    public JumpOrFly(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
        super.isXuyenMap = true;
    }

    @Override
    public void nextXY() {
        if (this.isMaxY) {
            super.isXuyenMap = false;
        }
        super.nextXY();
        if (super.collect) {
            bulletManager.getFightManager().getPlayerTurn().setXY(X, Y);
        }
    }

}
