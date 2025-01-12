package com.teamobi.mobiarmy2.service;

/**
 * @author tuyen
 */
public interface IGiftBoxService {
    boolean isOpeningGift();

    void startGiftBoxOpening(int availableGifts, int giftOpenTime);

    void openGiftBoxAfterFight(byte boxIndex);
}
