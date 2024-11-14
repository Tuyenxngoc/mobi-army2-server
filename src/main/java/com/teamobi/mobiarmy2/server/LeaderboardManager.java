package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.dao.impl.RankingDao;
import com.teamobi.mobiarmy2.model.user.PlayerLeaderboardEntry;
import lombok.Getter;

import java.util.*;

/**
 * @author tuyen
 */
public class LeaderboardManager {
    private final IRankingDao rankingDao;

    public LeaderboardManager() {
        this.rankingDao = new RankingDao();
    }

    private static class SingletonHelper {
        private static final LeaderboardManager INSTANCE = new LeaderboardManager();
    }

    public static LeaderboardManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Getter
    private boolean isComplete;
    private final Timer timer = new Timer(true);
    @Getter
    private final String[] leaderboardCategories = {"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    @Getter
    private final String[] leaderboardLabels = {"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};
    @Getter
    private final List<List<PlayerLeaderboardEntry>> leaderboardEntries = new ArrayList<>(leaderboardCategories.length);

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
                ServerManager.getInstance().getLog().success("Refresh BXH");
            }
        }, calendar.getTime(), 86_400_000L);
    }

    private void addBonusGiftsForPlayers() {
        int[] topBonus = ServerManager.getInstance().getConfig().getTopBonus();

        int i = 0;
        for (PlayerLeaderboardEntry entry : leaderboardEntries.getFirst()) {
            if (i >= 3) {
                break;
            }
            rankingDao.addBonusGift(entry.getPlayerId(), topBonus[i]);
            i++;
        }
    }

    public List<PlayerLeaderboardEntry> getLeaderboardEntries(int type, int page, int pageSize) {
        List<PlayerLeaderboardEntry> list = leaderboardEntries.get(type);
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        return list.subList(startIndex, endIndex);
    }

    private void refreshXH(int type) {
        List<PlayerLeaderboardEntry> list = leaderboardEntries.get(type);
        list.clear();
        switch (type) {
            case 0 -> list.addAll(rankingDao.getTopHonor());
            case 1 -> list.addAll(rankingDao.getTopMasters());
            case 2 -> list.addAll(rankingDao.getTopRichestXu());
            case 3 -> list.addAll(rankingDao.getTopRichestLuong());
            case 4 -> list.addAll(rankingDao.getWeeklyTopHonor());
            case 5 -> list.addAll(rankingDao.getWeeklyTopRichest());
        }
    }

}
