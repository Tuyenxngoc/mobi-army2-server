package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.boss.SmallBoom;

import java.io.IOException;

public class SmallBoomAdd extends Bullet {

    public SmallBoomAdd(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect && X > 0 && X < fm.mapManager.width && Y < fm.mapManager.height) {
            Player players;
            try {
                players = new SmallBoom(fm, (byte) 11, "Small Boom", (byte) fm.allCount, 1000 + (fm.getLevelTeam() * 8), X, Y);
                bullMNG.addboss.add(new BulletManager.AddBoss(players, 2));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
