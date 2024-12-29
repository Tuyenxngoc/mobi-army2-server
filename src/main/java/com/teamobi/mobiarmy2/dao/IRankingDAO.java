package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.PlayerLeaderboardDTO;

import java.util.List;

/**
 * @author tuyen
 */
public interface IRankingDAO {

    List<PlayerLeaderboardDTO> getTopHonor();

    List<PlayerLeaderboardDTO> getTopMasters();

    List<PlayerLeaderboardDTO> getTopRichestXu();

    List<PlayerLeaderboardDTO> getTopRichestLuong();

    List<PlayerLeaderboardDTO> getWeeklyTopHonor();

    List<PlayerLeaderboardDTO> getWeeklyTopRichest();

    void addBonusGift(int playerId, int quantity);

}
