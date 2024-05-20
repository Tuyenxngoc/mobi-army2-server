package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.GiftCode.GetGiftCode;

public interface IGiftCodeDao {

    GetGiftCode getGiftCode(String code);

    void updateGiftCode(GetGiftCode giftCode);

}
