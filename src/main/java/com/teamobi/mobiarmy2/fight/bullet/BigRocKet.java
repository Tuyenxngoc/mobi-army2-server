package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;

public class BigRocKet extends Bullet {

    private short toX;
    private boolean flyshoot;

    public BigRocKet(BulletManager bullMNG, byte bullId, long satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.X, pl.Y - (pl.height + 12), 0, -50, 0, 0);
        super.g100 = 80;
        ArrayList<Short> ar = new ArrayList();
        for (int i = 0; i < ServerManager.maxPlayers; i++) {
            if (this.fm.players[i] != null && !this.fm.players[i].isDie) {
                ar.add(this.fm.players[i].X);
            }
        }
        this.toX = ar.get(Utils.nextInt(ar.size()));
        this.flyshoot = true;
    }

    @Override
    public void nextXY() {
        if (this.flyshoot) {
            this.vy = -50;
            if (this.Y < -1200) {
                vy = 0;
                for (byte i = 0; i < 21; i++) {
                    X = (short) (X - (pl.X - toX) / 20);
                    super.frame++;
                    this.XArray.add(X);
                    this.YArray.add(Y);
                }
                X = toX;
                this.flyshoot = false;
            }
        }
        super.nextXY();
    }

}
