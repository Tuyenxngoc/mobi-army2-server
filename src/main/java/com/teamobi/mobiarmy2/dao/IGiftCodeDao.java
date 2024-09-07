package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.GiftCodeEntry;

public interface IGiftCodeDao {

    GiftCodeEntry getGiftCode(String code, int playerId);

    void decrementGiftCodeUsageLimit(long giftCodeId);

    void logGiftCodeRedemption(long giftCodeId, int playerId);

}
