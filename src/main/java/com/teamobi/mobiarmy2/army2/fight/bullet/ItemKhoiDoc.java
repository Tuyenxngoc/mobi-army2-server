package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.Player;
import com.teamobi.mobiarmy2.army2.server.ServerManager;

import java.io.IOException;

public class ItemKhoiDoc extends Bullet {

    public ItemKhoiDoc(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            for (int i = 0; i < ServerManager.maxPlayers; i++) {
                Player pl = fm.players[i];
                if (pl != null) {
                    int tamAH = Bullet.getTamAHByBullID(bullId);
                    int kcX = Math.abs(pl.X - X);
                    int kcY = Math.abs(pl.Y - pl.height / 2 - Y);
                    int kc = (int) Math.sqrt(kcX * kcX + kcY * kcY);
                    if (!pl.isDie && pl.voHinhCount <= 0 && kc <= tamAH + pl.width / 2) {
                        try {
                            this.fm.updateBiDoc(pl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
