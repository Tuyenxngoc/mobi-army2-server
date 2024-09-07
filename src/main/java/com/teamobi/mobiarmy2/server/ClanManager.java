package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.repository.ClanItemData;
import com.teamobi.mobiarmy2.model.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.item.ClanItemEntry;
import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanManager {

    private static volatile ClanManager instance;
    private final IClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    public static ClanManager getInstance() {
        if (instance == null) {
            synchronized (ClanManager.class) {
                if (instance == null) {
                    instance = new ClanManager();
                }
            }
        }
        return instance;
    }

    public byte getClanLevel(short clanId) {
        int exp = clanDao.getExp(clanId);
        return Utils.calculateLevelClan(exp);
    }

    public int getClanXu(short clanId) {
        return clanDao.getXu(clanId);
    }

    public int getClanLuong(short clanId) {
        return clanDao.getLuong(clanId);
    }

    public void updateItemClan(short clanId, int playerId, ClanItemEntry clanItemEntry, boolean isBuyXu) {
        if (isBuyXu) {
            clanDao.updateXu(clanId, -clanItemEntry.getXu());
            clanDao.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemEntry.getXu()) + " xu", playerId, -clanItemEntry.getXu(), 0);
        } else {
            clanDao.updateLuong(clanId, -clanItemEntry.getLuong());
            clanDao.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemEntry.getLuong()) + " lượng", playerId, 0, -clanItemEntry.getLuong());
        }

        ClanItemJson[] items = clanDao.getClanItems(clanId);
        boolean found = false;
        LocalDateTime now = LocalDateTime.now();

        for (ClanItemJson item : items) {
            if (item.getId() == clanItemEntry.getId()) {
                if (item.getTime().isBefore(now)) {
                    item.setTime(now);
                }
                item.setTime(item.getTime().plusHours(clanItemEntry.getTime()));
                found = true;
                break;
            }
        }

        if (!found) {
            List<ClanItemJson> updatedItems = new ArrayList<>(Arrays.asList(items));
            ClanItemJson newItem = new ClanItemJson();
            newItem.setId(clanItemEntry.getId());
            newItem.setTime(now.plusHours(clanItemEntry.getTime()));
            updatedItems.add(newItem);
            items = updatedItems.toArray(new ClanItemJson[0]);
        }
        clanDao.updateClanItems(clanId, items);
    }

    public byte[] getClanIcon(short clanId) {
        return Utils.getFile("res/icon/clan/" + clanDao.getClanIcon(clanId) + ".png");
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
            clanDao.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " xu", playerId, quantity, 0);
        } else {
            clanDao.updateLuong(clanId, quantity);
            clanDao.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " lượng", playerId, 0, quantity);
        }
    }

    public List<ClanMemEntry> getMemberClan(short clanId, byte page) {
        return clanDao.getClanMember(clanId, page);
    }

    public ClanInfo getClanInfo(short clanId) {
        return clanDao.getClanInfo(clanId);
    }

    public byte getTotalPagesClan() {
        double count = clanDao.getCountClan();
        return (byte) Math.ceil(count / 10);
    }

    public List<ClanEntry> getTopTeams(byte page) {
        return clanDao.getTopTeams(page);
    }

    public boolean[] getClanItems(short clanId) {
        boolean[] result = new boolean[ClanItemData.CLAN_ITEM_ENTRY_MAP.size()];
        LocalDateTime now = LocalDateTime.now();
        ClanItemJson[] items = clanDao.getClanItems(clanId);

        for (ClanItemJson item : items) {
            if (item.getTime().isAfter(now)) {
                result[item.getId() - 1] = true;
            }
        }

        return result;
    }

}
