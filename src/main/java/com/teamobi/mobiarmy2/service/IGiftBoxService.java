package com.teamobi.mobiarmy2.service;

public interface IGiftBoxService {
    boolean isOpeningGift();

    void startGiftBoxOpening(int availableGifts, int giftOpenTime);

    void openGiftBoxAfterFight(byte boxIndex);
}
