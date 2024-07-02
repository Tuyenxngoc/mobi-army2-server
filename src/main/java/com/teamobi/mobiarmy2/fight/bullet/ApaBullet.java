package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

public class ApaBullet extends Bullet {

    protected byte force;
    protected byte force2;
    protected long satThuongGoc;

    public ApaBullet(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force, byte force2) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        this.force = force;
        this.force2 = force2;
        this.satThuongGoc = (int) satThuong;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (this.force2 == frame) {
            this.collect = true;
            this.isCanCollision = false;
            this.satThuong = 0;
            fightManager.mapManager.handleCollision(this.X, this.Y, this);
            int arg = bulletManager.arg + Utils.toArg0_360(Utils.getArg(this.pl.x - this.X, this.pl.y - this.Y));
            if (bulletManager.arg < 90) {
                arg = 180 - arg;
            }
            arg = arg - 15;
            for (int i = 0; i < 3; i++, arg += 15) {
                int x = this.X + (20 * Utils.cos(arg) >> 10);
                int y = (this.Y - 12) - (20 * Utils.sin(arg) >> 10);
                int vxn = (this.force * Utils.cos(arg) >> 11);
                int vyn = -(this.force * Utils.sin(arg) >> 11);
                bulletManager.addBullet(new Bullet(bulletManager, (byte) 18, this.satThuongGoc, super.pl, x, y, vxn, vyn, 30, 100));
            }
        }
    }

}
