package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

public class B52Bullet extends Bullet {
    private Point target;

    public B52Bullet(BulletManager bulletManager, int damage, Player player, int x, int y, Point target) {
        super(bulletManager, (byte) 3, damage, player, x, y, 5, 0, 0, 70);
        this.target = target;
        this.canSuperType = false;
        this.canPassThroughMap = true;
        this.canPassThroughPlayers = true;
    }

    @Override
    public void update() {
        super.update();

        boolean reachedX = x >= target.getX();
        boolean reachedY = y >= target.getY();

        if (reachedX) {
            x = (short) target.getX();
            vx = 0;
        }

        if (reachedY) {
            y = (short) target.getY();
            vy = 0;
        }

        if (reachedY) {
            canPassThroughMap = false;
            canPassThroughPlayers = false;
        }
    }
}