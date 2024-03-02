package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.MapEntry;
import com.teamobi.mobiarmy2.army2.fight.Player;


public class ItemToNhen extends Bullet {

    public ItemToNhen(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.fm.mapMNG.addEntry(new MapEntry(this.bullMNG.mangNhenId++, (short) (X - 21), (short) (Y - 20), MapEntry.mangNhenData, (short) MapEntry.mangNhenW, (short) MapEntry.mangNhenH, true));
        }
    }

}
