package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;

import java.util.List;

/**
 * @author tuyen
 */
public interface IRankingDAO {

    List<UserLeaderboardDTO> getTopCup();

    List<UserLeaderboardDTO> getTopMasters();

    List<UserLeaderboardDTO> getTopRichestXu();

    List<UserLeaderboardDTO> getTopRichestLuong();

    List<UserLeaderboardDTO> getWeeklyTopCup();

    List<UserLeaderboardDTO> getWeeklyTopRichest();

    void addBonusGift(int userId, int quantity);

}
