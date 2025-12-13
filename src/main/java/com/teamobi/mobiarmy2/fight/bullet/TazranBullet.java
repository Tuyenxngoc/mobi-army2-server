package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class TazranBullet extends Bullet {

    // -1: chưa quay, 0: bắt đầu quay, 1: đang quay
    private byte turnState;

    // true nếu hướng ban đầu là sang trái (vx <= 0)
    private boolean isLeft;

    public TazranBullet(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, (byte) 21, damage, player, x, y, vx, vy, msg, g100);
        this.turnState = -1;
        this.isLeft = (vx <= 0);
    }

    @Override
    public void update() {
        super.update();
        if (turnState == 0) {
            if (isLeft) {
                vx += 1;
            } else {
                vx -= 1;
            }
            turnState = 1;
        } else if (turnState == 1) {
            if (isLeft) {
                vx += 2;
            } else {
                vx -= 2;
            }
        } else if (isMaxY) {
            turnState = 0;
        }
    }
}