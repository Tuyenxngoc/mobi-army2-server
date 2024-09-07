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
    private byte characterId;
    private byte index;
    private byte pixel;
    private byte angry;
    private byte stamina;
    private short x;
    private short y;
    private short maxHp;
    private short hp;
    private short damage;
    private short defense;
    private short luck;
    private short teamPoints;
    private byte[] items;
    private boolean isUpdateHP;
    private boolean isUpdateAngry;
    private boolean isLucky;
    private boolean isPoisoned;
    private byte eyeSmokeCount;
    private byte freezeCount;
    private byte windStopCount;
    private boolean[] clanItems;

    public Player(int index, int x, int y, int hp, int maxHp) {
        this.fightManager = null;
        this.user = null;
        this.index = (byte) index;
        this.x = (short) x;
        this.y = (short) y;
        this.hp = (short) hp;
        this.maxHp = (short) maxHp;
    }

    public Player(IFightManager fightManager, User user, byte index, short x, short y, byte[] items, short[] abilities, short teamPoints, boolean[] clanItems) {
        this.fightManager = fightManager;
        this.user = user;
        this.characterId = user.getActiveCharacterId();
        this.index = index;
        this.x = x;
        this.y = y;
        this.stamina = 60;
        this.items = items;
        this.teamPoints = teamPoints;
        this.clanItems = clanItems;

        this.maxHp = abilities[0];
        this.damage = abilities[1];
        this.defense = abilities[2];
        this.luck = abilities[3];

        if (user.getClanId() != null) {
            applyClanBonuses();
        }

        this.hp = maxHp;
    }

    private void applyClanBonuses() {
        if (clanItems[1]) { // 5% may mắn
            luck += (short) (luck * 5 / 100);
        }
        if (clanItems[3]) { // 5% phòng thủ
            defense += (short) (defense * 5 / 100);
        }
        if (clanItems[5]) { // 5% HP
            maxHp += (short) (maxHp * 5 / 100);
        }
        if (clanItems[6]) { // 5% sức mạnh
            damage += (short) (damage * 5 / 100);
        }
        if (clanItems[9]) { // 10% may mắn
            luck += (short) (luck * 10 / 100);
        }
        if (clanItems[11]) { // 10% phòng thủ
            defense += (short) (defense * 10 / 100);
        }
        if (clanItems[13]) { // 10% HP
            maxHp += (short) (maxHp * 10 / 100);
        }
        if (clanItems[14]) { // 10% sức mạnh
            damage += (short) (damage * 10 / 100);
        }
        if (clanItems[15]) { // 30% phòng thủ cho Canon và AK
            if (characterId == 1 || characterId == 5) {
                defense += (short) (defense * 30 / 100);
            }
        }
        if (clanItems[16]) { // 15% sức mạnh cho King Kong và Proton
            if (characterId == 2 || characterId == 3) {
                damage += (short) (damage * 15 / 100);
            }
        }
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
