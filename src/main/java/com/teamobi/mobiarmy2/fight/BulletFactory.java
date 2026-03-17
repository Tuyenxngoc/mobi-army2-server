package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.bullet.*;
import com.teamobi.mobiarmy2.util.RandomUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BulletFactory {
    private static final boolean MAGENTA_NEW_BULLET = true;

    public static int[] getInitTrajectory(Player pl, int angle, int force, int radius) {
        int cos = Utils.cos(angle);
        int sin = Utils.sin(angle);

        // Tính vị trí bắt đầu của viên đạn
        int x = pl.getX() + (radius * cos >> 10);
        int y = pl.getY() - 12 - (radius * sin >> 10);

        // Tính vận tốc ban đầu của viên đạn
        int vx = (force * cos >> 10);
        int vy = -(force * sin >> 10);

        return new int[]{x, y, vx, vy};
    }

    public static List<Bullet> createBullets(BulletManager bulletManager, Player pl, byte bullId, short angle,
                                             byte force, byte force2, byte numShoot) {
        List<Bullet> bullets = new ArrayList<>();

        if (bullId == 49) { // Magenta trong chế độ bắn thường không phải luyện tập phải cộng lực thêm 5
            force += 5;
        }

        // Tính vị trí bắt đầu của viên đạn
        int[] initTraj = getInitTrajectory(pl, angle, force, 20);
        int x = initTraj[0], y = initTraj[1], vx = initTraj[2], vy = initTraj[3];

        int characterId = pl.getCharacterId();
        boolean isUsingItem = pl.getUsedItemId() > 0;

        for (int k = 0; k < numShoot; k++) {
            switch (bullId) {
                case 0 -> { // Gunner
                    if (isUsingItem || (characterId != 0 && characterId != 14)) {
                        return bullets;
                    }
                    bullets.add(new Bullet(bulletManager, (byte) 0, (pl.isUsePow() ? 630 : (numShoot == 2 ? 210 : 280)),
                            pl, x, y, vx, vy, 80, 100));
                }
                case 1 -> { // Aka
                    if (isUsingItem || characterId != 1) {
                        return bullets;
                    }
                    int damage = (numShoot == 2 ? 109 : 145);
                    int n = pl.isUsePow() ? 6 : 2;
                    for (int i = 0; i < n; i++) {
                        bullets.add(new Bullet(bulletManager, (byte) 1, damage, pl, x, y, vx, vy, 50, 50));
                    }
                }
                case 2 -> { // 3tia
                    if (isUsingItem || (characterId != 2 && characterId != 14)) {
                        return bullets;
                    }
                    int n = pl.isUsePow() ? 4 : 2;
                    int damage = numShoot == 2 ? 75 : 100;
                    for (int i = 0; i < n; i++) {
                        int arg = angle + i * 5;
                        int[] traj1 = getInitTrajectory(pl, arg, force, 20);

                        bullets.add(new Bullet(bulletManager, (byte) 2, damage, pl, traj1[0], traj1[1], traj1[2],
                                traj1[3], 80, 60));

                        if (i == 0) {
                            continue;
                        }

                        arg = angle - i * 5;
                        int[] traj2 = getInitTrajectory(pl, arg, force, 20);

                        bullets.add(new Bullet(bulletManager, (byte) 2, damage, pl, traj2[0], traj2[1], traj2[2],
                                traj2[3], 80, 60));
                    }
                }

                // Item bom huy diet
                case 4 -> {
                    if (pl.getUsedItemId() != 8) {
                        return bullets;
                    }
                    pl.getUser().updateMission(5, 1);
                    bullets.add(new ItemBomB52(bulletManager, 600, pl, x, y, vx, vy));
                }

                // Item bay
                case 5 -> {
                    if (pl.getUsedItemId() != 1) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemTeleport(bulletManager, 0, pl, x, y, vx, vy));
                }

                // Item bom pha dat
                case 6 -> {
                    if (pl.getUsedItemId() != 6) {
                        return bullets;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(bulletManager, (byte) 6, 200, pl, x, y, vx, vy, 70, 90));
                    }
                }

                // Item luu dan
                case 7 -> {
                    if (pl.getUsedItemId() != 7) {
                        return bullets;
                    }
                    bullets.add(new Bullet(bulletManager, (byte) 7, 500, pl, x, y, vx, vy, 70, 80));
                }

                // Item to nhen
                case 8 -> {
                    if (characterId == 13 || pl.getUsedItemId() == 9) {
                        bullets.add(new ItemToNhen(bulletManager, 300, pl, x, y, vx, vy));
                    }
                    if (characterId == 22) {
                        bullets.add(new SpiderDropSilk(bulletManager, 300, pl));
                    }
                }

                case 9 -> {// King kong
                    if (isUsingItem || characterId != 3) {
                        return bullets;
                    }
                    int arg2 = angle - 6;
                    for (int i = 0; i < 4; i++, arg2 += 4) {
                        int[] traj = getInitTrajectory(pl, arg2, force, 20);
                        x = traj[0];
                        y = traj[1];
                        vx = traj[2];
                        vy = traj[3];
                        bullets.add(new Bullet(bulletManager, (byte) 9,
                                pl.isUsePow() ? 210 : (numShoot == 2 ? 79 : 105), pl, x, y, vx, vy, 40, 90));
                    }
                }

                case 10 -> {// Rocket
                    if (isUsingItem || (characterId != 4 && characterId != 14)) {
                        return bullets;
                    }
                    for (int i = 0; i < 3; i++) {
                        bullets.add(new Bullet(bulletManager, (byte) 10,
                                pl.isUsePow() ? 240 : (numShoot == 2 ? 80 : 107), pl, x, y, vx, vy, 50, 80));
                    }
                }

                case 11 -> {// Granos
                    if (isUsingItem || characterId != 5) {
                        return bullets;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(bulletManager, (byte) 11,
                                pl.isUsePow() ? 140 : (numShoot == 2 ? 47 : 62), pl, x, y, vx, vy, 30, 90));
                    }
                }

                // Item dan voi rong
                case 13 -> {
                    if (pl.getUsedItemId() != 17) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemVoiRong(bulletManager, pl, x, y, vx, vy));
                }

                // Item dan laser
                case 14 -> {
                    if (characterId == 13 || pl.getUsedItemId() != 16) {
                        return bullets;
                    }
                    bullets.add(new ItemLaser(bulletManager, 500, pl, x, y, vx, vy));
                }

                // Item dan trai pha
                case 16 -> {
                    if (pl.getUsedItemId() != 11) {
                        return bullets;
                    }
                    bullets.add(new ItemTraiPha(bulletManager, 200, pl, x, y, vx, vy));
                }

                // Apache
                case 17 -> {
                    if (isUsingItem || characterId != 8) {
                        return bullets;
                    }
                    bullets.add(new ApaBullet(bulletManager, pl.isUsePow() ? 216 : (numShoot == 2 ? 81 : 108), pl, x, y,
                            vx, vy, angle, force, force2));
                }

                // Chicky
                case 19 -> {
                    if (isUsingItem || characterId != 6) {
                        return bullets;
                    }
                    bullets.add(new ChickyBullet(bulletManager, pl.isUsePow() ? 500 : (numShoot == 2 ? 169 : 225), pl,
                            x, y, vx, vy, force2));
                }

                // Tazan
                case 21 -> {
                    if (isUsingItem || characterId != 7) {
                        return bullets;
                    }
                    bullets.add(new TazranBullet(bulletManager, pl.isUsePow() ? 800 : (numShoot == 2 ? 225 : 340), pl,
                            x, y, vx, vy));
                }

                // Item chuot gan bom
                case 22 -> {
                    if (pl.getUsedItemId() != 18) {
                        return bullets;
                    }
                    bullets.add(new BombMouseBullet(bulletManager, 500, pl, x, y, force, angle < 89));
                }

                // Item Sao Bang
                case 23 -> {
                    if (pl.getUsedItemId() != 21) {
                        return bullets;
                    }
                    bullets.add(new ItemSaoBang(bulletManager, 200, pl, x, y, vx, vy));
                }

                // Item Dan xuyen dat
                case 25 -> {
                    if (pl.getUsedItemId() != 20) {
                        return bullets;
                    }
                    vy = (force * Utils.sin(-angle) >> 10);
                    bullets.add(new ItemXuyenDat(bulletManager, 500, pl, x, y, vx, vy, force));
                }

                // Item ten lua
                case 26 -> {
                    if (pl.getUsedItemId() != 19) {
                        return bullets;
                    }
                    bullets.add(new ItemTenLua(bulletManager, 200, pl, x, y, vx, vy, force));
                }

                // Item mua dan
                case 28 -> {
                    if (pl.getUsedItemId() != 22) {
                        return bullets;
                    }
                    int newVy = -force / 2;
                    bullets.add(new ItemMuaDan(bulletManager, 200, pl, x, y, 0, newVy));
                }

                // Item khoang dat
                case 30 -> {
                    if (pl.getUsedItemId() != 23) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemKhoangDat(bulletManager, pl, force));
                }

                // Big boom bum
                case 31 -> {
                    if (characterId != 12) {
                        return bullets;
                    }
                    bullets.add(new BigBoomBum(bulletManager, 1000, pl));
                }

                // Small boom bum
                case 32 -> {
                    if (characterId != 11) {
                        return bullets;
                    }
                    bullets.add(new SmallBoomBum(bulletManager, 100, pl));
                }

                // dan nhen
                case 33 -> {
                    if (characterId != 13) {
                        return bullets;
                    }
                    for (int i = 0; i < 5; i++) {
                        bullets.add(new Bullet(bulletManager, (byte) 33, 120, pl, x, y, vx, vy, 50, 80));
                    }
                }

                // small bom add
                case 34 -> {
                    if (characterId != 12) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new SmallBoomAdd(bulletManager, 0, pl, x, y, vx, vy));
                }

                // T-rex or Robot jump
                case 35 -> {
                    if (characterId != 15 && characterId != 14) {
                        return bullets;
                    }
                    bullets.add(new Jump(bulletManager, 1200, pl));
                }

                // Jump Fly
                case 36 -> {
                    if (characterId != 14 && characterId != 13) {
                        return bullets;
                    }
                    bullets.add(new JumpOrFly(bulletManager, 0, pl, x, y, vx, vy));
                }

                // T-rex Rocket
                case 37 -> {
                    if (characterId != 15) {
                        return bullets;
                    }
                    bullets.add(new BigRocKet(bulletManager, 570, pl));
                }

                // T-rex lazer
                case 40 -> {
                    if (characterId != 15) {
                        return bullets;
                    }
                    bullets.add(new Bullet(bulletManager, (byte) 40, 220, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                // T-rex white
                case 41 -> {
                    if (characterId != 15) {
                        return bullets;
                    }
                    bullets.add(new Bullet(bulletManager, (byte) 41, 200, pl, x - 20, y - 20, vx, vy, 10, 80));
                }

                // UFO Lazer
                case 42 -> {
                    if (characterId != 16) {
                        return bullets;
                    }
                    Bullet bullet = new Bullet(bulletManager, (byte) 42, 1000, pl, pl.getX(), pl.getY(), vx, vy, 10, 0);
                    bullet.setCanSuperType(false);
                    bullets.add(bullet);
                }

                // Balloon Gun Big
                case 43 -> {
                    if (characterId != 17) {
                        return bullets;
                    }
                    for (byte i = 0; i < 10; i++) {
                        if (i > 0) {
                            x = x + 105;
                        }
                        // if (x > this.fm.mapMNG.getWidth()) {
                        // x = 105 - (x - this.fm.mapMNG.getWidth());
                        // }
                        bullets.add(new Bullet(bulletManager, (byte) 43, 300, pl, x, (short) (y + 50), vx, vy, 0, 100));
                    }
                }

                // Balloon Gun
                case 44 -> {
                    if (characterId != 17) {
                        return bullets;
                    }
                    short vxrd = 0;
                    for (int i = 0; i < 15; i++) {
                        vxrd = (short) RandomUtil.nextInt(-10, 10);
                        bullets.add(
                                new Bullet(bulletManager, (byte) 44, 100, pl, x + 51, y + 40, vx + vxrd, vy, 40, 40));
                    }
                }

                // Balloon Lazer
                case 45 -> {
                    if (characterId != 17) {
                        return bullets;
                    }
                    bullets.add(new BalloonLazer(bulletManager, 500, pl, x + 65, y - 27));
                }

                case 47 -> {
                    if (characterId != 22) {
                        return bullets;
                    }
                    for (byte i = 0; i < 5; i++) {
                        int arg = angle + i * 5;
                        int[] traj = getInitTrajectory(pl, arg, force, 30);

                        bullets.add(new Bullet(bulletManager, (byte) 47, 400, pl, traj[0], traj[1], traj[2], traj[3], 0,
                                0));
                    }
                }
                // Magenta
                case 49 -> {
                    if (isUsingItem || characterId != 9) {
                        return bullets;
                    }
                    if (MAGENTA_NEW_BULLET) {
                        bullets.add(new MagentaBulletNew(bulletManager, (byte) 49,
                                pl.isUsePow() ? 1000 : (numShoot == 2 ? 308 : 400), pl, x, y, vx, vy, force));
                    } else {
                        vx = (1600 * Utils.cos(angle) >> 10);
                        vy = -(1600 * Utils.sin(angle) >> 10);
                        bullets.add(new MagentaBulletOld(bulletManager,
                                pl.isUsePow() ? 1000 : (numShoot == 2 ? 308 : 400), pl, x, y, vx, vy, force));
                    }
                }

                // Item tu sat
                case 50 -> {
                    if (pl.getUsedItemId() != 24) {
                        return bullets;
                    }
                    bullets.add(new SuicideItem(bulletManager, 1500, pl));
                }

                // Item bom mu
                case 51 -> {
                    if (pl.getUsedItemId() != 25) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemBomMu(bulletManager, pl, x, y, vx, vy));
                }

                // Item Khoang dat 2
                case 52 -> {
                    if (pl.getUsedItemId() != 26) {
                        return bullets;
                    }
                    bullets.add(new ItemKhoangDat2(bulletManager, 500, pl, x, y, vx, vy));
                }

                // Item Dong Bang
                case 54 -> {
                    if (pl.getUsedItemId() != 28) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemDongBang(bulletManager, pl, x, y, vx, vy));
                }

                // Item Khoi Doc
                case 55 -> {
                    if (pl.getUsedItemId() != 29) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemKhoiDoc(bulletManager, 150, pl, x, y, vx, vy));
                }

                // Item To nhen 2
                case 56 -> {
                    if (pl.getUsedItemId() != 30) {
                        return bullets;
                    }
                    int arg3 = angle - 5;
                    for (int i = 0; i < 3; i++, arg3 += 5) {
                        int[] traj = getInitTrajectory(pl, arg3, force, 20);
                        bullets.add(new ItemToNhen(bulletManager, 300, pl, traj[0], traj[1], traj[2], traj[3]));
                    }
                }

                // Item Bom hen gio
                case 57 -> {
                    if (pl.getUsedItemId() != 31) {
                        return bullets;
                    }
                    pl.setLucky(false);
                    bullets.add(new ItemBomHenGio(bulletManager, 600, pl, x, y, vx, vy));
                }
            }
        }
        return bullets;
    }
}
