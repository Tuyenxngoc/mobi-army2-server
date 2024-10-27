package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Reward;
import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class GiftBox extends Boss {

    private static final short[] COINS_BONUS = {1000, 5000, 10000, 15000, 20000, 25000, 30000};
    private static final short[] XP_BONUS = {50, 100, 150, 200, 250};
    private static final byte[] ITEM_IDS = {1, 2, 3, 4, 5};
    private static final byte[] SPECIAL_ITEM_IDS = {10, 11, 12, 13};

    public GiftBox(IFightManager fightManager, byte index, short x, short y) {
        super(fightManager, index, (byte) 24, "Gift Box", x, y, (short) 30, (short) 30, (short) 1, 0);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from GiftBox!");
    }

    public Reward getRandomReward() {
        Reward reward = new Reward();
        switch (Utils.nextInt(5)) {
            case 0 -> {
                short randomCoins = COINS_BONUS[Utils.nextInt(COINS_BONUS.length)];
                reward.coins(randomCoins);
            }

            case 1 -> {
                byte randomItemId = ITEM_IDS[Utils.nextInt(ITEM_IDS.length)];
                byte itemCount = (byte) (Utils.nextInt(3) + 1);
                reward.items(randomItemId, itemCount);
            }

            case 2 -> reward.equip();

            case 3 -> {
                short randomXP = XP_BONUS[Utils.nextInt(XP_BONUS.length)];
                reward.xp(randomXP);
            }

            case 4 -> {
                byte randomSpecialItemId = SPECIAL_ITEM_IDS[Utils.nextInt(SPECIAL_ITEM_IDS.length)];
                byte specialItemCount = (byte) (Utils.nextInt(2) + 1);
                reward.specialItems(randomSpecialItemId, specialItemCount);
            }
        }
        return reward;
    }
}
