package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;

import java.util.List;

public interface ILeaderboardService {
    boolean isComplete();

    String[] getCategories();

    String[] getLabels();

    void init();

    List<UserLeaderboardDTO> getUsers(int type, int page, int pageSize);

    int getTotalPageByType(byte type);
}
