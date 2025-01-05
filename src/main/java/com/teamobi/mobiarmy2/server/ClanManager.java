package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IClanDAO;
import com.teamobi.mobiarmy2.dao.IClanMemberDAO;
import com.teamobi.mobiarmy2.dao.impl.ClanDAO;
import com.teamobi.mobiarmy2.dao.impl.ClanMemberDAO;
import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.model.ClanItem;
import com.teamobi.mobiarmy2.model.ClanItemShop;
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
    private final IClanDAO clanDAO;
    private final IClanMemberDAO clanMemberDAO;

    public ClanManager() {
        clanDAO = new ClanDAO();
        clanMemberDAO = new ClanMemberDAO();
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
        return clanDAO.getLevel(clanId);
    }

    public int getClanXu(short clanId) {
        return clanDAO.getXu(clanId);
    }

    public int getClanLuong(short clanId) {
        return clanDAO.getLuong(clanId);
    }

    public byte[] getClanIcon(short clanId) {
        return Utils.getFile(String.format(GameConstants.CLAN_ICON_PATH, clanDAO.getClanIcon(clanId)));
    }

    public byte getTotalPage(short clanId) {
        Byte mem = clanMemberDAO.count(clanId);
        if (mem == null) {
            return -1;
        }
        return (byte) Math.ceil((double) mem / 10);
    }

    public byte getTotalPagesClan() {
        double count = clanDAO.getCountClan();
        return (byte) Math.ceil(count / 10);
    }

    public ClanInfoDTO getClanInfo(short clanId) {
        return clanDAO.getClanInfo(clanId);
    }

    public List<ClanMemDTO> getMemberClan(short clanId, byte page) {
        return clanDAO.getClanMember(clanId, page);
    }

    public List<ClanDTO> getTopTeams(byte page) {
        return clanDAO.getTopTeams(page);
    }

    public boolean[] getClanItems(short clanId) {
        boolean[] result = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
        LocalDateTime now = LocalDateTime.now();
        ClanItem[] items = clanDAO.getClanItems(clanId);

        for (ClanItem item : items) {
            if (item.getTime().isAfter(now)) {
                result[item.getId() - 1] = true;
            }
        }

        return result;
    }

    public void updateItemClan(short clanId, int playerId, ClanItemShop clanItemShop, boolean isBuyXu) {
        synchronized (getClanLock(clanId)) {
            if (isBuyXu) {
                clanDAO.updateXu(clanId, -clanItemShop.getXu());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getXu()) + " xu", playerId, -clanItemShop.getXu(), 0);
            } else {
                clanDAO.updateLuong(clanId, -clanItemShop.getLuong());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getLuong()) + " lượng", playerId, 0, -clanItemShop.getLuong());
            }

            ClanItem[] items = clanDAO.getClanItems(clanId);
            boolean found = false;
            LocalDateTime now = LocalDateTime.now();

            for (ClanItem item : items) {
                if (item.getId() == clanItemShop.getId()) {
                    if (item.getTime().isBefore(now)) {
                        item.setTime(now);
                    }
                    item.setTime(item.getTime().plusHours(clanItemShop.getTime()));
                    found = true;
                    break;
                }
            }

            if (!found) {
                List<ClanItem> updatedItems = new ArrayList<>(Arrays.asList(items));
                ClanItem newItem = new ClanItem();
                newItem.setId(clanItemShop.getId());
                newItem.setTime(now.plusHours(clanItemShop.getTime()));
                updatedItems.add(newItem);
                items = updatedItems.toArray(new ClanItem[0]);
            }
            clanDAO.updateClanItems(clanId, items);
        }
    }

    public void contributeClan(short clanId, int playerId, int quantity, boolean isXu) {
        synchronized (getClanLock(clanId)) {
            if (isXu) {
                clanDAO.updateXu(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " xu", playerId, quantity, 0);
            } else {
                clanDAO.updateLuong(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " lượng", playerId, 0, quantity);
            }
        }
    }

    public void updateXp(short clanId, int playerId, int xpUp) {
        if (xpUp == 0) {
            return;
        }
        synchronized (getClanLock(clanId)) {
            int currentXp = clanDAO.getXp(clanId);
            long newXp = currentXp + xpUp;
            if (newXp > GameConstants.MAX_XP) {
                newXp = GameConstants.MAX_XP;
            } else if (newXp < GameConstants.MIN_XP) {
                newXp = GameConstants.MIN_XP;
            }

            int level = ClanXpManager.getLevelByXP((int) newXp);
            clanDAO.updateXp(clanId, playerId, (int) newXp, level);
            clanDAO.updateClanMemberPoints(playerId, xpUp);
        }
    }

    public void updateCup(short clanId, int playerId, int cupUp) {
        if (cupUp == 0) {
            return;
        }
        synchronized (getClanLock(clanId)) {
            int currentCup = clanDAO.getCup(clanId);
            long newCup = currentCup + cupUp;
            if (newCup > GameConstants.MAX_CUP) {
                newCup = GameConstants.MAX_CUP;
            } else if (newCup < GameConstants.MIN_CUP) {
                newCup = GameConstants.MIN_CUP;
            }

            clanDAO.updateCup(clanId, playerId, (int) newCup);
            clanDAO.updateClanMemberPoints(playerId, cupUp * 2);
        }
    }

}
