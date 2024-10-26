package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

/**
 * @author tuyen
 */
public class Balloon extends Boss {

    private final byte[] bodyParts;
    private int currentTurn;

    public Balloon(IFightManager fightManager, byte index, short x, short y) {
        super(fightManager, index, (byte) 17, "Balloon", x, y, (short) 0, (short) 0, (short) 0, 1);
        super.isFlying = true;
        bodyParts = new byte[]{index, (byte) (index + 1), (byte) (index + 2), (byte) (index + 3), -1};
        currentTurn = -1;
    }

    @Override
    public void turnAction() {
        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            if (!fightManager.checkWin()) {
                fightManager.nextTurn();
            }
            return;
        }

        boolean canAttack = false;
        for (int i = 0; i < bodyParts.length; i++) {
            if (bodyParts[i] == -1) {
                continue;
            }

            Player boss = fightManager.getPlayers()[bodyParts[i]];
            if (boss != null && !boss.isDead() && (i == 1 || i == 2 || i == 4)) {
                canAttack = true;
                break;
            }
        }
    }
}
