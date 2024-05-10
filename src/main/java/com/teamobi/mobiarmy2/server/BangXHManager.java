package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.dao.impl.RankingDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tuyen
 */
public class BangXHManager {

    @Getter
    @Setter
    public static class BangXHEntry {
        int playerId;
        String username;
        short clanId;
        byte nvUsed;
        byte level;
        byte levelPt;
        byte index;
        short[] data;
        String detail;
    }

    private static BangXHManager instance;
    private final IRankingDao rankingDao;

    public BangXHManager() {
        this.rankingDao = new RankingDao();
    }

    public static BangXHManager getInstance() {
        if (instance == null) {
            instance = new BangXHManager();
        }
        return instance;
    }

    @Getter
    private final boolean isComplete = true;
    @Getter
    private final String[] bangXHString = new String[]{"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    @Getter
    private final String[] bangXHString1 = new String[]{"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};

    private List<BangXHEntry> bangXHDanhDu;
    private List<BangXHEntry> bangXHCaoThu;
    private List<BangXHEntry> bangXHDaiGiaXu;
    private List<BangXHEntry> bangXHDaiGiaLuong;
    private List<BangXHEntry> bangXHDanhDuTuan;
    private List<BangXHEntry> bangXHDaiGiaTuan;

    public void init() {
        getBangXhDanhDu();
        getBangXhCaoThu();
        getBangXhDaiGiaXu();
        getBangXhDaiGiaLuong();
        getBangXhDanhDuTuan();
        getBangXhDaiGiaTuan();
    }

    private void getBangXhDanhDu() {
        bangXHDanhDu = rankingDao.getTopDanhDu();
    }

    private void getBangXhCaoThu() {
        bangXHCaoThu = rankingDao.getTopCaoThu();
    }

    private void getBangXhDaiGiaXu() {
        bangXHDaiGiaXu = rankingDao.getTopDaiGiaXu();
    }

    private void getBangXhDaiGiaLuong() {
        bangXHDaiGiaLuong = rankingDao.getTopDaiGiaLuong();
    }

    private void getBangXhDanhDuTuan() {
        bangXHDanhDuTuan = rankingDao.getTopDanhDuTuan();
    }

    private void getBangXhDaiGiaTuan() {
        bangXHDaiGiaTuan = rankingDao.getTopDaiGiaTuan();
    }

    public BangXHEntry[] getBangXH(int type, int page) {
        BangXHEntry[] result = new BangXHEntry[2];
        for (int i = 0; i < 2; i++) {
            result[i] = bangXHCaoThu.get(i);
        }
        return result;
    }

}
