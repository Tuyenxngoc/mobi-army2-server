package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class Ghost extends Boss {

    public Ghost(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 25, "Ghost", x, y, (short) 35, (short) 31, maxHp, 5);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        Player player = fightManager.getRandomPlayer(null);
        if (player == null) {
            skipTurn();
            return;
        }

        // Di chuyển lại người chơi
        if (x > player.getX()) {
            x = (short) (player.getX() + 30);
        } else {
            x = (short) (player.getX() - 30);
        }
        y = (short) (player.getY() - 15);

        // Gửi cập nhật vị trí
        fightManager.sendPlayerFlyPosition(index);

        // Tấn công
        fightManager.sendGhostAttackInfo(index, player.getIndex());

        // Cập nhật vị trí ngẫu nhiên
        short[] position = fightManager.getMapManger().getRandomPosition((short) 100, (short) 100, (short) 50, (short) 200);
        x = position[0];
        y = position[1];

        // Gửi cập nhật vị trí
        fightManager.sendPlayerFlyPosition(index);

        // Trừ máu người chơi
        player.updateHP((short) -Utils.nextInt(300, 600));

        // Tiếp tục chơi
        if (!fightManager.checkWin()) {
            fightManager.nextTurn();
        }
    }
}
