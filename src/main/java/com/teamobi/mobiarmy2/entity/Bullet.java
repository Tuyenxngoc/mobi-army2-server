package com.teamobi.mobiarmy2.entity;

import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.FightMapManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Bullet {
    protected BulletManager bulletManager;
    protected Player player;
    protected boolean isCollected;
    protected byte bullId;
    protected int damage;

    protected short X;
    protected short Y;
    protected short lastX;
    protected short lastY;
    protected short vx;
    protected short vy;

    protected short ax100;
    protected short ay100;
    protected short g100;

    protected short vxTemp;
    protected short vyTemp;
    protected short vyTemp2;

    protected boolean isMaxY;
    protected short peakX;
    protected short peakY;

    protected short frame;
    protected byte superType;
    protected boolean isXuyenPlayer;
    protected boolean isXuyenMap;
    protected boolean isCanCollision;
    protected List<Short> XArray;
    protected List<Short> YArray;

    public Bullet(BulletManager bulletManager, byte bullId, int damage, Player player, int X, int Y, int vx, int vy, int msg, int g100) {
        this.bulletManager = bulletManager;
        this.bullId = bullId;
        this.damage = (damage * player.getDamage()) / 100;
        this.player = player;
        this.X = (short) X;
        this.Y = (short) Y;
        this.lastX = (short) X;
        this.lastY = (short) Y;
        this.vx = (short) vx;
        this.vy = (short) vy;
        FightManager fightManager = bulletManager.getFightManager();
        this.ax100 = (short) (fightManager.getWindX() * msg / 100);
        this.ay100 = (short) (fightManager.getWindY() * msg / 100);
        this.g100 = (short) g100;
        this.vxTemp = 0;
        this.vyTemp = 0;
        this.vyTemp2 = 0;
        this.isCollected = false;
        this.isMaxY = false;
        this.peakX = -1;
        this.peakY = -1;
        this.frame = 0;
        this.superType = 0;
        this.XArray = new ArrayList<>();
        this.YArray = new ArrayList<>();
        this.isXuyenPlayer = false;
        this.isXuyenMap = false;
        this.isCanCollision = true;
    }

    public static int getImpactRadiusByBullId(int bullId) {
        return switch (bullId) {
            case 0 -> 21;
            case 1, 11, 17, 18, 19, 21, 26 -> 13;
            case 2, 9, 20, 24, 48, 49 -> 18;
            case 3 -> 100;
            case 6, 8 -> 22;
            case 7, 14, 22, 40, 41, 50, 51, 54, 55 -> 30;
            case 10, 16, 23 -> 19;
            case 12, 29, 52 -> 20;
            case 15, 45 -> 28;
            case 25 -> 8;
            case 27, 44 -> 11;
            case 30, 59 -> 16;
            case 31 -> 40;
            case 32, 35 -> 50;
            case 33 -> 25;
            case 37 -> 150;
            case 42, 43 -> 32;
            case 47 -> 7;
            case 57 -> 70;
            default -> 0;
        };
    }

    public void nextXY() {
        FightManager fightManager = bulletManager.getFightManager();
        FightMapManager mapManager = fightManager.getMapManger();

        frame++;

        this.XArray.add(X);
        this.YArray.add(Y);

        // Kiểm tra đạn bay ra ngoài map
        if ((X < -200) || (X > mapManager.getWidth() + 200) || (Y > mapManager.getHeight() + 200)) {
            isCollected = true;
            return;
        }

        // Lưu vị trí cũ để kiểm tra va chạm
        short preX = X;
        short preY = Y;

        // Di chuyển đạn theo vận tốc
        X += vx;
        Y += vy;

        lastX = X;
        lastY = Y;

        // Kiểm tra va chạm với map/player
        short[] collisionPoint = bulletManager.getCollisionPoint(preX, preY, X, Y, isXuyenPlayer, isXuyenMap);
        if (collisionPoint != null) {
            isCollected = true;

            X = collisionPoint[0];
            Y = collisionPoint[1];
            XArray.add(X);
            YArray.add(Y);

            // Tính super shot type
            calculateSuperType();

            if (this.isCanCollision) {
                mapManager.collision(X, Y, this);
            }

            return;
        }

        // Cập nhật gia tốc
        updateAcceleration();

        // Xác định điểm cao nhất (đỉnh quỹ đạo)
        if (vy > 0 && !isMaxY) {
            isMaxY = true;
            peakX = X;
            peakY = Y;
        }

        // Áp dụng hiệu ứng Vòi Rồng
        applyVoiRongEffect();
    }

    private void applyVoiRongEffect() {
        if (!this.bulletManager.isHasVoiRong()) {
            return;
        }

        for (BulletManager.VoiRong vr : this.bulletManager.getVoiRongs()) {
            if (this.X >= vr.X - 5 && this.X <= vr.X + 10) {
                this.vx -= 2;
                this.vy -= 2;
                break;
            }
        }
    }

    private void calculateSuperType() {
        // Nếu dùng item hoặc dùng power thì không tính
        if (player.getUsedItemId() != -1 || player.isUsePow()) {
            return;
        }

        if (isMaxY) {// Siêu cao
            int dropHeight = Y - peakY;

            if (dropHeight > 350 && dropHeight < 450) {// Super type 1: Rơi từ độ cao 350-450
                superType = 1;
            } else if (dropHeight >= 450) {// Super type 2: Rơi từ độ cao >= 450
                superType = 2;
            }
        }

        short gunId = player.getGunId();
        if ((gunId == 2 || gunId == 3) && !XArray.isEmpty()) { // Siêu xa
            int distance = Math.abs(X - XArray.getFirst());

            if (distance > 375) {
                superType = 4;
            }
        }
    }

    private void updateAcceleration() {
        // Tích lũy gia tốc
        vxTemp += Math.abs(ax100);
        vyTemp += Math.abs(ay100);
        vyTemp2 += g100;

        // Cập nhật vận tốc X
        if (Math.abs(vxTemp) >= 100) {
            if (ax100 > 0) {
                vx += vxTemp / 100;
            } else {
                vx -= vxTemp / 100;
            }
            vxTemp %= 100;
        }

        // Cập nhật vận tốc Y
        if (Math.abs(vyTemp) >= 100) {
            if (ay100 > 0) {
                vy += vyTemp / 100;
            } else {
                vy -= vyTemp / 100;
            }
            vyTemp %= 100;
        }

        if (Math.abs(vyTemp2) >= 100) {
            vy += vyTemp2 / 100;
            vyTemp2 %= 100;
        }
    }
}
