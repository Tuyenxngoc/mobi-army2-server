package com.teamobi.mobiarmy2.dao;

public interface IUserGiftCodeDAO {
    boolean existsByUserId(int userId);

    void create(long giftCodeId, int userId);
}
