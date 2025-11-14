package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulletManager {
    private final FightManager fightManager;

    private byte bulletId;
    private byte typeShoot;

    private List<Bullet> bullets;

    private byte typeSuper;//Loại đạn siêu cao
    private short xSuper;//Tọa độ X của đạn siêu cao
    private short ySuper;//Tọa độ Y của đạn siêu cao

    public BulletManager(FightManager fightManager) {
        this.fightManager = fightManager;
    }

    public void addShoot() {
    }

    public void drawBullet() {
        FightMapManager map = fightManager.getMapManger();
        short width = map.getWidth();
        short height = map.getHeight();

        boolean allDone;
        do {
            allDone = true;
            for (Bullet bullet : bullets) {
                boolean canContinue = bullet.update(width, height);

                // Nếu còn viên đạn nào vẫn vẽ được (return true) thì chưa done
                if (canContinue) {
                    allDone = false;
                }
            }
        } while (!allDone);
    }

    public void clearAllBullets() {
        bullets.clear();
        typeSuper = 0;
    }

    public void handleCollision(Bullet bullet) {
        fightManager.getMapManger().handleCollision(bullet);
        fightManager.handleBulletCollisionWithPlayers(bullet);
    }

    /**
     * Calculates the collision point along a line from (x1, y1) to (x2, y2).
     * Checks for collisions with the map and players.
     *
     * @param x1                    Start X coordinate
     * @param y1                    Start Y coordinate
     * @param x2                    End X coordinate
     * @param y2                    End Y coordinate
     * @param canPassThroughPlayers If true, ignores player collisions
     * @param canPassThroughMap     If true, ignores map collisions
     * @return int[] {x, y} of collision point, or null if no collision
     */
    public int[] getCollisionPoint(int x1, int y1, int x2, int y2, boolean canPassThroughPlayers, boolean canPassThroughMap) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        // Xác định hướng di chuyển theo X và Y
        int stepX = 0;
        int stepY = 0;
        int primaryStepX = 0;
        int primaryStepY = 0;

        if (deltaX < 0) { // Di chuyển sang trái
            stepX = primaryStepX = -1;
        } else if (deltaX > 0) { // Di chuyển sang phải
            stepX = primaryStepX = 1;
        }

        if (deltaY < 0) {// Di chuyển lên trên
            stepY = primaryStepY = -1;
        } else if (deltaY > 0) { // Di chuyển xuống dưới
            stepY = primaryStepY = 1;
        }

        int primaryDist = Math.abs(deltaX);
        int secondaryDist = Math.abs(deltaY);
        if (primaryDist > secondaryDist) {
            primaryStepY = 0;  // X là trục chính → chỉ di chuyển theo X
        } else {
            primaryDist = Math.abs(deltaY);
            secondaryDist = Math.abs(deltaX);
            primaryStepX = 0; // Y là trục chính → chỉ di chuyển theo Y
        }
        int error = primaryDist >> 1;
        int currentX = x1;
        int currentY = y1;

        for (int i = 0; i <= primaryDist; i++) {
            // Check map collision
            if (!canPassThroughMap) {
                if (fightManager.getMapManger().isCollision(currentX, currentY)) {
                    return new int[]{currentX, currentY};
                }
            }

            // Check player collision
            if (!canPassThroughPlayers) {
                for (int j = 0; j < fightManager.getTotalPlayers(); j++) {
                    Player pl = fightManager.getPlayers()[j];
                    if (pl != null && pl.isCollision(currentX, currentY)) {
                        return new int[]{currentX, currentY};
                    }
                }
            }

            error += secondaryDist;
            if (error >= primaryDist) {
                error -= primaryDist;
                currentX += stepX;
                currentY += stepY;
            } else {
                currentX += primaryStepX;
                currentY += primaryStepY;
            }
        }
        return null;
    }
}
