package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.dto.PlayerLeaderboardDTO;

import java.util.List;

public interface ILeaderboardService {
    boolean isComplete();

    String[] getCategories();

    String[] getLabels();

    void init();

    List<PlayerLeaderboardDTO> getUsers(int type, int page, int pageSize);

    int getTotalPageByType(byte type);
}
