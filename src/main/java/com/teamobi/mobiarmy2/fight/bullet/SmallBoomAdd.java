package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class SmallBoomAdd extends Bullet {

    public SmallBoomAdd(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect && X > 0 && X < bulletManager.getFightManager().getMapManger().getWidth() && Y < bulletManager.getFightManager().getMapManger().getHeight()) {
//            Player players;
//            try {
//                players = new SmallBoom(bulletManager.getFightManager(), (byte) 11, "Small Boom", (byte) bulletManager.getFightManager().getTotalPlayers().allCount, 1000 + (fm.getLevelTeam() * 8), X, Y);
//                bulletManager.getAddboss().add(new BulletManager.AddBoss(players, 2));
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
        }
    }
}
