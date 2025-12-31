package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.util.Utils;

public class UFO extends Boss {
    private boolean turnShoot;

    public UFO(FightManager fightManager, short x, short y, short maxHp) {
        super(fightManager, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 4);
        super.isFlying = true;
        turnShoot = false;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public boolean shouldCollideWith(Bullet bullet) {
        return bullet.getBullId() != 42;
    }

    @Override
    public void turnAction() {
        short ys = y, xs = x;
        FightMapManager mapManager = fightManager.getFightMapManager();
        while (turnShoot && ys < mapManager.getHeight() + 200 && !mapManager.isCollision(xs, ys)) {
            if (ys > mapManager.getHeight()) {
                turnShoot = false;
            }
            ys++;
        }

        if (turnShoot) {
            turnShoot = false;
            fightManager.createShoot(this, (byte) 42, (short) 270, (byte) 20, (byte) 0, (byte) 1);
        } else {
            turnShoot = true;

            Player player = fightManager.getRandomPlayer(null);
            if (player != null) {
                x = player.getX();
                y = (short) (player.getY() - Utils.nextInt(150, 500));
                fightManager.sendPlayerFlyPosition(index);
            }

            fightManager.doNextTurn();
        }
    }
}
