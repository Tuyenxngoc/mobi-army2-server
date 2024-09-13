package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * @author tuyen
 */
@Getter
@Setter
public class BulletManager {

    public static class VoiRong {

        public int X;
        public int Y;
        public int count;

        public VoiRong(int X, int Y, int count) {
            this.X = X;
            this.Y = Y;
            this.count = count;
        }

    }

    public static class BomHenGio {

        public int id;
        public int X;
        public int Y;
        public int count;
        public Bullet bull;

        public BomHenGio(int id, Bullet bull, int count) {
            this.id = id;
            this.X = bull.X;
            this.Y = bull.Y;
            this.count = count;
            this.bull = bull;
        }

    }

    public static class AddBoss {

        public Player players;
        public int XPE;

        public AddBoss(Player players, int XPE) {
            this.players = players;
            this.XPE = XPE;
        }

    }

    public static class Bullets {

        public Bullet bull;

        public Bullets(Bullet bull) {
            this.bull = bull;
        }
    }

    public FightManager fm;
    protected ArrayList<Bullet> entrys;
    protected byte force2;

    public int mangNhenId;
    public boolean hasVoiRong;
    public ArrayList<VoiRong> voiRongs;
    public ArrayList<BomHenGio> boms;
    public ArrayList<AddBoss> addboss;
    public ArrayList<Bullets> buls;
    public byte mgtAddX;
    public byte mgtAddY;
    public byte typeSC;
    public short XSC;
    public short YSC;
    public short arg;
    public short XPL;
    public short YPL;

    public BulletManager(FightManager fm) {
        this.fm = fm;
        this.entrys = new ArrayList<>();
        this.voiRongs = new ArrayList<>();
        this.boms = new ArrayList<>();
        this.addboss = new ArrayList<>();
        this.buls = new ArrayList<>();
        this.hasVoiRong = false;
        this.force2 = -1;
        this.mangNhenId = 200;
        this.XSC = 0;
        this.YSC = 0;
        this.typeSC = 0;
    }

    public void addShoot(Player pl, byte bull, short angle, byte force, byte force2, byte nshoot) {
        this.XPL = pl.getX();
        this.YPL = pl.getY();
        this.arg = angle;
        this.force2 = force2;
        if (bull == 49) {
            force += 5;
        }
        this.typeSC = 0;
        int x, y, vx, vy, idGun;
        x = pl.getX() + (20 * Utils.cos(angle) >> 10);
        y = pl.getY() - 12 - (20 * Utils.sin(angle) >> 10);
        vx = (force * Utils.cos(angle) >> 10);
        vy = -(force * Utils.sin(angle) >> 10);

        idGun = pl.getCharacterId();
        if (nshoot > 2 || nshoot < 1) {
            return;
        }
        if (idGun == 13) {
            y -= 25;
        }
        for (int k = 0; k < nshoot; k++) {
            switch (bull) {
                // Gunner
                case 0:
                    if (pl.getUsedItemId() > 0 || (idGun != 0 && idGun != 14)) {
                        return;
                    }
                    entrys.add(new Bullet(this, (byte) 0, (pl.isUsePow() ? 630 : (nshoot == 2 ? 210 : 280)), pl, x, y, vx, vy, 80, 100));
                    break;
            }
        }
    }

    public void fillXY() {
        boolean hasNext;
        do {
            hasNext = false;
            for (Bullet bull : this.entrys) {
                if (bull == null || bull.isCollect()) {
                    continue;
                }
                hasNext = true;
                bull.nextXY();
            }
        } while (hasNext);
    }

    public void reset() {
        this.entrys.clear();
    }

    public short[] getCollisionPoint(short X1, short Y1, short X2, short Y2, boolean isXuyenPlayer, boolean isXuyenMap) {
        int Dx = X2 - X1;
        int Dy = Y2 - Y1;
        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;
        Player us = this.fm.getPlayerTurn();
        if (Dx < 0) {
            x_unit = x_unit2 = -1;
        } else if (Dx > 0) {
            x_unit = x_unit2 = 1;
        }
        if (Dy < 0) {
            y_unit = y_unit2 = -1;
        } else if (Dy > 0) {
            y_unit = y_unit2 = 1;
        }
        int k1 = Math.abs(Dx);
        int k2 = Math.abs(Dy);
        if (k1 > k2) {
            y_unit2 = 0;
        } else {
            k1 = Math.abs(Dy);
            k2 = Math.abs(Dx);
            x_unit2 = 0;
        }
        int k = k1 >> 1;
        short X = X1, Y = Y1;
        for (int i = 0; i <= k1; i++) {
            if (!isXuyenMap) {
                if (fm.getMapManger().isCollision(X, Y)) {
                    return new short[]{X, Y};
                }
            }
            if (!isXuyenPlayer && us.getCharacterId() != 16) {
                for (int j = 0; j < fm.getTotalPlayers(); j++) {
                    Player pl = fm.getPlayers()[j];
                    if (pl != null) {
                        if (pl.getCharacterId() > 15 && pl.isDead()) {
                            continue;
                        }
                        if (pl.isCollision(X, Y)) {
                            return new short[]{X, Y};
                        }
                    }
                }
            }
            if (us.getCharacterId() == 16) {
                for (int j = 0; j < ServerManager.maxPlayers; j++) {
                    Player pl = this.fm.getPlayers()[j];
                    if (pl == null || pl.isDead()) {
                        continue;
                    }
                    if (pl.isCollision(X, Y)) {
                        return new short[]{X, Y};
                    }
                }

            }
            k += k2;
            if (k >= k1) {
                k -= k1;
                X += x_unit;
                Y += y_unit;
            } else {
                X += x_unit2;
                Y += y_unit2;
            }
        }
        return null;
    }
}
