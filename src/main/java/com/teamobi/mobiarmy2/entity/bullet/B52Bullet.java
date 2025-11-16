package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class B52Bullet extends Bullet {
    private final short toX;
    private final short toY;

    public B52Bullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100, short toX, short toY) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.canPassThroughMap = true;
        super.canPassThroughPlayers = true;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (toY <= Y) {
            super.canPassThroughMap = false;
            super.canPassThroughPlayers = false;
            X = toX;
            Y = toY;
        }
    }
}
