package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

import java.io.IOException;

public class ItemDongBang extends Bullet {

    public ItemDongBang(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            for (int i = 0; i < 8; i++) {
                Player pl = fightManager.players[i];
                if (pl != null) {
                    int tamAH = Bullet.getTamAHByBullID(bullId);
                    int kcX = Math.abs(pl.x - X);
                    int kcY = Math.abs(pl.y - pl.height / 2 - Y);
                    int kc = (int) Math.sqrt(kcX * kcX + kcY * kcY);
                    if (!pl.isDie && pl.voHinhCount <= 0 && kc <= tamAH + pl.width / 2) {
                        try {
                            this.fightManager.updateCantMove(pl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
