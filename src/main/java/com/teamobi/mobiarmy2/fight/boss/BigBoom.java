package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

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
            fightManager.nextTurn();
            return;
        }

        //Lưu lại vị trí ban đầu
        int preX = x;
        int preY = y;

        //Di chuyển đến vị trí của người chơi
        updateXY(player.getX(), player.getY());

        //Nếu vị trí thay đổi thì gửi message cập nhật
        if (preX != x || preY != y) {
            fightManager.sendMessageUpdateXY(index);
        }

        fightManager.nextTurn();
    }

    public void bomAction() {
        System.out.println("boom action");
    }
}
