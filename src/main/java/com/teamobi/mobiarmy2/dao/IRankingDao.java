package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.entry.user.PlayerLeaderboardEntry;

import java.util.List;

public interface IRankingDao {

    List<PlayerLeaderboardEntry> getTopHonor();

    List<PlayerLeaderboardEntry> getTopMasters();

    List<PlayerLeaderboardEntry> getTopRichestXu();

    List<PlayerLeaderboardEntry> getTopRichestLuong();

    List<PlayerLeaderboardEntry> getWeeklyTopHonor();

    List<PlayerLeaderboardEntry> getWeeklyTopRichest();

    void addBonusGift(int playerId, int quantity);

}
