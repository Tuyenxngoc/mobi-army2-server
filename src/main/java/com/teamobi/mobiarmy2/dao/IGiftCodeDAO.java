package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.GiftCodeDTO;

/**
 * @author tuyen
 */
public interface IGiftCodeDAO {

    GiftCodeDTO findById(String code);

    void decrementUsageLimit(long giftCodeId);

}
