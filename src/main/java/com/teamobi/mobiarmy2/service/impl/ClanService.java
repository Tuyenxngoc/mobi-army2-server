package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IClanDAO;
import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.model.ClanItemShop;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.server.ClanXpManager;
import com.teamobi.mobiarmy2.service.IClanService;
import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tuyen
 */
public class ClanService implements IClanService {
    private final ConcurrentHashMap<Short, Object> clanLocks = new ConcurrentHashMap<>();
    private final IClanDAO clanDAO;

    public ClanService(IClanDAO clanDAO) {
        this.clanDAO = clanDAO;
    }

    private Object getClanLock(short clanId) {
        return clanLocks.computeIfAbsent(clanId, k -> new Object());
    }

    @Override
    public int getClanLevel(short clanId) {
        return clanDAO.getLevel(clanId);
    }

    @Override
    public int getClanXu(short clanId) {
        return clanDAO.getXu(clanId);
    }

    @Override
    public int getClanLuong(short clanId) {
        return clanDAO.getLuong(clanId);
    }

    @Override
    public byte[] getClanIcon(short clanId) {
        return Utils.getFile(String.format(GameConstants.CLAN_ICON_PATH, clanDAO.getClanIcon(clanId)));
    }

    @Override
    public byte getTotalPage(short clanId) {
        Byte mem = clanDAO.getMembersOfClan(clanId);
        if (mem == null) {
            return -1;
        }
        return (byte) Math.ceil((double) mem / 10);
    }

    @Override
    public byte getTotalPagesClan() {
        double count = clanDAO.getCountClan();
        return (byte) Math.ceil(count / 10);
    }

    @Override
    public ClanInfoDTO getClanInfo(short clanId) {
        return clanDAO.getClanInfo(clanId);
    }

    @Override
    public List<ClanMemDTO> getMemberClan(short clanId, byte page) {
        return clanDAO.getClanMember(clanId, page);
    }

    @Override
    public List<ClanDTO> getTopTeams(byte page) {
        return clanDAO.getTopTeams(page);
    }

    @Override
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

    @Override
    public void updateItemClan(short clanId, int playerId, ClanItemShop clanItemShop, boolean isBuyXu) {
        synchronized (getClanLock(clanId)) {
            if (isBuyXu) {
                clanDAO.updateXu(clanId, -clanItemShop.getXu());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getXu()) + " xu", playerId, -clanItemShop.getXu(), 0);
            } else {
                clanDAO.updateLuong(clanId, -clanItemShop.getLuong());
                clanDAO.gopClanContribute("Mua item đội -" + Utils.getStringNumber(clanItemShop.getLuong()) + " lượng", playerId, 0, -clanItemShop.getLuong());
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
        }
    }

    @Override
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

    @Override
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

    @Override
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
