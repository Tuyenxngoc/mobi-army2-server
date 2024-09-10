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

    private IFightManager fightManager;
    private User user;
    private byte characterId;
    private byte index;
    private byte pixel;
    private byte angry;
    private short steps;
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
    private boolean isFlying;
    private byte eyeSmokeCount;
    private byte freezeCount;
    private byte windStopCount;
    private boolean[] clanItems;
    private byte skippedTurns;
    private boolean itemUsed;
    private boolean isDoubleShoot;
    private boolean isDoubleSpeed;
    private boolean isUsePow;

    public Player(int index, int x, int y, int hp, int maxHp) {
        this.index = (byte) index;
        this.x = (short) x;
        this.y = (short) y;
        this.hp = (short) hp;
        this.maxHp = (short) maxHp;
    }

    public Player(IFightManager fightManager, byte index, byte characterId, short x, short y, short maxHp) {
        this.fightManager = fightManager;
        this.index = index;
        this.characterId = characterId;
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
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
        if (clanItems[8]) { // 10% may mắn
            luck += (short) (luck * 10 / 100);
        }
        if (clanItems[10]) { // 10% phòng thủ
            defense += (short) (defense * 10 / 100);
        }
        if (clanItems[12]) { // 10% HP
            maxHp += (short) (maxHp * 10 / 100);
        }
        if (clanItems[13]) { // 10% sức mạnh
            damage += (short) (damage * 10 / 100);
        }
        if (clanItems[14]) { // 30% phòng thủ cho Canon và AK
            if (characterId == 1 || characterId == 5) {
                defense += (short) (defense * 30 / 100);
            }
        }
        if (clanItems[15]) { // 15% sức mạnh cho King Kong và Proton
            if (characterId == 2 || characterId == 3) {
                damage += (short) (damage * 15 / 100);
            }
        }
    }

    public void die() {
        hp = 0;
        isUpdateHP = true;
    }

    public void nextLuck() {
        isLucky = Math.random() < 0.5;
    }

    public void decreaseWindStopCount() {
        if (windStopCount > 0) {
            windStopCount--;
        }
    }

    public void incrementSkippedTurns() {
        skippedTurns++;
    }

    public void updateHP(short addHp) {
        isUpdateHP = true;
        hp += addHp;
        if (hp <= 0) {
            hp = 0;
        } else if (hp < 10) {
            hp = 10;
        } else if (hp > maxHp) {
            hp = maxHp;
        }
        int oldPixel = pixel;
        pixel = (byte) (hp * 25 / maxHp);

        if (addHp != 0) {
            updateAngry((byte) ((oldPixel - pixel) * 4));
        }
    }

    private void updateAngry(byte addAngry) {
        isUpdateAngry = true;
        angry += addAngry;
        if (angry < 0) {
            angry = 0;
        }
        if (angry > 100) {
            angry = 100;
        }
    }

    public byte getPowerUsageStatus() {
        return (byte) (isUsePow ? 1 : 0);
    }

    public void updateXY(short x, short y) {
        while (x != this.x || y != this.y) {
            int preX = this.x;
            int preY = this.y;
            if (x < this.x) {
                move(false);
            } else if (x > this.x) {
                move(true);
            }

            //Nếu không di chuyển được thì thoát vòng lặp
            if (preX == this.x && preY <= this.y) {
                return;
            }
        }
    }

    protected void move(boolean addX) {
        IMapManager mapManager = fightManager.getMapManger();
        if (this.freezeCount > 0) {
            return;
        }
        byte step = 1;
        if (this.isDoubleSpeed) {
            step = 2;
        }
        if (steps > stamina) {
            return;
        }
        steps++;
        if (addX) {
            x += step;
        } else {
            x -= step;
        }
        if (mapManager.isCollision(x, (short) (y - 5))) {
            steps--; // Giảm số bước nếu không thể di chuyển
            if (addX) {
                x -= step;
            } else {
                x += step;
            }
            return;
        }
        for (short i = 4; i >= 0; i--) {
            if (mapManager.isCollision(x, (short) (y - i))) {
                y -= i;
                return;
            }
        }
        updateYPosition();
    }

    public void updateYPosition() {
        IMapManager mapManager = fightManager.getMapManger();
        while (y < mapManager.getHeight() + 200) {
            if (mapManager.isCollision(x, y) || isFlying) {
                return;
            }
            y++;
        }
    }
}
