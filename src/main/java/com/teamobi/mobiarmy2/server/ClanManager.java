package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanManager {

    @Getter
    @Setter
    public static class ClanInfo {
        private short id;
        private String name;
        private byte memberCount;
        private byte maxMemberCount;
        private String masterName;
        private int xu;
        private int luong;
        private int cup;
        private int exp;
        private int xpUpLevel;
        private byte level;
        private byte levelPercentage;
        private String description;
        private String dateCreated;
        private List<ClanItem> items;
    }

    @Getter
    @Setter
    public static class ClanItem {
        private String name;
        private int time;
    }

    @Getter
    @Setter
    public static class ClanEntry {

        int id;
        int master;
        String name;
        int icon;
        String thongBao;
        String item;
        int xu;
        int luong;
        int xp;
        int cup;
        int mem;
        int memMax;
        int level;
        String dateCreat;
        String masterName;
    }

    @Getter
    @Setter
    public static class ClanMemEntry {
        byte index;
        int id;
        int clan;
        Date timeJoin;
        int xu;
        int luong;
        int cup;
        String n_contribute;
        String contribute_time;
        String contribute_text;
        byte right;
        byte nvUsed;
        byte online;
        int lever;
        byte levelPt;
        int xp;
        short[] dataEquip;
        String name;
    }

    private static ClanManager instance;
    private final IClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    public static ClanManager getInstance() {
        if (instance == null) {
            instance = new ClanManager();
        }
        return instance;
    }

    public byte[] getClanIcon(short clanId) {
        byte[] data;
        Short index = clanDao.getClanIcon(clanId);
        if (index == null) {
            data = new byte[0];
        } else {
            data = Until.getFile("res/icon/clan/" + index + ".png");
        }
        return data;
    }

    public void contributeClan(short clanId, int playerId, int quantity, boolean isXu) {
        if (isXu) {
            clanDao.updateXu(clanId, quantity);
            clanDao.gopClanContribute(Until.getStringNumber(quantity) + " xu", playerId, quantity, 0);
        } else {
            clanDao.updateLuong(clanId, quantity);
            clanDao.gopClanContribute(Until.getStringNumber(quantity) + " lượng", playerId, 0, quantity);
        }
    }

    public List<ClanMemEntry> getMemberClan(short clanId, byte page) {
        return clanDao.getClanMember(clanId, page);
    }

    public ClanInfo getClanInfo(short clanId) {
        return clanDao.getClanInfo(clanId);
    }
}
