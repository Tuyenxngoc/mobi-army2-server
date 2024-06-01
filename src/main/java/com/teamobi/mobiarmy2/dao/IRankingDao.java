package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.entry.user.PlayerLeaderboardEntry;

import java.util.List;

public interface IRankingDao {

    List<PlayerLeaderboardEntry> getTopDanhDu();

    List<PlayerLeaderboardEntry> getTopCaoThu();

    List<PlayerLeaderboardEntry> getTopDaiGiaXu();

    List<PlayerLeaderboardEntry> getTopDaiGiaLuong();

    List<PlayerLeaderboardEntry> getTopDanhDuTuan();

    List<PlayerLeaderboardEntry> getTopDaiGiaTuan();

    void addBonusGift(int playerId, int quantity);

}
