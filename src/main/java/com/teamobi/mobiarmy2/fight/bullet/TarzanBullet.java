package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class TarzanBullet extends Bullet {

    protected byte quayLai;
    protected boolean addTZ;

    public TarzanBullet(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        this.quayLai = -1;
        this.addTZ = (vx <= 0);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (quayLai == 0) {
            if (addTZ) {
                vx += 1;
            } else {
                vx -= 1;
            }
            quayLai = 1;
        } else if (quayLai == 1) {
            if (addTZ) {
                vx += 2;
            } else {
                vx -= 2;
            }
        } else if (super.isMaxY) {
            quayLai = 0;
        }
    }

}
