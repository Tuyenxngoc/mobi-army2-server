package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.entry.GiftCodeEntry;

public interface IGiftCodeDao {

    GiftCodeEntry getGiftCode(String code);

    void updateGiftCode(String code, int playerId);

}
