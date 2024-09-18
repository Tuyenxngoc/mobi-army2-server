package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class Player {

    protected IFightManager fightManager;
    private User user;
    private short gunId;
    private byte characterId;
    protected byte index;
    private byte pixel;
    private byte angry;
    private short steps;
    private byte stamina;
    protected short x;
    protected short y;
    private short maxHp;
    private short hp;
    private short damage;
    private short defense;
    private short luck;
    private short teamPoints;
    private byte[] items;
    private boolean isUpdateHP;
    private boolean isUpdateAngry;
    private boolean isUpdateXP;
    private boolean isLucky;
    private boolean isPoisoned;
    private boolean isFlying;
    private byte eyeSmokeCount;
    private byte invisibleCount;
    private byte freezeCount;
    private byte windStopCount;
    private boolean[] clanItems;
    private byte skippedTurns;
    private boolean itemUsed;
    private boolean isDoubleShoot;
    private boolean isDoubleSpeed;
    private boolean isUsePow;
    private boolean isDead;
    private boolean isTeamBlue;
    private byte usedItemId;
    private short width;
    private short height;
    private int xpUp;

    public Player(int index, int x, int y, int hp, int maxHp) {
        this.index = (byte) index;
        this.x = (short) x;
        this.y = (short) y;
        this.hp = (short) hp;
        this.maxHp = (short) maxHp;
    }

    public Player(IFightManager fightManager, byte index, byte characterId, short x, short y, short width, short height, short maxHp) {
        this.fightManager = fightManager;
        this.index = index;
        this.characterId = characterId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public Player(IFightManager fightManager, User user, byte index, boolean isTeamBlue, short x, short y, byte[] items, short[] abilities, short teamPoints, boolean[] clanItems) {
        this.fightManager = fightManager;
        this.user = user;
        this.gunId = user.getGunId();
        this.characterId = user.getActiveCharacterId();
        this.index = index;
        this.isTeamBlue = isTeamBlue;
        this.x = x;
        this.y = y;
        this.stamina = 60;
        this.width = 24;
        this.height = 24;
        this.items = items;
        this.teamPoints = teamPoints;
        this.clanItems = clanItems;
        this.usedItemId = -1;

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
        isDead = true;
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
            isDead = true;
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

    public void updateAngry(byte addAngry) {
        isUpdateAngry = true;
        angry += addAngry;
        if (angry < 0) {
            angry = 0;
        }
        if (angry > 100) {
            angry = 100;
        }
    }

    public void updateXp(int addXP) {
        if (user == null || addXP == 0) {
            return;
        }
        isUpdateXP = true;
        if (clanItems[1]) {
            addXP *= 2;
        }
        if (clanItems[8]) {
            addXP *= 3;
        }
        xpUp += addXP;
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

        // Nếu rơi quá bản đồ thì tự sát
        die();
    }

    public boolean isCollision(short x, short y) {
        if (eyeSmokeCount > 0) {
            return false;
        }
        return Utils.inRegion(x, y, this.x - this.width / 2, this.y - this.height, this.width, this.height);
    }

    public void collision(short bx, short by, Bullet bull) {
        //Bỏ qua nếu đã bại hoặc đang vô hình
        if (isDead || invisibleCount > 0) {
            return;
        }

        Player shooter = bull.getPl();
        int bullId = bull.getBullId();
        int shooterCharacterId = shooter.getCharacterId();

        //Logic tính toán tầm ảnh hưởng
        int impactRadius = Bullet.getImpactRadiusByBullId(bull.getBullId());
        if (bullId == 35 && shooterCharacterId == 15) {//T-rex jump
            impactRadius = 250;
        }

        // Nhân đôi tầm ảnh hưởng nếu sử dụng kỹ năng pow với các nhân vật cụ thể
        if (shooter.isUsePow() && (shooterCharacterId == 3 || shooterCharacterId == 4 || shooterCharacterId == 6 || shooterCharacterId == 7 || shooterCharacterId == 8)) {
            impactRadius *= 2;
        }

        // Kiểm tra điều kiện để bỏ qua xử lý va chạm
        if (!Utils.intersectRegions(x, y, width, height, bx, by, impactRadius * 2, impactRadius * 2)) {
            return;
        }

        // Bỏ qua nếu bullet không hợp lệ
        if (bullId == 31 || bullId == 32 || bullId == 35) {
            return;
        }

        // Tính toán khoảng cách từ điểm va chạm
        int deltaX = Math.abs(x - bx);
        int deltaY = Math.abs(y - height / 2 - by);
        int distance = (int) Math.hypot(deltaX, deltaY);

        // Tính sát thương
        int damage = bull.getDamage();
        if (distance > width / 2) {
            damage -= (damage * (distance - width / 2)) / impactRadius;
        }
        if (damage <= 0) {
            return;
        }

        //Nhân đôi sát thương nếu may mắn
        if (shooter.isLucky) {
            damage *= 2;
        }

        // Tăng sát thương từ item clan
        if (shooter.isUsePow()) {
            if (shooter.clanItems[5]) {
                damage += (damage * 5) / 100;  // +5% damage
            }
            if (shooter.clanItems[6]) {
                damage += (damage * 11) / 100;  // +10% damage
            }
        }

        // Tăng sát thương khi đạn siêu cao
        //todo...

        // Tính toán điểm phòng thủ
        int d = defense;
        if (isLucky) {
            // Giảm sát thương
            damage = Math.round((float) damage / 2);

            //Cộng thêm chỉ số phòng thủ
            d = defense + defense / 10;
        }
        if (d > 0) {
            damage = damage - d / 100;
        }

        updateHP((short) -damage);

        if (shooter instanceof Boss) {
            return;
        }

        if (isDead) {
            switch (this.characterId) {
                case 6 -> System.out.println("Update mission 1");
                case 7 -> System.out.println("Update mission 2");
                case 9 -> System.out.println("Update mission 3");
            }
        }
    }

    public void resetValueInNewTurn() {
        itemUsed = false;
        usedItemId = -1;
        stamina = 60;
        steps = 0;
    }
}
