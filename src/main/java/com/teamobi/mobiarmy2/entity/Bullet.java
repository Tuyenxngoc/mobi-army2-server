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
    protected byte bullId;
    protected int damage;

    protected List<Point> trajectory = new ArrayList<>();//Quỹ đạo bay của đạn từ súng đến mục tiêu

    protected byte dXLaser;//Vector hướng X của tia laser
    protected byte dYLaser;//Vector hướng Y của tia laser

    protected List<Point> hitPoints = new ArrayList<>();//Các điểm va chạm/nổ

    protected short x;
    protected short y;
    protected short vx;
    protected short vy;

    protected short ax100;
    protected short ay100;
    protected short g100;

    protected short vxTemp;
    protected short vyTemp;
    protected short vyTemp2;

    protected boolean isMaxY;//Bullet khi đạt đỉnh Y và bắt đầu rơi xuống vào lượt sau
    protected short peakY;//Giá trị cực đại của Y

    protected short frame;
    protected boolean canPassThroughPlayers;
    protected boolean canPassThroughMap;
    protected boolean canCollide = true;// Đạn có thể va chạm
    protected boolean isCollected;// Đạn đã bị thu hồi (va chạm hoặc bay ra ngoài map)

    public Bullet() {
    }

    public Bullet(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        this.bulletManager = bulletManager;
        this.bullId = bullId;
        this.damage = (damage * player.getDamage()) / 100;
        this.player = player;
        this.x = (short) x;
        this.y = (short) y;
        this.vx = (short) vx;
        this.vy = (short) vy;
        FightManager fightManager = bulletManager.getFightManager();
        this.ax100 = (short) (fightManager.getWindX() * msg / 100);
        this.ay100 = (short) (fightManager.getWindY() * msg / 100);
        this.g100 = (short) g100;
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

    public void update() {
        FightManager fightManager = bulletManager.getFightManager();
        FightMapManager mapManager = fightManager.getMapManger();

        frame++;
        trajectory.add(new Point(x, y));

        // Kiểm tra đạn bay ra ngoài map
        if ((x < -200) || (x > mapManager.getWidth() + 200) || (y > mapManager.getHeight() + 200)) {
            isCollected = true;
            return;
        }

        // Lưu vị trí cũ để kiểm tra va chạm
        short preX = x;
        short preY = y;

        // Di chuyển đạn theo vận tốc
        x += vx;
        y += vy;

        // Kiểm tra va chạm với map/player
        short[] collisionResult = bulletManager.getCollisionPoint(preX, preY, x, y, canPassThroughPlayers, canPassThroughMap);
        if (collisionResult != null) {
            isCollected = true;

            x = collisionResult[0];
            y = collisionResult[1];
            trajectory.add(new Point(x, y));

            int type = collisionResult[2]; // Loại va chạm
            if (type == 1) { // Va chạm với player tính super shot type
                calculateSuperType();
            }

            if (canCollide) {
                bulletManager.handleCollision(x, y, this);
            }

            return;
        }

        // Cập nhật gia tốc
        updateAcceleration();

        // Xác định điểm cao nhất (đỉnh quỹ đạo)
        if (vy > 0 && !isMaxY) {
            isMaxY = true;
            peakY = y;
        }

        // Áp dụng hiệu ứng Vòi Rồng
        applyGravityFieldEffect();
    }

    private void applyGravityFieldEffect() {

    }

    private void calculateSuperType() {
        // Nếu dùng item hoặc dùng power thì không tính
        if (player.getUsedItemId() != -1 || player.isUsePow()) {
            return;
        }

        // Nếu đã có super type thì không tính nữa
        if (bulletManager.getSuperType() != 0) {
            return;
        }

        if (isMaxY) {// Siêu cao
            int dropHeight = y - peakY;

            if (dropHeight > 350 && dropHeight < 450) {// Super type 1: Rơi từ độ cao 350-450
                bulletManager.setSuperType((byte) 1);
            } else if (dropHeight >= 450) {// Super type 2: Rơi từ độ cao >= 450
                bulletManager.setSuperType((byte) 2);
            }
        }

        short gunId = player.getGunId();
        if ((gunId == 2 || gunId == 3) && !trajectory.isEmpty()) { // Siêu xa
            int distance = Math.abs(x - trajectory.getFirst().getX());

            if (distance > 375) {
                bulletManager.setSuperType((byte) 4);
            }
        }

        if (bulletManager.getSuperType() != 0) {
            // Tọa độ đầu tiên đạt đỉnh nếu có nhiều đỉnh Y
            Point peakPoint = null;

            for (Point point : trajectory) {
                if (point.getY() == peakY) {
                    peakPoint = point;
                    break;
                }
            }

            if (peakPoint != null) {
                bulletManager.setSuperX((short) peakPoint.getX());
                bulletManager.setSuperY((short) peakPoint.getY());
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
