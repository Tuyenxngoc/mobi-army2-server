package com.teamobi.mobiarmy2.fight;

/**
 * @author tuyen
 */
public interface IGiftBoxManager {

    boolean isOpeningGift();

    void startGiftBoxOpening(int availableGifts, int giftOpenTime);

    void openGiftBoxAfterFight(byte boxIndex);
}
