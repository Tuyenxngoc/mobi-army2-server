package com.teamobi.mobiarmy2.entity;

import com.teamobi.mobiarmy2.fight.BulletManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Bullet {
    private List<Point> trajectory;//Quỹ đạo bay của đạn từ súng đến mục tiêu

    private byte dXLaser;//Vector hướng X của tia laser
    private byte dYLaser;//Vector hướng Y của tia laser

    private List<Point> hitPoints;//Các điểm va chạm/nổ

    private short frame;// Số frame đã bay
    private int x;// Tọa độ X hiện tại của đạn
    private int y;// Tọa độ Y hiện tại của đạn
    private int vx;// Vận tốc theo trục X
    private int vy;// Vận tốc theo trục Y

    private int ax100; // Gia tốc theo trục X
    private int ay100; // Gia tốc theo trục Y
    private int g100;// Lực trọng lực

    private int vxTemp;// Tích lũy gia tốc X (gió ngang)
    private int vyTemp;// Tích lũy gia tốc Y từ gió
    private int vyTemp2;// Tích lũy gia tốc Y từ trọng lực
    private int peakX;// Tọa độ X tại đỉnh
    private int peakY;// Tọa độ Y tại đỉnh
    private boolean reachedPeak;// Đã đạt đỉnh chưa
    private byte superType;// 0: thường, 1: cháy nhẹ, 2: cháy mạnh, 4: bay xa
    private boolean isCollected;// Đạn đã va chạm/thu hồi

    private boolean penetratePlayer;// Đạn có xuyên qua người chơi không
    private boolean penetrateMap; // Đạn có xuyên qua bản đồ không

    private BulletManager bulletManager;

    public Bullet(BulletManager bulletManager, int x, int y, int vx, int vy, int msg, int g100) {
        this.bulletManager = bulletManager;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ax100 = bulletManager.getFightManager().getWindX() * msg / 100;
        this.ay100 = bulletManager.getFightManager().getWindY() * msg / 100;
        this.g100 = g100;

        this.penetratePlayer = false;
        this.penetrateMap = false;

        this.isCollected = false;
        this.superType = 0;
        this.reachedPeak = false;
        this.frame = 0;
    }

    public boolean update(int mapWidth, int mapHeight) {
        if (isCollected) {
            return false;
        }

        frame++;
        trajectory.add(new Point(x, y));

        // Kiểm tra đạn bay ra ngoài map
        if (x < -200 || x > mapWidth + 200 || y > mapHeight + 200) {
            isCollected = true;
            return false;
        }

        // Lưu vị trí cũ để kiểm tra va chạm
        int preX = x;
        int preY = y;

        // Di chuyển đạn theo vận tốc
        x += vx;
        y += vy;

        // Kiểm tra va chạm với map/player
        int[] collisionPoint = bulletManager.getCollisionPoint(preX, preY, x, y, penetratePlayer, penetrateMap);
        if (collisionPoint != null) {
            isCollected = true;

            x = collisionPoint[0];
            y = collisionPoint[1];
            trajectory.add(new Point(x, y));

            // Tính super shot type
            calculateSuperType();

            // Xử lý va chạm
            bulletManager.handleCollision(this);

            return false;
        }

        // Cập nhật gia tốc
        updateAcceleration();

        // Xác định điểm cao nhất (đỉnh quỹ đạo)
        if (vy > 0 && !reachedPeak) {
            reachedPeak = true;
            peakX = preX;
            peakY = preY;
        }

        //TODO Xử lý hiệu ứng lốc xoáy (Vòi rồng)

        return true;
    }

    private void calculateSuperType() {
        if (reachedPeak) {// Siêu cao
            int dropHeight = y - peakY;

            // Super type 1: Rơi từ độ cao 350-450
            if (dropHeight > 350 && dropHeight < 450) {
                superType = 1;
            }
            // Super type 2: Rơi từ độ cao >= 450
            else if (dropHeight >= 450) {
                superType = 2;
            }
        }

        int bulletId = bulletManager.getBulletId();
        if ((bulletId == 2 || bulletId == 3) && !trajectory.isEmpty()) { // Siêu xa
            int distance = Math.abs(x - trajectory.getFirst().getX());
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
