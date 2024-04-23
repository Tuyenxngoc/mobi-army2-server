package com.teamobi.mobiarmy2.dao;

public interface IClanDao {

    Short getClanIcon(int clanId);

    void gopXu(int clanId, int xu);

    void gopLuong(int clanId, int luong);

    void gopClanContribute(String txtContribute, int userId);
}
