package com.teamobi.mobiarmy2.fight;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public abstract class Boss extends Player {

    private String name;

    public Boss(IFightManager fightManager, byte index, byte characterId, String name, short x, short y, short width, short height, short maxHp, int xpExist) {
        super(fightManager, index, characterId, x, y, width, height, maxHp, xpExist);
        this.name = name;
        this.stamina = 100;
        this.damage = 100;
    }

    public abstract void turnAction();

    protected void moveToTarget(Player player) {
        //Lưu lại vị trí ban đầu
        int preX = x;
        int preY = y;

        //Di chuyển đến vị trí của người chơi
        updateXY(player.getX(), player.getY());

        //Nếu vị trí thay đổi thì gửi message cập nhật
        if (preX != x || preY != y) {
            fightManager.sendMessageUpdateXY(index);
        }
    }

    protected int calculateDistance(short x, short y) {
        int deltaX = Math.abs(this.x - x);
        int deltaY = Math.abs(this.y - y);
        return (int) Math.abs(Math.hypot(deltaX, deltaY));
    }

}
