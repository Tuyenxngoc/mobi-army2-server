package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemBomMu extends Bullet {

    public ItemBomMu(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        this.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            for (int i = 0; i < 8; i++) {
                Player pl = bulletManager.getFightManager().getPlayers()[i];
                if (pl != null) {
                    int tamAH = Bullet.getImpactRadiusByBullId(bullId);
                    int kcX = Math.abs(pl.getX() - X);
                    int kcY = Math.abs(pl.getY() - pl.getHeight() / 2 - Y);
                    int kc = (int) Math.sqrt(kcX * kcX + kcY * kcY);
                    if (!pl.isDead() && pl.getInvisibleCount() <= 0 && kc <= tamAH + pl.getWidth() / 2) {
                        bulletManager.getFightManager().updateCantSee(pl);
                    }
                }
            }
        }
    }

}
