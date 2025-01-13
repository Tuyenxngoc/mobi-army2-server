package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class Robot extends Boss {
    public Robot(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 14, "Robot", x, y, (short) 24, (short) 25, maxHp, 4);
    }

    @Override
    public void turnAction() {
        Player closestPlayer = fightManager.findClosestPlayer(x, y);
        if (closestPlayer == null) {
            skipTurn();
            return;
        }

        if (Math.abs(x - closestPlayer.getX()) <= 40 && Math.abs(y - closestPlayer.getY()) <= 40) {
            fightManager.newShoot(index, (byte) 35, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
            byte force = (byte) Utils.nextInt(15, 30);
            short arg = (short) Utils.nextInt(80, 100);
            fightManager.newShoot(index, (byte) 36, arg, force, (byte) 0, (byte) 1, true);
            return;
        }

        //Lấy random người chơi
        Player randomPlayer = fightManager.getRandomPlayer(null);
        if (randomPlayer == null) {
            randomPlayer = closestPlayer;
        }

        switch (Utils.nextInt(9)) {
            case 0 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 80, 100
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 0, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            case 1 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 80, 60
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 2, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            case 2 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 50, 80
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 10, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            case 3 -> {
                usedItemId = 6;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 70, 90
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 6, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            case 4 -> {
                usedItemId = 7;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 70, 80
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 7, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            default -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        randomPlayer.getWidth(), randomPlayer.getHeight(), 50, 5, 0, 80
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 36, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
        }
    }
}