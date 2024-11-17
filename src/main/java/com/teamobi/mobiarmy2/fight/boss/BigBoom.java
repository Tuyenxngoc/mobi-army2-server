package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class BigBoom extends Boss {

    public BigBoom(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 12, "Big boom", x, y, (short) 28, (short) 28, maxHp, 4);
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            skipTurn();
            return;
        }

        if (Utils.nextInt(100) < 45) {
            short angle;
            if (player.getX() > x) {
                angle = (short) Utils.nextInt(70, 75);
            } else {
                angle = (short) Utils.nextInt(110, 115);
            }
            byte force = (byte) (Math.abs(x - player.getX()) / 20);
            if (force < 8) {
                force = 8;
            }
            if (force > 30) {
                force = 30;
            }
            fightManager.newShoot(index, (byte) 34, angle, force, (byte) 0, (byte) 1, true);
        } else {
            moveToTarget(player);
            int distance = calculateDistance(player.getX(), player.getY());
            if (distance < 35) {
                fightManager.newShoot(index, (byte) 31, (short) 0, (byte) 0, (byte) 0, (byte) 1, true);
                return;
            }
            skipTurn();
        }
    }
}
