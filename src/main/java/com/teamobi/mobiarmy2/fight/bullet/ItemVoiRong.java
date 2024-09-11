package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.BulletManager.VoiRong;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemVoiRong extends Bullet {

    public ItemVoiRong(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.bullMNG.hasVoiRong = true;
            this.bullMNG.voiRongs.add(new VoiRong(X, Y, 3));
        }
    }

}
