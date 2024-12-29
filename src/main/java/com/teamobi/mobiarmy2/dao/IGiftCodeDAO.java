package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.GiftCodeDTO;

/**
 * @author tuyen
 */
public interface IGiftCodeDAO {

    GiftCodeDTO getGiftCode(String code, int playerId);

    void decrementGiftCodeUsageLimit(long giftCodeId);

    void logGiftCodeRedemption(long giftCodeId, int playerId);

}
