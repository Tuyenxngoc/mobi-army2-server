package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

public class MGTBulletNew extends Bullet {

    protected byte force;

    public MGTBulletNew(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        bullMNG.mgtAddX = 0;
        bullMNG.mgtAddY = 0;
        this.force = force;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (isMaxY) {
            this.collect = true;
            int nextX = X - XArray.get(0);
            int nextY = Y - YArray.get(0);
            int arg = Utils.getArg(nextX, nextY);
            nextX = ((force + 5) * Utils.cos(arg)) >> 10;
            nextY = ((force + 5) * Utils.sin(arg)) >> 10;
            short x = XArray.get(XArray.size() - 1);
            short y = YArray.get(YArray.size() - 1);
            while (true) {
                if ((x < -100) || (x > fm.mapManager.getWidth() + 100) || (y > fm.mapManager.getHeight() + 100)) {
                    break;
                }
                short[] XYVC = bullMNG.getCollisionPoint(x, y, (short) (x + nextX), (short) (y - nextY), false, false);
                if (XYVC != null) {
                    x = XYVC[0];
                    y = XYVC[1];
                    break;
                }
                x += nextX;
                y -= nextY;
            }

            fm.mapManager.handleCollision(x, y, this);
            XArray.add((short) x);
            YArray.add((short) y);
            bullMNG.mgtAddX = (byte) nextX;
            bullMNG.mgtAddY = (byte) nextY;
        }
    }

}
