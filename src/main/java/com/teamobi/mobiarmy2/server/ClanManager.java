package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.dao.impl.ClanDao;
import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.model.ClanItem;
import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tuyen
 */
public class ClanManager {
    private final ConcurrentHashMap<Short, Object> clanLocks = new ConcurrentHashMap<>();
    private final IClanDao clanDao;

    public ClanManager() {
        clanDao = new ClanDao();
    }

    private static class SingletonHelper {
        private static final ClanManager INSTANCE = new ClanManager();
    }

    public static ClanManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private Object getClanLock(short clanId) {
        return clanLocks.computeIfAbsent(clanId, k -> new Object());
    }

    public int getClanLevel(short clanId) {
        return clanDao.getLevel(clanId);
    }

    public int getClanXu(short clanId) {
        return clanDao.getXu(clanId);
    }

    public int getClanLuong(short clanId) {
        return clanDao.getLuong(clanId);
    }

    public byte[] getClanIcon(short clanId) {
        return Utils.getFile(String.format(GameConstants.CLAN_ICON_PATH, clanDao.getClanIcon(clanId)));
    }

    public byte getTotalPage(short clanId) {
        Byte mem = clanDao.getMembersOfClan(clanId);
        if (mem == null) {
            return -1;
        }
        return (byte) Math.ceil((double) mem / 10);
    }

    public byte getTotalPagesClan() {
        double count = clanDao.getCountClan();
        return (byte) Math.ceil(count / 10);
    }

    public ClanInfoDTO getClanInfo(short clanId) {
        return clanDao.getClanInfo(clanId);
    }

    public List<ClanMemDTO> getMemberClan(short clanId, byte page) {
        return clanDao.getClanMember(clanId, page);
    }

    public List<ClanDTO> getTopTeams(byte page) {
        return clanDao.getTopTeams(page);
    }

    public boolean[] getClanItems(short clanId) {
        boolean[] result = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
        LocalDateTime now = LocalDateTime.now();
        ClanItemJson[] items = clanDao.getClanItems(clanId);

        for (ClanItemJson item : items) {
            if (item.getTime().isAfter(now)) {
                result[item.getId() - 1] = true;
            }
        }

        return result;
    }

    public void updateItemClan(short clanId, int playerId, ClanItem clanItem, boolean isBuyXu) {
        synchronized (getClanLock(clanId)) {
            if (isBuyXu) {
                clanDao.updateXu(clanId, -clanItem.getXu());
                clanDao.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItem.getXu()) + " xu", playerId, -clanItem.getXu(), 0);
            } else {
                clanDao.updateLuong(clanId, -clanItem.getLuong());
                clanDao.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItem.getLuong()) + " lượng", playerId, 0, -clanItem.getLuong());
            }

            ClanItemJson[] items = clanDao.getClanItems(clanId);
            boolean found = false;
            LocalDateTime now = LocalDateTime.now();

            for (ClanItemJson item : items) {
                if (item.getId() == clanItem.getId()) {
                    if (item.getTime().isBefore(now)) {
                        item.setTime(now);
                    }
                    item.setTime(item.getTime().plusHours(clanItem.getTime()));
                    found = true;
                    break;
                }
            }

            if (!found) {
                List<ClanItemJson> updatedItems = new ArrayList<>(Arrays.asList(items));
                ClanItemJson newItem = new ClanItemJson();
                newItem.setId(clanItem.getId());
                newItem.setTime(now.plusHours(clanItem.getTime()));
                updatedItems.add(newItem);
                items = updatedItems.toArray(new ClanItemJson[0]);
            }
            clanDao.updateClanItems(clanId, items);
        }
    }

    public void contributeClan(short clanId, int playerId, int quantity, boolean isXu) {
        synchronized (getClanLock(clanId)) {
            if (isXu) {
                clanDao.updateXu(clanId, quantity);
                clanDao.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " xu", playerId, quantity, 0);
            } else {
                clanDao.updateLuong(clanId, quantity);
                clanDao.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " lượng", playerId, 0, quantity);
            }
        }
    }

    public void updateXp(short clanId, int playerId, int xpUp) {
        if (xpUp == 0) {
            return;
        }
        synchronized (getClanLock(clanId)) {
            int currentXp = clanDao.getXp(clanId);
            long newXp = currentXp + xpUp;
            if (newXp > GameConstants.MAX_XP) {
                newXp = GameConstants.MAX_XP;
            } else if (newXp < GameConstants.MIN_XP) {
                newXp = GameConstants.MIN_XP;
            }

            int level = ClanXpManager.getLevelByXP((int) newXp);
            clanDao.updateXp(clanId, playerId, (int) newXp, level);
            clanDao.updateClanMemberPoints(playerId, xpUp);
        }
    }

    public void updateCup(short clanId, int playerId, int cupUp) {
        if (cupUp == 0) {
            return;
        }
        synchronized (getClanLock(clanId)) {
            int currentCup = clanDao.getCup(clanId);
            long newCup = currentCup + cupUp;
            if (newCup > GameConstants.MAX_CUP) {
                newCup = GameConstants.MAX_CUP;
            } else if (newCup < GameConstants.MIN_CUP) {
                newCup = GameConstants.MIN_CUP;
            }

            clanDao.updateCup(clanId, playerId, (int) newCup);
            clanDao.updateClanMemberPoints(playerId, cupUp * 2);
        }
    }

}
