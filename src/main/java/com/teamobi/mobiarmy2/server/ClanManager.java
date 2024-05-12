package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.json.ClanItemData;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        int playerId;
        String username;
        byte nvUsed;
        byte online;
        byte lever;
        byte levelPt;
        byte index;
        int cup;
        short[] dataEquip;
        String contribute_text;
        String contribute_count;
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

    public byte getClanLevel(short clanId) {
        int exp = clanDao.getExp(clanId);
        return Until.calculateLevelClan(exp);
    }

    public int getClanXu(short clanId) {
        return clanDao.getXu(clanId);
    }

    public int getClanLuong(short clanId) {
        return clanDao.getLuong(clanId);
    }

    public void updateItemClan(short clanId, int playerId, ItemClanData.ClanItemDetail clanItemDetail, boolean isBuyXu) {
        if (isBuyXu) {
            clanDao.updateXu(clanId, -clanItemDetail.getXu());
            clanDao.gopClanContribute("Mua item đội -" + Until.getStringNumber(clanItemDetail.getXu()) + " xu", playerId, -clanItemDetail.getXu(), 0);
        } else {
            clanDao.updateLuong(clanId, -clanItemDetail.getLuong());
            clanDao.gopClanContribute("Mua item đội -" + Until.getStringNumber(clanItemDetail.getLuong()) + " lượng", playerId, 0, -clanItemDetail.getLuong());
        }

        ClanItemData[] items = clanDao.getClanItems(clanId);
        boolean found = false;
        for (ClanItemData item : items) {
            if (item.getId() == clanItemDetail.getId()) {
                item.setTime(item.getTime().plusHours(clanItemDetail.getTime()));
                found = true;
                break;
            }
        }

        if (!found) {
            List<ClanItemData> updatedItems = new ArrayList<>(Arrays.asList(items));
            ClanItemData newItem = new ClanItemData();
            newItem.setId(clanItemDetail.getId());
            newItem.setTime(LocalDateTime.now().plusHours(clanItemDetail.getTime()));
            updatedItems.add(newItem);
            items = updatedItems.toArray(new ClanItemData[0]);
        }
        clanDao.updateClanItems(clanId, items);
    }

    public byte[] getClanIcon(short clanId) {
        return Until.getFile("res/icon/clan/" + clanDao.getClanIcon(clanId) + ".png");
    }

    public byte getTotalPage(short clanId) {
        Byte mem = clanDao.getMembersOfClan(clanId);
        if (mem == null) {
            return -1;
        }
        return (byte) Math.ceil((double) mem / 10);
    }

    public void contributeClan(short clanId, int playerId, int quantity, boolean isXu) {
        if (isXu) {
            clanDao.updateXu(clanId, quantity);
            clanDao.gopClanContribute("Góp " + Until.getStringNumber(quantity) + " xu", playerId, quantity, 0);
        } else {
            clanDao.updateLuong(clanId, quantity);
            clanDao.gopClanContribute("Góp " + Until.getStringNumber(quantity) + " lượng", playerId, 0, quantity);
        }
    }

    public List<ClanMemEntry> getMemberClan(short clanId, byte page) {
        return clanDao.getClanMember(clanId, page);
    }

    public ClanInfo getClanInfo(short clanId) {
        return clanDao.getClanInfo(clanId);
    }
}
