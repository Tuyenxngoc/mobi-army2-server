package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

/**
 * @author tuyen
 */
public class B52Bullet extends Bullet {

    private final short toX;
    private final short toY;

    public B52Bullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100, short toX, short toY) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.isXuyenMap = true;
        super.isXuyenPlayer = true;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (toY <= Y) {
            super.isXuyenMap = false;
            super.isXuyenPlayer = false;
            X = toX;
            Y = toY;
        }
    }
}
