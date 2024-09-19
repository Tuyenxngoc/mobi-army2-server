package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.IBulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class BulletManager implements IBulletManager {

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
            this.X = bull.getX();
            this.Y = bull.getY();
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

    private IFightManager fightManager;
    private ArrayList<Bullet> bullets;
    private byte force2;
    private int mangNhenId;
    private boolean hasVoiRong;
    private List<VoiRong> voiRongs;
    private List<BomHenGio> boms;
    private List<AddBoss> addboss;
    private List<Bullets> buls;
    private byte mgtAddX;
    private byte mgtAddY;
    private byte typeSC;
    private short XSC;
    private short YSC;
    private short arg;
    private short XPL;
    private short YPL;

    public BulletManager(IFightManager fightManager) {
        this.fightManager = fightManager;
        this.bullets = new ArrayList<>();
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

    @Override
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
                case 0 -> {
                    if (pl.getUsedItemId() > 0 || (idGun != 0 && idGun != 14)) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 0, (pl.isUsePow() ? 630 : (nshoot == 2 ? 210 : 280)), pl, x, y, vx, vy, 80, 100));
                }
                case 1 -> {
                    if (pl.getUsedItemId() > 0 || idGun != 1) {
                        return;
                    }
                    int n = pl.isUsePow() ? 6 : 2;
                    for (byte i = 0; i < n; i++) {
                        bullets.add(new Bullet(this, (byte) 1, nshoot == 2 ? 109 : 145, pl, x, y, vx, vy, 50, 50));
                    }
                }

                case 2 -> {
                    if (pl.getUsedItemId() > 0 || (idGun != 2 && idGun != 14)) {
                        return;
                    }
                    int n = pl.isUsePow() ? 4 : 2;
                    for (byte i = 0; i < n; i++) {
                        int arg = angle + i * 5;
                        x = pl.getX() + (20 * Utils.cos(arg) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg) >> 10);
                        vx = (force * Utils.cos(arg) >> 10);
                        vy = -(force * Utils.sin(arg) >> 10);
                        bullets.add(new Bullet(this, (byte) 2, nshoot == 2 ? 75 : 100, pl, x, y, vx, vy, 80, 60));
                        if (i == 0) {
                            continue;
                        }
                        arg = angle - i * 5;
                        x = pl.getX() + (20 * Utils.cos(arg) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg) >> 10);
                        vx = (force * Utils.cos(arg) >> 10);
                        vy = -(force * Utils.sin(arg) >> 10);
                        bullets.add(new Bullet(this, (byte) 2, nshoot == 2 ? 75 : 100, pl, x, y, vx, vy, 80, 60));
                    }
                }
            }
        }
    }

    @Override
    public void fillXY() {
        boolean hasNext;
        do {
            hasNext = false;
            for (Bullet bull : this.bullets) {
                if (bull == null || bull.isCollect()) {
                    continue;
                }
                hasNext = true;
                bull.nextXY();
            }
        } while (hasNext);
    }

    public short[] getCollisionPoint(short X1, short Y1, short X2, short Y2, boolean isXuyenPlayer, boolean isXuyenMap) {
        int Dx = X2 - X1;
        int Dy = Y2 - Y1;
        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;
        Player us = this.fightManager.getPlayerTurn();
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
                if (fightManager.getMapManger().isCollision(X, Y)) {
                    return new short[]{X, Y};
                }
            }
            if (!isXuyenPlayer && us.getCharacterId() != 16) {
                for (int j = 0; j < fightManager.getTotalPlayers(); j++) {
                    Player pl = fightManager.getPlayers()[j];
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
                    Player pl = this.fightManager.getPlayers()[j];
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

    @Override
    public void refresh() {

    }
}
