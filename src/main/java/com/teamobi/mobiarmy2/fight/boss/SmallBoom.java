package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

public class SmallBoom extends Boss {
    public SmallBoom(IFightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 11, "Small Boom", x, y, (short) 18, (short) 18, maxHp, 2);
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            fightManager.doNextTurn();
            return;
        }

        moveToTarget(player);
        int distance = calculateDistance(player.getX(), player.getY());
        if (distance < 25) {
            fightManager.createShoot(this, (byte) 32, (short) 0, (byte) 0, (byte) 0, (byte) 1);
            return;
        }
        fightManager.doNextTurn();
    }
}
