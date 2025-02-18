package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Player;

/**
 * @author tuyen
 */
public class SmallBoom extends Boss {

    public SmallBoom(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 11, "Small Boom", x, y, (short) 18, (short) 18, maxHp, 2);
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            skipTurn();
            return;
        }

        moveToTarget(player);
        int distance = calculateDistance(player.getX(), player.getY());
        if (distance < 25) {
            fightManager.newShoot(index, (byte) 32, (short) 0, (byte) 0, (byte) 0, (byte) 1, true);
            return;
        }
        if (!fightManager.checkWin()) {
            fightManager.nextTurn();
        }
    }

}
