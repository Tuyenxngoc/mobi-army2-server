package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.bullet.*;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulletManager {
    private static final boolean MAGENTA_NEW_BULLET = true;
    private FightManager fightManager;
    private FightMapManager fightMapManager;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Player> pendingBosses = new ArrayList<>();
    private byte typeShoot;
    private byte superType;
    private short superX;
    private short superY;

    public BulletManager(FightManager fightManager) {
        this.fightManager = fightManager;
        this.fightMapManager = fightManager.getFightMapManager();
    }

    public void addPendingBoss(Player player) {
        pendingBosses.add(player);
    }

    public void addShoot(Player pl, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        if (bullId == 49) { // Magenta trong chế độ bắn thường không phải luyện tập phải cộng lực thêm 5
            force += 5;
        }

        //Tính vị trí bắt đầu của viên đạn
        int x = pl.getX() + (20 * Utils.cos(angle) >> 10);
        int y = pl.getY() - 12 - (20 * Utils.sin(angle) >> 10);

        // Tính vận tốc ban đầu của viên đạn
        int vx = (force * Utils.cos(angle) >> 10);
        int vy = -(force * Utils.sin(angle) >> 10);

        int characterId = pl.getCharacterId();
        boolean isUsingItem = pl.getUsedItemId() > 0;

        for (int k = 0; k < numShoot; k++) {
            switch (bullId) {
                case 0 -> {//Gunner
                    if (isUsingItem || (characterId != 0 && characterId != 14)) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 0, (pl.isUsePow() ? 630 : (numShoot == 2 ? 210 : 280)), pl, x, y, vx, vy, 80, 100));
                }
                case 1 -> { //Aka
                    if (isUsingItem || characterId != 1) {
                        return;
                    }
                    int damage = (numShoot == 2 ? 109 : 145);
                    int n = pl.isUsePow() ? 6 : 2;
                    for (int i = 0; i < n; i++) {
                        bullets.add(new Bullet(this, (byte) 1, damage, pl, x, y, vx, vy, 50, 50));
                    }
                }
                case 2 -> { //3tia
                    if (isUsingItem || (characterId != 2 && characterId != 14)) {
                        return;
                    }
                    int n = pl.isUsePow() ? 4 : 2;
                    int damage = numShoot == 2 ? 75 : 100;
                    for (int i = 0; i < n; i++) {
                        int arg = angle + i * 5;
                        x = pl.getX() + (20 * Utils.cos(arg) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg) >> 10);
                        vx = (force * Utils.cos(arg) >> 10);
                        vy = -(force * Utils.sin(arg) >> 10);
                        bullets.add(new Bullet(this, (byte) 2, damage, pl, x, y, vx, vy, 80, 60));
                        if (i == 0) {
                            continue;
                        }
                        arg = angle - i * 5;
                        x = pl.getX() + (20 * Utils.cos(arg) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg) >> 10);
                        vx = (force * Utils.cos(arg) >> 10);
                        vy = -(force * Utils.sin(arg) >> 10);
                        bullets.add(new Bullet(this, (byte) 2, damage, pl, x, y, vx, vy, 80, 60));
                    }
                }

                // Item bom huy diet
                case 4 -> {
                    if (pl.getUsedItemId() != 8) {
                        return;
                    }
                    pl.getUser().updateMission(5, 1);
                    bullets.add(new ItemBomB52(this, (byte) 4, 600, pl, x, y, vx, vy, 0, 80));
                }

                // Item bay
                case 5 -> {
                    if (pl.getUsedItemId() != 1) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemTeleport(this, (byte) 5, 0, pl, x, y, vx, vy, 0, 80));
                }

                // Item bom pha dat
                case 6 -> {
                    if (pl.getUsedItemId() != 6) {
                        return;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(this, (byte) 6, 200, pl, x, y, vx, vy, 70, 90));
                    }
                }

                // Item luu dan
                case 7 -> {
                    if (pl.getUsedItemId() != 7) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 7, 500, pl, x, y, vx, vy, 70, 80));
                }

                // Item to nhen
                case 8 -> {
                    if (pl.getUsedItemId() == 9) {
                        bullets.add(new ItemToNhen(this, (byte) 8, 300, pl, x, y, vx, vy, 70, 70));
                    }
                    if (characterId == 22) {
                        bullets.add(new SpiderDropSilk(this, (byte) 8, 300, pl));
                    }
                }

                case 9 -> {// King kong
                    if (isUsingItem || characterId != 3) {
                        return;
                    }
                    int arg2 = angle - 6;
                    for (int i = 0; i < 4; i++, arg2 += 4) {
                        x = pl.getX() + (20 * Utils.cos(arg2) >> 10);
                        y = pl.getY() - 12 - (20 * Utils.sin(arg2) >> 10);
                        vx = (force * Utils.cos(arg2) >> 10);
                        vy = -(force * Utils.sin(arg2) >> 10);
                        bullets.add(new Bullet(this, (byte) 9, pl.isUsePow() ? 210 : (numShoot == 2 ? 79 : 105), pl, x, y, vx, vy, 40, 90));
                    }
                }

                case 10 -> {// Rocket
                    if (isUsingItem || (characterId != 4 && characterId != 14)) {
                        return;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(this, (byte) 10, pl.isUsePow() ? 240 : (numShoot == 2 ? 80 : 107), pl, x, y, vx, vy, 50, 80));
                    }
                }

                case 11 -> {// Granos
                    if (isUsingItem || characterId != 5) {
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(this, (byte) 11, pl.isUsePow() ? 140 : (numShoot == 2 ? 47 : 62), pl, x, y, vx, vy, 30, 90));
                    }
                }

                // Item dan voi rong
                case 13 -> {
                    if (pl.getUsedItemId() != 17) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemVoiRong(this, (byte) 13, 0, pl, x, y, vx, vy, 50, 120));
                }

                // Item dan laser
                case 14 -> {
                    if (pl.getUsedItemId() != 16) {
                        return;
                    }
                    bullets.add(new ItemLaser(this, (byte) 14, 500, pl, x, y, vx, vy, 10, 50));
                }

                // Item dan trai pha
                case 16 -> {
                    if (pl.getUsedItemId() != 11) {
                        return;
                    }
                    bullets.add(new ItemTraiPha(this, (byte) 16, 200, pl, x, y, vx, vy, 0, 100));
                }

                // Apache
                case 17 -> {
                    if (isUsingItem || characterId != 8) {
                        return;
                    }
                    bullets.add(new ApaBullet(this, (byte) 17, pl.isUsePow() ? 216 : (numShoot == 2 ? 81 : 108), pl, x, y, vx, vy, 30, 100, angle, force, force2));
                }

                // Chicky
                case 19 -> {
                    if (isUsingItem || characterId != 6) {
                        return;
                    }
                    bullets.add(new ChickyBullet(this, (byte) 19, pl.isUsePow() ? 500 : (numShoot == 2 ? 169 : 225), pl, x, y, vx, vy, 20, 50, force2));
                }

                // Tazan
                case 21 -> {
                    if (isUsingItem || characterId != 7) {
                        return;
                    }
                    bullets.add(new TazranBullet(this, (byte) 21, pl.isUsePow() ? 800 : (numShoot == 2 ? 225 : 340), pl, x, y, vx, vy, 10, 50));
                }

                // Item chuot gan bom
                case 22 -> {
                    if (pl.getUsedItemId() != 18) {
                        return;
                    }
                    bullets.add(new ItemChuotGanBom(this, (byte) 22, 500, pl, x, y, force, angle < 89));
                }

                // Item Sao Bang
                case 23 -> {
                    if (pl.getUsedItemId() != 21) {
                        return;
                    }
                    bullets.add(new ItemSaoBang(this, (byte) 23, 200, pl, x, y, vx, vy, 20, 100));
                }

                // Item Dan xuyen dat
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
                    if (characterId != 12) {
                        return;
                    }
                    bullets.add(new BigBoomBum(this, (byte) 31, 1000, pl));
                }

                // Small boom bum
                case 32 -> {
                    if (characterId != 11) {
                        return;
                    }
                    bullets.add(new SmallBoomBum(this, (byte) 32, 100, pl));
                }

                //dan nhen
                case 33 -> {
                    if (characterId != 13) {
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(this, (byte) 33, 120, pl, x, y, vx, vy, 50, 80));
                    }
                }

                //small bom add
                case 34 -> {
                    if (characterId != 12) {
                        return;
                    }
                    pl.setLucky(false);
                    bullets.add(new SmallBoomAdd(this, (byte) 34, 0, pl, x, y, vx, vy, 0, 80));
                }

                //T-rex or Robot jump
                case 35 -> {
                    if (characterId != 15 && characterId != 14) {
                        return;
                    }
                    bullets.add(new Jump(this, (byte) 35, 1200, pl));
                }

                //Jump Fly 
                case 36 -> {
                    if (characterId != 14 && characterId != 13) {
                        return;
                    }
                    bullets.add(new JumpOrFly(this, (byte) 36, 0, pl, x, y, vx, vy, 0, 80));
                }

                //T-rex Rocket
                case 37 -> {
                    if (characterId != 15) {
                        return;
                    }
                    bullets.add(new BigRocKet(this, (byte) 37, 570, pl));
                }

                // T-rex lazer
                case 40 -> {
                    if (characterId != 15) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 40, 220, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                // T-rex white
                case 41 -> {
                    if (characterId != 15) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 41, 200, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                //UFO Lazer
                case 42 -> {
                    if (characterId != 16) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 42, 1000, pl, pl.getX(), pl.getY(), vx, vy + 10, 10, 0));
                }

                //Balloon Gun Big
                case 43 -> {
                    if (characterId != 17) {
                        return;
                    }
                    for (byte i = 0; i < 10; i++) {
                        if (i > 0) {
                            x = x + 105;
                        }
//                        if (x > this.fm.mapMNG.getWidth()) {
//                            x = 105 - (x - this.fm.mapMNG.getWidth());
//                        }
                        bullets.add(new Bullet(this, (byte) 43, 300, pl, x, (short) (y + 50), vx, vy, 0, 100));
                    }
                }

                //Balloon Gun
                case 44 -> {
                    if (characterId != 17) {
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
                    if (characterId != 17) {
                        return;
                    }
                    bullets.add(new BalloonLazer(this, (byte) 45, 500, pl, x + 65, y - 27));
                }

                case 47 -> {
                    if (characterId != 22) {
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
                    if (isUsingItem || characterId != 9) {
                        return;
                    }
                    if (MAGENTA_NEW_BULLET) {
                        bullets.add(new MagentaBulletNew(this, (byte) 49, pl.isUsePow() ? 1000 : (numShoot == 2 ? 308 : 400), pl, x, y, vx, vy, 40, 70, force));
                    } else {
                        vx = (1600 * Utils.cos(angle) >> 10);
                        vy = -(1600 * Utils.sin(angle) >> 10);
                        bullets.add(new MagentaBulletOld(this, (byte) 59, pl.isUsePow() ? 1000 : (numShoot == 2 ? 308 : 400), pl, x, y, vx, vy, force));
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

                //T-rex TG Rocket
                case 58 -> {
                    if (characterId != 15) {
                        return;
                    }
                    bullets.add(new BigRocKet(this, (byte) 37, 600, pl));
                }

                //T-rex TG jump
                case 59 -> {
                    if (characterId != 15 && characterId != 14) {
                        return;
                    }
                    bullets.add(new Jump(this, (byte) 35, 100000, pl));
                }

            }
        }
    }

    public void resetBullets() {
        bullets.clear();
        typeShoot = 0;
        superType = 0;
        superX = 0;
        superY = 0;
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void updateBullets() {
        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                if (bullet == null || bullet.isCollected()) {
                    continue;
                }
                bullet.update();
                updated = true;
            }
        } while (updated);
    }

    public short[] getCollisionPoint(short x1, short y1, short x2, short y2, boolean canPassThroughPlayers, boolean canPassThroughMap) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;

        if (deltaX < 0) {
            x_unit = x_unit2 = -1;
        } else if (deltaX > 0) {
            x_unit = x_unit2 = 1;
        }
        if (deltaY < 0) {
            y_unit = y_unit2 = -1;
        } else if (deltaY > 0) {
            y_unit = y_unit2 = 1;
        }

        int k1 = Math.abs(deltaX);
        int k2 = Math.abs(deltaY);
        if (k1 > k2) {
            y_unit2 = 0;
        } else {
            k1 = Math.abs(deltaY);
            k2 = Math.abs(deltaX);
            x_unit2 = 0;
        }
        int k = k1 >> 1;
        short X = x1, Y = y1;

        for (int i = 0; i <= k1; i++) {
            // Check map collision
            if (!canPassThroughMap) {
                if (fightMapManager.isCollision(X, Y)) {
                    return new short[]{X, Y, 0};
                }
            }

            // Check player collision
            if (!canPassThroughPlayers) {
                for (int j = 0; j < fightManager.getTotalPlayers(); j++) {
                    Player pl = fightManager.getPlayers()[j];
                    if (pl != null && pl.isCollision(X, Y)) {
                        return new short[]{X, Y, 1};
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

    public void handleCollision(short x, short y, Bullet bullet) {
        fightMapManager.collision(x, y, bullet);
        fightManager.collisionPlayers(x, y, bullet);
    }

}
