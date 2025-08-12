package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.dao.RankingDAO;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LeaderboardService {
    public static final String[] CATEGORIES = {"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    public static final String[] LABELS = {"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};
    private final RankingDAO rankingDAO;
    private final Timer timer;
    private final List<List<UserLeaderboardDTO>> leaderboardEntries;
    @Getter
    private boolean isComplete;

    public LeaderboardService(RankingDAO rankingDAO) {
        this.rankingDAO = rankingDAO;
        this.timer = new Timer(true);
        this.leaderboardEntries = new ArrayList<>(CATEGORIES.length);
        for (int i = 0; i < CATEGORIES.length; i++) {
            leaderboardEntries.add(new ArrayList<>());
        }
    }

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
                log.info("Refresh BXH");
            }
        }, calendar.getTime(), 86_400_000L);
    }

    private void addBonusGiftsForPlayers() {
        int[] topBonus = ApplicationContext.getInstance()
                .getBean(ServerConfig.class).getTopBonus();

        int i = 0;
        for (UserLeaderboardDTO userLeaderboardDTO : leaderboardEntries.getFirst()) {
            if (i >= 3) {
                break;
            }
            rankingDAO.addBonusGift(userLeaderboardDTO.getUserId(), topBonus[i]);
            i++;
        }
    }

    public int getTotalPageByType(byte type) {
        return leaderboardEntries.get(type).size() / 10;
    }

    public List<UserLeaderboardDTO> getUsers(int type, int page, int pageSize) {
        List<UserLeaderboardDTO> list = leaderboardEntries.get(type);
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        return list.subList(startIndex, endIndex);
    }

    private void refreshXH(int type) {
        List<UserLeaderboardDTO> list = leaderboardEntries.get(type);
        list.clear();
        switch (type) {
            case 0 -> list.addAll(rankingDAO.getTopCup());
            case 1 -> list.addAll(rankingDAO.getTopMasters());
            case 2 -> list.addAll(rankingDAO.getTopRichestXu());
            case 3 -> list.addAll(rankingDAO.getTopRichestLuong());
            case 4 -> list.addAll(rankingDAO.getWeeklyTopCup());
            case 5 -> list.addAll(rankingDAO.getWeeklyTopRichest());
        }
    }
}
