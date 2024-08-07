package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class JumpOrFly extends Bullet {

    public JumpOrFly(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
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
            fightManager.getPlayerTurn().setXY((short) X, (short) Y);
        }
    }

}
