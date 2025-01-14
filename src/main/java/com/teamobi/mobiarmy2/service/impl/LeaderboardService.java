package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.dao.IRankingDAO;
import com.teamobi.mobiarmy2.dto.PlayerLeaderboardDTO;
import com.teamobi.mobiarmy2.service.ILeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author tuyen
 */
public class LeaderboardService implements ILeaderboardService {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    private static final String[] CATEGORIES = {"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    private static final String[] LABELS = {"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};

    private boolean isComplete;
    private final IRankingDAO rankingDAO;
    private final IServerConfig serverConfig;
    private final Timer timer;
    private final List<List<PlayerLeaderboardDTO>> leaderboardEntries;

    public LeaderboardService(IRankingDAO rankingDAO, IServerConfig serverConfig) {
        this.rankingDAO = rankingDAO;
        this.serverConfig = serverConfig;
        this.timer = new Timer(true);
        this.leaderboardEntries = new ArrayList<>(CATEGORIES.length);
        for (int i = 0; i < CATEGORIES.length; i++) {
            leaderboardEntries.add(new ArrayList<>());
        }
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public String[] getCategories() {
        return CATEGORIES;
    }

    @Override
    public String[] getLabels() {
        return LABELS;
    }

    @Override
    public void init() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MILLISECOND, 0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isComplete = false;
                for (byte i = 0; i < CATEGORIES.length; i++) {
                    refreshXH(i);
                }
                addBonusGiftsForPlayers();
                isComplete = true;
                logger.info("Refresh BXH");
            }
        }, calendar.getTime(), 86_400_000L);
    }

    private void addBonusGiftsForPlayers() {
        int[] topBonus = serverConfig.getTopBonus();

        int i = 0;
        for (PlayerLeaderboardDTO playerLeaderboardDTO : leaderboardEntries.getFirst()) {
            if (i >= 3) {
                break;
            }
            rankingDAO.addBonusGift(playerLeaderboardDTO.getPlayerId(), topBonus[i]);
            i++;
        }
    }

    @Override
    public int getTotalPageByType(byte type) {
        return leaderboardEntries.get(type).size() / 10;
    }

    @Override
    public List<PlayerLeaderboardDTO> getUsers(int type, int page, int pageSize) {
        List<PlayerLeaderboardDTO> list = leaderboardEntries.get(type);
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        return list.subList(startIndex, endIndex);
    }

    private void refreshXH(int type) {
        List<PlayerLeaderboardDTO> list = leaderboardEntries.get(type);
        list.clear();
        switch (type) {
            case 0 -> list.addAll(rankingDAO.getTopHonor());
            case 1 -> list.addAll(rankingDAO.getTopMasters());
            case 2 -> list.addAll(rankingDAO.getTopRichestXu());
            case 3 -> list.addAll(rankingDAO.getTopRichestLuong());
            case 4 -> list.addAll(rankingDAO.getWeeklyTopHonor());
            case 5 -> list.addAll(rankingDAO.getWeeklyTopRichest());
        }
    }

}
