package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class TRex extends Boss {

    public TRex(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 15, "T. rex", x, y, (short) 45, (short) 50, maxHp, 8);
    }

    @Override
    public void turnAction() {
        Player closestPlayer = fightManager.findClosestPlayer(x, y);
        if (closestPlayer == null) {
            if (!fightManager.checkWin()) {
                fightManager.nextTurn();
            }
            return;
        }

        // Nhảy nếu người chơi ở gần khu vực
        if (Math.abs(x - closestPlayer.getX()) <= 90 && Math.abs(y - closestPlayer.getY()) <= 250) {
            fightManager.newShoot(index, (byte) 35, (short) 0, (byte) 0, (byte) 0, (byte) 1, true);
            return;
        }

        // Lấy random người chơi
        Player randomPlayer = fightManager.getRandomPlayer(null);
        if (randomPlayer == null) {
            randomPlayer = closestPlayer;
        }

        switch (Utils.nextInt(3)) {
            case 0 -> // T. rex rocket
                    fightManager.newShoot(index, (byte) 37, (short) 110, (byte) 30, (byte) 0, (byte) 1, true);

            case 1 -> { // T. rex laser
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) 70, (short) 70, 110, 5, 10, 80);
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 40, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }

            default -> { // T. rex white
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) 70, (short) 70, 110, 5, 10, 80);
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 41, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
        }
    }
}