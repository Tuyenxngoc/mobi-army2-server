package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class RobotSpider extends Boss {

    public RobotSpider(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 13, "Robot Spider", x, y, (short) 42, (short) 42, maxHp, 4);
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            skipTurn();
            return;
        }

        int distance = calculateDistance(player.getX(), player.getY());
        if (distance < 30) {
            this.usedItemId = 9;
            fightManager.newShoot(index, (byte) 8, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 30, (byte) 0, (byte) 1, false);
            byte force = (byte) Utils.nextInt(15, 30);
            short arg = (short) Utils.nextInt(80, 100);
            fightManager.newShoot(index, (byte) 36, arg, force, (byte) 0, (byte) 1, true);
            return;
        }

        switch (Utils.nextInt(3)) {
            case 0 -> {// Tơ nhện
                usedItemId = 9;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 70, 70
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 8, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, false);
                byte force = (byte) Utils.nextInt(15, 30);
                short arg = (short) Utils.nextInt(80, 100);
                fightManager.newShoot(index, (byte) 36, arg, force, (byte) 0, (byte) 1, true);
            }
            case 1 -> {// Laser
                usedItemId = 16;
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, false, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 10, 50
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 14, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
            case 2 -> {// Rocket
                short[] forceArgXY = fightManager.getForceArgXY(
                        characterId, true, x, y, player.getX(),
                        (short) (player.getY() - (player.getHeight() / 2)),
                        (short) (player.getWidth() / 2), player.getHeight(),
                        50, 5, 50, 80
                );
                if (forceArgXY == null) {
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                    return;
                }
                fightManager.newShoot(index, (byte) 33, forceArgXY[0], (byte) forceArgXY[1], (byte) 0, (byte) 1, true);
            }
        }
    }
}
