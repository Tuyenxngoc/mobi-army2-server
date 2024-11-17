package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.IMapManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class UFO extends Boss {

    private boolean turnShoot;

    public UFO(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 4);
        super.isFlying = true;
        turnShoot = false;
    }

    @Override
    public void turnAction() {
        short ys = y, xs = x;
        IMapManager mapManager = fightManager.getMapManger();
        while (turnShoot && ys < mapManager.getHeight() + 200 && !mapManager.isCollision(xs, ys)) {
            if (ys > mapManager.getHeight()) {
                turnShoot = false;
            }
            ys++;
        }

        if (turnShoot) {
            turnShoot = false;
            fightManager.newShoot(index, (byte) 42, (short) 270, (byte) 20, (byte) 0, (byte) 1, true);
        } else {
            turnShoot = true;

            Player player = fightManager.getRandomPlayer(null);
            if (player != null) {
                x = player.getX();
                y = (short) (player.getY() - Utils.nextInt(150, 500));
                fightManager.sendPlayerFlyPosition(index);
            }

            skipTurn();
        }
    }
}
