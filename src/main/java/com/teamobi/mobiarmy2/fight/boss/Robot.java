package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.RandomUtil;

public class Robot extends Boss {
    public Robot(FightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 14, "Robot", x, y, (short) 24, (short) 25, maxHp, 4);
    }

    @Override
    public void turnAction() {
        Player closestPlayer = fightManager.findClosestPlayer(x, y);
        if (closestPlayer == null) {
            fightManager.doNextTurn();
            return;
        }

        if (Math.abs(x - closestPlayer.getX()) <= 40 && Math.abs(y - closestPlayer.getY()) <= 40) {
            fightManager.createShoot(this, (byte) 35, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
            byte force = (byte) RandomUtil.nextInt(15, 30);
            short arg = (short) RandomUtil.nextInt(80, 100);
            fightManager.createShoot(this, (byte) 36, arg, force, (byte) 0, (byte) 1);
            return;
        }

        //Lấy random người chơi
        Player randomPlayer = fightManager.getRandomPlayer(null);
        if (randomPlayer == null) {
            randomPlayer = closestPlayer;
        }

        switch (RandomUtil.nextInt(9)) {
            case 0 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 80, 100
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 0, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            case 1 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 80, 60
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 2, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            case 2 -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 50, 80
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 10, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            case 3 -> {
                usedItemId = 6;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 70, 90
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 6, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            case 4 -> {
                usedItemId = 7;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        (short) (randomPlayer.getWidth() / 2), (short) (randomPlayer.getHeight() / 2), 50, 5, 70, 80
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 7, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            default -> {
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, randomPlayer.getX(), randomPlayer.getY(),
                        randomPlayer.getWidth(), randomPlayer.getHeight(), 50, 5, 0, 80
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 36, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
        }
    }
}