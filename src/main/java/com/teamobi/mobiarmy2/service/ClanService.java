package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.ClanDAO;
import com.teamobi.mobiarmy2.dto.*;
import com.teamobi.mobiarmy2.entity.ClanItemShop;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.server.ClanXpManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ClanService {

    private static final ConcurrentHashMap<Short, ReentrantLock> clanLocks = new ConcurrentHashMap<>();
    private final ClanDAO clanDAO;

    public ClanService(ClanDAO clanDAO) {
        this.clanDAO = clanDAO;
    }

    private ReentrantLock getClanLock(short clanId) {
        return clanLocks.computeIfAbsent(clanId, k -> new ReentrantLock());
    }

    public void updateXp(short clanId, int userId, int xpUp) {
        if (xpUp == 0) {
            return;
        }

        ReentrantLock lock = getClanLock(clanId);
        lock.lock();
        try {
            int currentXp = clanDAO.getXp(clanId);
            long newXp = currentXp + xpUp;
            if (newXp > GameConstants.MAX_XP) {
                newXp = GameConstants.MAX_XP;
            } else if (newXp < GameConstants.MIN_XP) {
                newXp = GameConstants.MIN_XP;
            }

            int level = ClanXpManager.getLevelByXP((int) newXp);
            clanDAO.updateXp(clanId, userId, (int) newXp, level);
            clanDAO.updateClanMemberPoints(userId, xpUp);
        } finally {
            lock.unlock();
        }
    }

    public void updateCup(short clanId, int userId, int cupUp) {
        if (cupUp == 0) {
            return;
        }

        ReentrantLock lock = getClanLock(clanId);
        lock.lock();
        try {
            int currentCup = clanDAO.getCup(clanId);
            long newCup = currentCup + cupUp;
            if (newCup > GameConstants.MAX_CUP) {
                newCup = GameConstants.MAX_CUP;
            } else if (newCup < GameConstants.MIN_CUP) {
                newCup = GameConstants.MIN_CUP;
            }

            clanDAO.updateCup(clanId, userId, (int) newCup);
            clanDAO.updateClanMemberPoints(userId, cupUp * 2);
        } finally {
            lock.unlock();
        }
    }

    public boolean[] getClanItems(short clanId) {
        boolean[] result = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
        LocalDateTime now = LocalDateTime.now();
        ClanItemJson[] items = clanDAO.getClanItems(clanId);

        for (ClanItemJson item : items) {
            if (item.getTime().isAfter(now)) {
                result[item.getId() - 1] = true;
            }
        }

        return result;
    }

    public void purchaseClanItem(short clanId, int userId, ClanItemShop clanItemShop, boolean isBuyXu) {
        ReentrantLock lock = getClanLock(clanId);
        lock.lock();
        try {
            if (isBuyXu) {
                clanDAO.updateXu(clanId, -clanItemShop.getXu());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getXu()) + " xu", userId, -clanItemShop.getXu(), 0);
            } else {
                clanDAO.updateLuong(clanId, -clanItemShop.getLuong());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getLuong()) + " lượng", userId, 0, -clanItemShop.getLuong());
            }

            ClanItemJson[] items = clanDAO.getClanItems(clanId);
            boolean found = false;
            LocalDateTime now = LocalDateTime.now();

            for (ClanItemJson item : items) {
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
                List<ClanItemJson> updatedItems = new ArrayList<>(Arrays.asList(items));
                ClanItemJson newItem = new ClanItemJson();
                newItem.setId(clanItemShop.getId());
                newItem.setTime(now.plusHours(clanItemShop.getTime()));
                updatedItems.add(newItem);
                items = updatedItems.toArray(new ClanItemJson[0]);
            }
            clanDAO.updateClanItems(clanId, items);
        } finally {
            lock.unlock();
        }
    }

    public void contributeToClan(short clanId, int userId, int quantity, boolean isXu) {
        ReentrantLock lock = getClanLock(clanId);
        lock.lock();
        try {
            if (isXu) {
                clanDAO.updateXu(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " xu", userId, quantity, 0);
            } else {
                clanDAO.updateLuong(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " lượng", userId, 0, quantity);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean canUnlockClanItem(short clanId, ClanItemShop item) {
        int currentLevel = clanDAO.getLevel(clanId);
        return currentLevel >= item.getLevel();
    }

    public boolean hasEnoughFundsForClanItem(short clanId, ClanItemShop clanItemShop, boolean isBuyXu) {
        if (isBuyXu) {
            int xuClan = clanDAO.getXu(clanId);
            return xuClan >= clanItemShop.getXu();
        } else {
            int luongClan = clanDAO.getLuong(clanId);
            return luongClan >= clanItemShop.getLuong();
        }
    }

    public byte[] getClanIconBytes(short clanId) {
        short iconFile = clanDAO.getClanIcon(clanId);
        return Utils.getFile(String.format(GameConstants.CLAN_ICON_PATH, iconFile));
    }

    public ClanInfoDTO getClanInfo(short clanId) {
        return clanDAO.getClanInfo(clanId);
    }

    public TopClanResultDTO getTopClan(byte requestedPage) {
        short count = clanDAO.getCountClan();

        byte page = normalizePage(requestedPage, count, 10);

        List<ClanDTO> topClan = clanDAO.getTopTeams(page);
        return new TopClanResultDTO(page, topClan);
    }

    public ClanMembersResultDTO getClanMembers(short clanId, byte requestedPage) {
        Byte count = clanDAO.getMembersOfClan(clanId);
        if (count == null) return null;

        byte page = normalizePage(requestedPage, count, 10);

        List<ClanMemDTO> members = clanDAO.getClanMember(clanId, page);
        return new ClanMembersResultDTO(page, members);
    }

    private byte normalizePage(byte requestedPage, int totalItems, int pageSize) {
        byte totalPages = (byte) Math.max(1, Math.ceil((double) totalItems / pageSize));
        if (requestedPage < 0) return (byte) (totalPages - 1);
        if (requestedPage >= totalPages) return 0;
        return requestedPage;
    }

}
