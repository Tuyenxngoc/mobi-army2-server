package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.dao.IRankingDAO;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.ILeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author tuyen
 */
public class LeaderboardService implements ILeaderboardService {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);

    private boolean isComplete;
    private final IRankingDAO rankingDao;
    private final Timer timer = new Timer(true);
    private final String[] leaderboardCategories = {"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    private final String[] leaderboardLabels = {"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};
    private final List<List<UserLeaderboardDTO>> leaderboardEntries = new ArrayList<>(leaderboardCategories.length);

    public LeaderboardService(IRankingDAO rankingDao) {
        this.rankingDao = rankingDao;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public String[] getCategories() {
        return leaderboardCategories;
    }

    @Override
    public String[] getLabels() {
        return leaderboardLabels;
    }

    @Override
    public void init() {
        for (int i = 0; i < leaderboardCategories.length; i++) {
            leaderboardEntries.add(new ArrayList<>());
        }
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
                for (byte i = 0; i < leaderboardCategories.length; i++) {
                    refreshXH(i);
                }
                addBonusGiftsForPlayers();
                isComplete = true;
                logger.info("Refresh BXH");
            }
        }, calendar.getTime(), 86_400_000L);
    }

    private void addBonusGiftsForPlayers() {
        int[] topBonus = ServerManager.getInstance().getConfig().getTopBonus();

        int i = 0;
        for (UserLeaderboardDTO userLeaderboardDTO : leaderboardEntries.getFirst()) {
            if (i >= 3) {
                break;
            }
            rankingDao.addBonusGift(userLeaderboardDTO.getUserId(), topBonus[i]);
            i++;
        }
    }

    @Override
    public List<UserLeaderboardDTO> getUsers(int type, int page, int pageSize) {
        List<UserLeaderboardDTO> list = leaderboardEntries.get(type);
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        return list.subList(startIndex, endIndex);
    }

    @Override
    public int getTotalPageByType(byte type) {
        return leaderboardEntries.get(type).size() / 10;
    }

    private void refreshXH(int type) {
        List<UserLeaderboardDTO> list = leaderboardEntries.get(type);
        list.clear();
        switch (type) {
            case 0 -> list.addAll(rankingDao.getTopCup());
            case 1 -> list.addAll(rankingDao.getTopMasters());
            case 2 -> list.addAll(rankingDao.getTopRichestXu());
            case 3 -> list.addAll(rankingDao.getTopRichestLuong());
            case 4 -> list.addAll(rankingDao.getWeeklyTopHonor());
            case 5 -> list.addAll(rankingDao.getWeeklyTopRichest());
        }
    }

}
