package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class Player {

    private final IFightManager fightManager;
    private final User user;
    private byte index;
    private byte pixel;
    private byte angry;
    private short x;
    private short y;
    private short maxHp;
    private short hp;
    private byte[] items;
    private boolean isUpdateHP;
    private boolean isUpdateAngry;
    private boolean isLucky;
    private boolean isPoisoned;
    private byte eyeSmokeCount;
    private byte freezeCount;
    private byte windStopCount;

    public Player(IFightManager fightManager, User user, byte index, short x, short y, byte[] items) {
        this.fightManager = fightManager;
        this.user = user;
        this.index = index;
        this.x = x;
        this.y = y;
        this.items = items;

        short[] ability = user.getAbility();

        this.maxHp = ability[0];
    }

    public void die() {
        this.hp = 0;
    }

    public void nextLuck() {
        isLucky = Math.random() < 0.5;
    }

    public void decreaseWindStopCount() {
        if (windStopCount > 0) {
            windStopCount--;
        }
    }

}
