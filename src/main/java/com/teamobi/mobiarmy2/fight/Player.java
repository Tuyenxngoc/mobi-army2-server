package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.fight.boss.GiftBox;
import com.teamobi.mobiarmy2.fight.boss.GiftBoxFalling;
import com.teamobi.mobiarmy2.util.RandomUtil;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class Player {
    // Hằng số K trong công thức: P = stat / (stat + K)
    // Ý nghĩa:
    // - Khi stat = 3825 → hiệu quả = 50%
    // - Điều khiển tốc độ tăng (diminishing return)
    // - Stat càng cao → tăng hiệu quả càng chậm, không bao giờ đạt 100%
    // Dùng cho các hệ: luck (tỉ lệ), phòng thủ (giảm damage), v.v.
    public static final int STAT_HALF_EFFECT_POINT = 3825;

    protected FightManager fightManager;
    protected User user;
    protected byte characterId;
    protected byte index;
    protected short gunId;
    protected boolean isTeamBlue;

    protected short x;
    protected short y;
    protected short width;
    protected short height;
    protected short steps;
    protected boolean isFlying;

    protected int hp;
    protected int maxHp;
    protected int damagePercent;
    protected int defense;
    protected int luck;
    protected byte stamina;
    protected byte angry;
    protected byte pixel;

    protected boolean isDead;
    protected boolean isLucky;
    protected boolean isPoisoned;
    protected boolean isDoubleShoot;
    protected boolean isDoubleSpeed;
    protected boolean isUsePow;
    protected boolean itemUsed;
    protected byte usedItemId;

    protected byte invisibleCount; // Số lần vô hình
    protected byte vanishCount; // Số lần tàn hình
    protected byte vampireCount; // Số lần hút máu
    protected byte freezeCount;
    protected byte windStopCount;
    protected byte eyeSmokeCount;
    protected byte skippedTurns;
    protected byte inactiveTurns;

    protected byte[] items;
    protected boolean[] clanItems;

    protected boolean isUpdateHP;
    protected boolean isUpdateAngry;
    protected boolean isUpdateXP;
    protected boolean isUpdateCup;

    protected int xpUp;
    protected int allXpUp;
    protected int cupUp;
    protected int allCupUp;
    protected int xpExist;
    protected List<Reward> rewards;

    public Player(int index, int x, int y, int hp, int maxHp) {
        this.index = (byte) index;
        this.x = (short) x;
        this.y = (short) y;
        this.hp = hp;
        this.maxHp = maxHp;
    }

    public Player(FightManager fightManager, byte characterId, short x, short y, short width, short height, int maxHp,
                  int xpExist) {
        this.fightManager = fightManager;
        this.characterId = characterId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.xpExist = xpExist;
    }

    public Player(FightManager fightManager, User user, byte index, boolean isTeamBlue, short x, short y, byte[] items,
                  int[] abilities, boolean[] clanItems) {
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
        this.clanItems = clanItems;
        this.usedItemId = -1;
        this.xpExist = user.getCurrentLevel() / 2 + 2;

        this.maxHp = abilities[0];
        this.damagePercent = abilities[1];
        this.defense = abilities[2];
        this.luck = abilities[3];

        if (user.hasClan()) {
            applyClanBonuses();
        }

        this.hp = maxHp;
    }

    private void applyClanBonuses() {
        if (clanItems[1]) { // 5% may mắn
            luck = Utils.calculatePercentBonus(luck, 5);
        }
        if (clanItems[3]) { // 5% phòng thủ
            defense = Utils.calculatePercentBonus(defense, 5);
        }
        if (clanItems[5]) { // 5% HP
            maxHp = Utils.calculatePercentBonus(maxHp, 5);
        }
        if (clanItems[6]) { // 5% sức mạnh
            damagePercent = Utils.calculatePercentBonus(damagePercent, 5);
        }
        if (clanItems[8]) { // 10% may mắn
            luck = Utils.calculatePercentBonus(luck, 10);
        }
        if (clanItems[10]) { // 10% phòng thủ
            defense = Utils.calculatePercentBonus(defense, 10);
        }
        if (clanItems[12]) { // 10% HP
            maxHp = Utils.calculatePercentBonus(maxHp, 10);
        }
        if (clanItems[13]) { // 10% sức mạnh
            damagePercent = Utils.calculatePercentBonus(damagePercent, 10);
        }
        if (clanItems[14]) { // 30% phòng thủ cho Canon và AK
            if (characterId == 1 || characterId == 5) {
                defense = Utils.calculatePercentBonus(defense, 30);
            }
        }
        if (clanItems[15]) { // 15% sức mạnh cho King Kong và Proton
            if (characterId == 2 || characterId == 3) {
                damagePercent = Utils.calculatePercentBonus(damagePercent, 15);
            }
        }
    }

    public synchronized void die() {
        hp = 0;
        isUpdateHP = true;
        isDead = true;
        isLucky = false;
    }

    public void nextLuck() {
        // Tỉ lệ = luck / (luck + BASE_STAT_LIMIT)
        isLucky = RandomUtil.nextInt(luck + STAT_HALF_EFFECT_POINT) < luck;
    }

    public void decreaseWindStopCount() {
        if (windStopCount > 0) {
            windStopCount--;
        }
    }

    public void incrementSkippedTurns() {
        skippedTurns++;
    }

    public synchronized void updateHP(int addHp) {
        isUpdateHP = true;
        hp += addHp;

        // Nếu may mắn và máu thấp hơn 10 thì máu bằng 10
        if (isLucky && hp < 10) {
            hp = 10;
        }

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

    public synchronized void updateXp(int addXP, boolean shareXp) {
        if (user == null || addXP == 0) {
            return;
        }

        // Cộng XP cho đồng đội
        int teamXp = addXP / 4;
        if (shareXp && teamXp > 1) {
            fightManager.giveXpToTeammates(isTeamBlue, teamXp, this);
        }

        if (clanItems[0]) {
            addXP *= 2;
        }
        if (clanItems[7]) {
            addXP *= 3;
        }
        isUpdateXP = true;
        xpUp += addXP;
        allXpUp += addXP;
    }

    public synchronized void updateCup(int addCup) {
        if (user == null || addCup == 0) {
            return;
        }
        isUpdateCup = true;
        cupUp += addCup;
        allCupUp += addCup;
    }

    public synchronized void updateXY(short x, short y) {
        if (this.isFlying) {
            this.x = x;
            this.y = y;
            return;
        }
        while (this.x != x || this.y != y) {
            short preX = this.x;
            short preY = this.y;
            if (this.x < x) {
                move(true);
            } else if (this.x > x) {
                move(false);
            } else if (this.y != y) {
                // Nếu X đã khớp nhưng Y chưa khớp (rơi thẳng đứng)
                updateYPosition();
            }

            // Nếu không di chuyển được nữa (vướng tường hoặc đã chạm đất) thì thoát
            if (preX == this.x && preY == this.y) {
                return;
            }
        }
    }

    private void move(boolean addX) {
        FightMapManager mapManager = fightManager.getFightMapManager();
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

        // Logic vướng tường (Wall check)
        if (mapManager.isCollision(x, (short) (y - 5))) {
            steps--; // Trả lại step
            if (addX) {
                x -= step;
            } else {
                x += step;
            }
            return;
        }

        // Logic leo dốc (Slope check)
        for (short i = 4; i >= 0; i--) {
            if (mapManager.isCollision(x, (short) (y - i))) {
                y -= i;
                return;
            }
        }

        // Nếu không có đất dưới chân -> rơi (Fall)
        updateYPosition();
    }

    public synchronized void updateYPosition() {
        if (isFlying || isDead) {
            return;
        }
        FightMapManager mapManager = fightManager.getFightMapManager();
        // Rơi theo từng pixel cho đến khi chạm đất hoặc ra ngoài bản đồ
        while (y < mapManager.getHeight() + 200) {
            if (mapManager.isCollision(x, y)) {
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

    public synchronized void collision(short bx, short by, Bullet bull) {
        // Bỏ qua nếu đã bại hoặc đang vô hình
        if (isDead || invisibleCount > 0 || characterId == 17) {
            return;
        }

        // Bỏ qua va chạm cho boom bum
        if ((bull.bullId == 31 || bull.bullId == 32 || bull.bullId == 35) && this.index >= 8) {
            return;
        }

        Player shooter = bull.getPlayer();
        int bullId = bull.getBullId();
        int shooterCharacterId = shooter.getCharacterId();

        // Logic tính toán tầm ảnh hưởng
        int impactRadius = Bullet.getImpactRadiusByBullId(bull.getBullId());
        if (bullId == 35 && shooterCharacterId == 15) {// T. rex jump
            impactRadius = 250;
        }

        // Nhân đôi tầm ảnh hưởng nếu sử dụng kỹ năng pow với các nhân vật cụ thể
        if (shooter.isUsePow() && (shooterCharacterId == 3 || shooterCharacterId == 4 || shooterCharacterId == 6
                || shooterCharacterId == 7 || shooterCharacterId == 8)) {
            impactRadius *= 2;
        }

        // Kiểm tra điều kiện để bỏ qua xử lý va chạm
        if (!Utils.intersectRegions(x, y, width, height, bx, by, impactRadius * 2, impactRadius * 2)) {
            return;
        }

        // Tính toán khoảng cách từ điểm va chạm
        int deltaX = Math.abs(x - bx);
        int deltaY = Math.abs(y - height / 2 - by);
        int distance = (int) Math.hypot(deltaX, deltaY);

        // Tính sát thương
        int damage = bull.getDamage();
        if (distance > width / 2) {
            damage -= (int) Math.round((double) damage * (distance - width / 2) / impactRadius);
        }
        if (damage <= 0) {
            return;
        }

        // Nhân đôi sát thương nếu may mắn
        if (shooter.isLucky) {
            damage *= 2;
        }

        // Tăng sát thương từ item clan khi sử dụng tuyệt chiêu Pow
        if (shooter.isUsePow()) {
            if (shooter.clanItems[4]) {
                damage = Utils.calculatePercentBonus(damage, 5); // +5% damage
            }
            if (shooter.clanItems[11]) {
                damage = Utils.calculatePercentBonus(damage, 10); // +10% damage
            }
        }

        // Tăng sát thương khi đạn siêu cao
        byte superType = bull.getBulletManager().getSuperType();
        switch (superType) {
            case 1 -> damage = Utils.calculatePercentBonus(damage, 10);// +10%
            case 2 -> damage = Utils.calculatePercentBonus(damage, 20);// +20%
            case 4 -> damage = Utils.calculatePercentBonus(damage, 30);// +30%
        }

        // Tính toán điểm phòng thủ
        int effectiveDefense = defense;
        if (isLucky) {
            // Giảm sát thương
            damage = Math.round((float) damage / 2);

            // Cộng thêm 10% chỉ số phòng thủ khi may mắn
            effectiveDefense = Utils.calculatePercentBonus(defense, 10);
        }

        if (effectiveDefense > 0) {
            log.info("Effective Defense: {}", effectiveDefense);

            // Áp dụng công thức phòng thủ: D = A * STAT_HALF_EFFECT_POINT /
            // (STAT_HALF_EFFECT_POINT + DEF)
            damage = (int) Math
                    .round((double) damage * STAT_HALF_EFFECT_POINT / (STAT_HALF_EFFECT_POINT + effectiveDefense));
        }

        log.info("Damage: {}", damage);

        updateHP(-damage);

        if (shooter instanceof Boss || shooter == this) {
            return;
        }

        if (isDead) {
            switch (characterId) {
                case 6 -> shooter.getUser().updateMission(6, 1);
                case 7 -> shooter.getUser().updateMission(7, 1);
                case 9 -> shooter.getUser().updateMission(8, 1);
                case 23 -> {
                    GiftBoxFalling giftBoxFalling = (GiftBoxFalling) this;
                    shooter.addReward(giftBoxFalling.getRandomReward());
                }
                case 24 -> {
                    GiftBox giftBox = (GiftBox) this;
                    shooter.addReward(giftBox.getRandomReward());
                }
                case 26 -> {
                    // Player players = new Ghost2(fightManager, (byte) (fightMNG.allCount +
                    // fightMNG.bullMNG.addboss.size()), 1800 + (fightMNG.getLevelTeam() * 10),
                    // (short) (Until.nextInt(100, fightMNG.mapMNG.Width - 100)), (short)
                    // Until.nextInt(150));
                }
            }

            // Chỉ cộng XP và Cup nếu hạ gục kẻ địch
            if (shooter.isTeamBlue() != this.isTeamBlue()) {
                // Cộng xp
                shooter.updateXp(xpExist, true);

                // Logic cộng cup
                if (shooter.getUser() != null && user != null) {
                    int cupDifference = shooter.getUser().getCup() - user.getCup();
                    int cupUp = (3000 - cupDifference) / 100;
                    if (cupUp > 0) {
                        if (cupUp > 60) {
                            cupUp = 60;
                        }
                        updateCup(-cupUp);
                        shooter.updateCup(cupUp);
                    }
                }
            }
        }
    }

    private void addReward(Reward reward) {
        if (rewards == null) {
            rewards = new ArrayList<>();
        }

        rewards.add(reward);
    }

    public synchronized void resetValueInNewTurn() {
        itemUsed = false;
        isUsePow = false;
        usedItemId = -1;
        stamina = 60;
        steps = 0;
    }

    public synchronized void setXY(short x, short y) {
        if (x >= 0 && x < fightManager.getFightMapManager().getWidth()
                && y < fightManager.getFightMapManager().getHeight()) {
            this.x = x;
            this.y = y;
        }
    }

    public synchronized void usedItem(int slot) {
        usedItemId = items[slot];
        if (usedItemId == 0 || usedItemId == 2 || usedItemId == 3 || usedItemId == 4 || usedItemId == 5
                || usedItemId == 10 || usedItemId == 32 || usedItemId == 33 || usedItemId == 34 || usedItemId == 35
                || usedItemId == 100) {
            usedItemId = -1;
        }
        itemUsed = true;
        items[slot] = -1;
    }

    public boolean shouldCollide() {
        return true;
    }

    public boolean shouldCollideWith(Bullet bullet) {
        return true;
    }
}
