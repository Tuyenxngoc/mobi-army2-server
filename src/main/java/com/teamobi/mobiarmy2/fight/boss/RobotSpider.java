package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.RandomUtil;
import com.teamobi.mobiarmy2.util.Utils;

public class RobotSpider extends Boss {
    public RobotSpider(FightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 13, "Robot Spider", x, y, (short) 42, (short) 42, maxHp, 4);
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            fightManager.doNextTurn();
            return;
        }

        int distance = calculateDistance(player.getX(), player.getY());
        if (distance < 30) {
            fightManager.createShoot(this, (byte) 8, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 30, (byte) 0, (byte) 1, false);
            byte force = (byte) RandomUtil.nextInt(15, 30);
            short arg = (short) RandomUtil.nextInt(80, 100);
            fightManager.createShoot(this, (byte) 36, arg, force, (byte) 0, (byte) 1);
            return;
        }

        switch (RandomUtil.nextInt(3)) {
            case 0 -> {// Tơ nhện
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 70, 70
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 8, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, false);
                byte force = (byte) RandomUtil.nextInt(15, 30);
                short arg = (short) RandomUtil.nextInt(80, 100);
                fightManager.createShoot(this, (byte) 36, arg, force, (byte) 0, (byte) 1);
            }
            case 1 -> {// Laser
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 10, 50
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 14, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
            case 2 -> {// Rocket
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 50, 80
                );
                if (forceArgXY == null) {
                    fightManager.doNextTurn();
                    return;
                }
                fightManager.createShoot(this, (byte) 33, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1);
            }
        }
    }
}
