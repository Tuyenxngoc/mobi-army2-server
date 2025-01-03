package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.util.Utils;

public class MGTBulletNew extends Bullet {

    protected byte force;

    public MGTBulletNew(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        bullMNG.setMgtAddX((byte) 0);
        bullMNG.setMgtAddY((byte) 0);
        this.force = force;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (isMaxY) {
            this.collect = true;
            int nextX = X - XArray.getFirst();
            int nextY = Y - YArray.getFirst();
            int arg = Utils.getArg(nextX, nextY);
            nextX = ((force + 5) * Utils.cos(arg)) >> 10;
            nextY = ((force + 5) * Utils.sin(arg)) >> 10;
            short x = XArray.get(XArray.size() - 1);
            short y = YArray.get(YArray.size() - 1);
            while (true) {
                if ((x < -100) || (x > bulletManager.getFightManager().getMapManger().getWidth() + 100) || (y > bulletManager.getFightManager().getMapManger().getHeight() + 100)) {
                    break;
                }
                short[] XYVC = bulletManager.getCollisionPoint(x, y, (short) (x + nextX), (short) (y - nextY), false, false);
                if (XYVC != null) {
                    x = XYVC[0];
                    y = XYVC[1];
                    break;
                }
                x += nextX;
                y -= nextY;
            }

            bulletManager.getFightManager().getMapManger().collision(x, y, this);
            XArray.add(x);
            YArray.add(y);
            bulletManager.setMgtAddX((byte) nextX);
            bulletManager.setMgtAddY((byte) nextY);
        }
    }

}
