package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.bullet.*;
import com.teamobi.mobiarmy2.fight.item.ItemBomB52;
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
    private List<Boss> addboss;
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

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    @Override
    public List<Boss> getAddboss() {
        return addboss;
    }

    @Override
    public void decreaseSpiderWebCount() {
        mangNhenId++;
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
                case 0 -> {//Gunner
                    if (pl.getUsedItemId() > 0 || (idGun != 0 && idGun != 14)) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 0, (pl.isUsePow() ? 630 : (nshoot == 2 ? 210 : 280)), pl, x, y, vx, vy, 80, 100));
                }

                case 1 -> {//Aka
                    if (pl.getUsedItemId() > 0 || idGun != 1) {
                        return;
                    }
                    int n = pl.isUsePow() ? 6 : 2;
                    for (byte i = 0; i < n; i++) {
                        bullets.add(new Bullet(this, (byte) 1, nshoot == 2 ? 109 : 145, pl, x, y, vx, vy, 50, 50));
                    }
                }

                case 2 -> {//Electric
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

                case 4 -> {//B52
                    if (pl.getUsedItemId() != 8) {
                        return;
                    }
                    pl.getUser().updateMission(5, 1);
                    bullets.add(new ItemBomB52(this, (byte) 4, 600, pl, x, y, vx, vy, 0, 80));
                }

                case 5 -> {//Fly
                    if (pl.getUsedItemId() != 1) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemTeleport(this, (byte) 5, 0, pl, x, y, vx, vy, 0, 80));
                }

                case 6 -> {//Bom phá đất
                    if (pl.getUsedItemId() != 6) {
                        return;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(this, (byte) 6, 200, pl, x, y, vx, vy, 70, 90));
                    }
                }

                case 7 -> {//Lưu đạn
                    if (pl.getUsedItemId() != 7) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 7, 500, pl, x, y, vx, vy, 70, 80));
                }

                case 8 -> {//Tơ nhện
                    if (pl.getUsedItemId() == 9) {
                        bullets.add(new ItemToNhen(this, (byte) 8, 300, pl, x, y, vx, vy, 70, 70));
                        return;
                    }

                    if (idGun == 22) {
                        bullets.add(new SpiderDropSilk(this, (byte) 8, 300, pl));
                    }
                }

                case 9 -> {//Kingkong
                    if (pl.getUsedItemId() > 0 || idGun != 3) {
                        return;
                    }
                    int arg2 = angle - 6;
                    for (int i = 0; i < 4; i++, arg2 += 4) {
                        x = pl.getX() + (20 * Utils.cos(arg2) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg2) >> 10);
                        vx = (force * Utils.cos(arg2) >> 10);
                        vy = -(force * Utils.sin(arg2) >> 10);
                        bullets.add(new Bullet(this, (byte) 9, pl.isUsePow() ? 210 : (nshoot == 2 ? 79 : 105), pl, x, y, vx, vy, 40, 90));
                    }
                }

                case 10 -> {//Rocket
                    if (pl.getUsedItemId() >= 0 || (idGun != 4 && idGun != 14)) {
                        return;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(this, (byte) 10, pl.isUsePow() ? 240 : (nshoot == 2 ? 80 : 107), pl, x, y, vx, vy, 50, 80));
                    }
                }

                case 11 -> {//Granos
                    if (pl.getUsedItemId() >= 0 || idGun != 5) {
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(this, (byte) 11, pl.isUsePow() ? 140 : (nshoot == 2 ? 47 : 62), pl, x, y, vx, vy, 30, 90));
                    }
                }

                case 13 -> {//Item dan voi rong
                    if (pl.getUsedItemId() != 17) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemVoiRong(this, (byte) 13, 0, pl, x, y, vx, vy, 50, 120));
                }

                case 14 -> { //Item dan laser
                    if (pl.getUsedItemId() != 16) {
                        return;
                    }
                    bullets.add(new ItemLaser(this, (byte) 14, 500, pl, x, y, vx, vy, 10, 50));
                }

                case 16 -> {//Item dan trai pha
                    if (pl.getUsedItemId() != 11) {
                        return;
                    }
                    bullets.add(new ItemTraiPha(this, (byte) 16, 200, pl, x, y, vx, vy, 0, 100));

                }

                case 17 -> {//Apache
                    if (pl.getUsedItemId() >= 0 || idGun != 8) {
                        return;
                    }
                    bullets.add(new ApaBullet(this, (byte) 17, pl.isUsePow() ? 216 : (nshoot == 2 ? 81 : 108), pl, x, y, vx, vy, 30, 100, force, force2));
                }

                case 19 -> {
                    if (pl.getUsedItemId() >= 0 || idGun != 6) {
                        return;
                    }
                    bullets.add(new ChickyBullet(this, (byte) 19, pl.isUsePow() ? 500 : (nshoot == 2 ? 169 : 225), pl, x, y, vx, vy, 20, 50, force2));

                }

                case 21 -> {
                    if (pl.getUsedItemId() >= 0 || idGun != 7) {
                        return;
                    }
                    bullets.add(new TazranBullet(this, (byte) 21, pl.isUsePow() ? 800 : (nshoot == 2 ? 225 : 340), pl, x, y, vx, vy, 10, 50));

                }

                case 22 -> {
                    if (pl.getUsedItemId() != 18) {
                        return;
                    }
                    bullets.add(new ItemChuotGanBom(this, (byte) 22, 500, pl, x, y, force, angle < 89));
                }

                case 23 -> {
                    if (pl.getUsedItemId() != 21) {
                        return;
                    }
                    bullets.add(new ItemSaoBang(this, (byte) 23, 200, pl, x, y, vx, vy, 20, 100));

                }

                case 25 -> {
                    if (pl.getUsedItemId() != 20) {
                        return;
                    }
                    vy = (force * Utils.sin(-angle) >> 10);
                    bullets.add(new ItemXuyenDat(this, (byte) 25, 500, pl, x, y, vx, vy, 0, -50, force));

                }

                // Item ten lua
                case 26 -> {
                    if (pl.getUsedItemId() != 19) {
                        return;
                    }
                    bullets.add(new ItemTenLua(this, (byte) 26, 200, pl, x, y, vx, vy, 30, 60, force));
                }

                // Item mua dan
                case 28 -> {
                    if (pl.getUsedItemId() != 22) {
                        return;
                    }
                    vx = 0;
                    vy = -force / 2;
                    bullets.add(new ItemMuaDan(this, (byte) 28, 200, pl, x, y, vx, vy, 0, 20));
                }

                // Item khoang dat
                case 30 -> {
                    if (pl.getUsedItemId() != 23) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemKhoangDat(this, (byte) 30, pl, pl.getX(), pl.getY(), force));
                }

                // Big boom bum
                case 31 -> {
                    if (idGun != 12) {
                        return;
                    }
                    bullets.add(new BigBoomBum(this, (byte) 31, pl.getHp(), pl));
                }

                // Small boom bum
                case 32 -> {
                    if (idGun != 11) {
                        return;
                    }
                    bullets.add(new SmallBoomBum(this, (byte) 32, pl.getHp(), pl));
                }

                //dan nhen
                case 33 -> {
                    if (idGun != 13) {
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(this, (byte) 33, 120, pl, x, y, vx, vy, 50, 80));
                    }
                }

                //small bom add
                case 34 -> {
                    if (idGun != 12) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new SmallBoomAdd(this, (byte) 34, 0, pl, x, y, vx, vy, 0, 80));
                }

                //T-rex or Robot jump
                case 35 -> {
                    if (idGun != 15 && idGun != 14) {
                        return;
                    }
                    bullets.add(new Jump(this, (byte) 35, 1200, pl));
                }

                //Jump Fly 
                case 36 -> {
                    if (idGun != 14 && idGun != 13) {
                        return;
                    }
                    bullets.add(new JumpOrFly(this, (byte) 36, 0, pl, x, y, vx, vy, 0, 80));
                }

                //T-rex Rocket
                case 37 -> {
                    if (idGun != 15) {
                        return;
                    }
                    bullets.add(new BigRocKet(this, (byte) 37, 570, pl));
                }

                // T-rex lazer
                case 40 -> {
                    if (idGun != 15) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 40, 220, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                // T. rex white
                case 41 -> {
                    if (idGun != 15) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 41, 200, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                //UFO Lazer
                case 42 -> {
                    if (idGun != 16) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 42, 1000, pl, pl.getX(), pl.getY(), vx, vy + 10, 10, 0));
                }

                //Balloon Gun Big
                case 43 -> {
                    if (idGun != 17) {
                        return;
                    }
                    for (byte i = 0; i < 10; i++) {
                        if (i > 0) {
                            x = x + 105;
                        }
                        if (x > fightManager.getMapManger().getWidth()) {
                            x = 105 - (x - fightManager.getMapManger().getWidth());
                        }
                        bullets.add(new Bullet(this, (byte) 43, 300, pl, x, (short) (y + 50), vx, vy, 0, 100));
                    }
                }

                //Balloon Gun
                case 44 -> {
                    if (idGun != 17) {
                        return;
                    }
                    short vxrd = 0;
                    for (int i = 0; i < 15; i++) {
                        vxrd = (short) Utils.nextInt(-10, 10);
                        bullets.add(new Bullet(this, (byte) 44, 100, pl, x + 51, y + 40, vx + vxrd, vy, 40, 40));
                    }
                }

                //Balloon Lazer
                case 45 -> {
                    if (idGun != 17) {
                        return;
                    }
                    bullets.add(new BalloonLazer(this, (byte) 45, 500, pl, x + 65, y - 27));
                }

                case 47 -> {
                    if (idGun != 22) {
                        return;
                    }
                    for (byte i = 0; i < 5; i++) {
                        int arg = angle + i * 5;
                        x = pl.getX() + (30 * Utils.cos(arg) >> 10);
                        y = pl.getY() - 12 - (30 * Utils.sin(arg) >> 10);
                        vx = (force * Utils.cos(arg) >> 10);
                        vy = -(force * Utils.sin(arg) >> 10);
                        bullets.add(new Bullet(this, (byte) 47, 400, pl, x, y, vx, vy, 0, 0));
                    }
                }

                // Magenta
                case 49 -> {
                    if (pl.getUsedItemId() >= 0 || idGun != 9) {
                        return;
                    }
                    if (ServerManager.mgtBullNew) {
                        bullets.add(new MGTBulletNew(this, (byte) 49, pl.isUsePow() ? 1000 : (nshoot == 2 ? 308 : 400), pl, x, y, vx, vy, 40, 70, force));
                    } else {
                        vx = (1600 * Utils.cos(angle) >> 10);
                        vy = -(1600 * Utils.sin(angle) >> 10);
                        bullets.add(new MGTBulletOld(this, (byte) 59, pl.isUsePow() ? 1000 : (nshoot == 2 ? 308 : 400), pl, x, y, vx, vy, force));
                    }
                }

                // Item tu sat
                case 50 -> {
                    if (pl.getUsedItemId() != 24) {
                        return;
                    }
                    bullets.add(new ItemTuSat(this, (byte) 50, 1500, pl));
                }

                // Item bom mu
                case 51 -> {
                    if (pl.getUsedItemId() != 25) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemBomMu(this, (byte) 51, 0, pl, x, y, vx, vy, 5, 60));
                }

                // Item Khoang dat 2
                case 52 -> {
                    if (pl.getUsedItemId() != 26) {
                        return;
                    }
                    bullets.add(new ItemKhoangDat2(this, (byte) 52, 500, pl, x, y, vx, vy, 10, 100));
                }

                // Item Dong Bang
                case 54 -> {
                    if (pl.getUsedItemId() != 28) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemDongBang(this, (byte) 54, 0, pl, x, y, vx, vy, 0, 80));
                }

                // Item Khoi Doc
                case 55 -> {
                    if (pl.getUsedItemId() != 29) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemKhoiDoc(this, (byte) 55, 150, pl, x, y, vx, vy, 6, 60));
                }

                // Item To nhen 2
                case 56 -> {
                    if (pl.getUsedItemId() != 30) {
                        return;
                    }
                    int arg3 = angle - 5;
                    for (int i = 0; i < 3; i++, arg3 += 5) {
                        x = pl.getX() + (20 * Utils.cos(arg3) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg3) >> 10);
                        vx = (force * Utils.cos(arg3) >> 10);
                        vy = -(force * Utils.sin(arg3) >> 10);
                        bullets.add(new ItemToNhen(this, (byte) 56, 300, pl, x, y, vx, vy, 70, 70));
                    }
                }

                // Item Bom hen gio
                case 57 -> {
                    if (pl.getUsedItemId() != 31) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemBomHenGio(this, (byte) 57, 600, pl, x, y, vx, vy, 0, 120));
                }

            }
        }
    }

    @Override
    public void fillXY() {
        boolean hasNext;
        do {
            hasNext = false;
            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                if (bullet == null || bullet.isCollect()) {
                    continue;
                }
                hasNext = true;
                bullet.nextXY();
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
