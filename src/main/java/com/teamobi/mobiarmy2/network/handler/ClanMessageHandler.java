package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameConstants;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.ClanDAO;
import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.dto.ClanItemDTO;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.entity.ClanItemShop;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.CacheManager;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.server.ClanXpManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClanMessageHandler extends BaseMessageHandler {
    private static final ConcurrentHashMap<Short, Object> clanLocks = new ConcurrentHashMap<>();
    private final ClanDAO clanDAO;

    public ClanMessageHandler(Session session, ClanDAO clanDAO) {
        super(session);
        this.clanDAO = clanDAO;
    }

    private Object getClanLock(short clanId) {
        return clanLocks.computeIfAbsent(clanId, k -> new Object());
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

    public void updateItemClan(short clanId, int userId, ClanItemShop clanItemShop, boolean isBuyXu) {
        synchronized (getClanLock(clanId)) {
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
        }
    }

    public void contributeClan(short clanId, int userId, int quantity, boolean isXu) {
        synchronized (getClanLock(clanId)) {
            if (isXu) {
                clanDAO.updateXu(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " xu", userId, quantity, 0);
            } else {
                clanDAO.updateLuong(clanId, quantity);
                clanDAO.gopClanContribute("Góp " + Utils.getStringNumber(quantity) + " lượng", userId, 0, quantity);
            }
        }
    }

    public void updateXp(short clanId, int userId, int xpUp) {
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
            clanDAO.updateXp(clanId, userId, (int) newXp, level);
            clanDAO.updateClanMemberPoints(userId, xpUp);
        }
    }

    public void updateCup(short clanId, int userId, int cupUp) {
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

            clanDAO.updateCup(clanId, userId, (int) newCup);
            clanDAO.updateClanMemberPoints(userId, cupUp * 2);
        }
    }

    public void contributeToClan(Message ms) throws IOException {
        if (user.isNotWaiting()) {
            return;
        }
        if (user.getClanId() == null) {
            return;
        }

        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        int quantity = dis.readInt();

        if (quantity <= 0) {
            return;
        }

        if (type == 0) {
            if (quantity > user.getXu()) {
                return;
            }

            int minXuContributeClan = ApplicationContext.getInstance().getBean(ServerConfig.class).getMinXuContributeClan();
            if (quantity < minXuContributeClan) {
                sendServerMessage(GameString.createClanContributionMinXuMessage(minXuContributeClan));
                return;
            }

            //Update xu user
            user.updateXu(-quantity);

            //Update xu clan
            contributeClan(user.getClanId(), user.getUserId(), quantity, Boolean.TRUE);
            sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
        } else {
            if (quantity > user.getLuong()) {
                return;
            }

            //Update lg user
            user.updateLuong(-quantity);

            //Update lg clan
            contributeClan(user.getClanId(), user.getUserId(), quantity, Boolean.FALSE);
            sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
        }
    }

    public void handlePurchaseClanItem(Message ms) throws IOException {
        if (user.getClanId() == null) {
            sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
            return;
        }
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        if (type == 0) {
            sendClanShop();
        } else {
            byte unit = dis.readByte();
            byte itemId = dis.readByte();
            buyClanShop(unit, itemId);
        }
    }

    private void buyClanShop(byte unit, byte itemId) {
        ClanItemShop clanItemShop = ClanItemManager.getItemClanById(itemId);

        if (clanItemShop == null || clanItemShop.getOnSale() != 1) {
            return;
        }

        int currentLevel = clanDAO.getLevel(user.getClanId());
        if (currentLevel < clanItemShop.getLevel()) {
            sendServerMessage(GameString.CLAN_LEVEL_INSUFFICIENT);
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemShop.getXu() < 0) {
                return;
            }
            int xuClan = clanDAO.getXu(user.getClanId());
            if (xuClan < clanItemShop.getXu()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_XU);
                return;
            }

            updateItemClan(user.getClanId(), user.getUserId(), clanItemShop, true);
        } else {//Luong
            if (clanItemShop.getLuong() < 0) {
                return;
            }
            int luongClan = clanDAO.getLuong(user.getClanId());
            if (luongClan < clanItemShop.getLuong()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_LUONG);
                return;
            }

            updateItemClan(user.getClanId(), user.getUserId(), clanItemShop, false);
        }
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    private void sendClanShop() throws IOException {
        Message ms = CacheManager.cachedClanItemShop;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        ms = new Message(Cmd.SHOP_BIETDOI);
        DataOutputStream ds = ms.writer();
        ds.writeByte(ClanItemManager.CLAN_ITEM_MAP.size());
        for (ClanItemShop clanItemShop : ClanItemManager.CLAN_ITEM_MAP.values()) {
            ds.writeByte(clanItemShop.getId());
            ds.writeUTF(clanItemShop.getName());
            ds.writeInt(clanItemShop.getXu());
            ds.writeInt(clanItemShop.getLuong());
            ds.writeByte(clanItemShop.getTime());
            ds.writeByte(clanItemShop.getLevel());
        }
        ds.flush();

        CacheManager.cachedClanItemShop = ms;

        sendMessage(ms);
    }

    public void getTopClan(Message ms) throws IOException {
        byte page = ms.reader().readByte();

        double count = clanDAO.getCountClan();
        byte totalPages = (byte) Math.ceil(count / 10);
        if (page > totalPages) {
            page = 0;
        }

        List<ClanDTO> topClan = clanDAO.getTopTeams(page);
        ms = new Message(Cmd.TOP_CLAN);
        DataOutputStream ds = ms.writer();
        ds.writeByte(page);
        for (int i = 0; i < topClan.size(); i++) {
            ClanDTO clan = topClan.get(i);
            ds.writeShort(clan.getClanId());
            ds.writeUTF(String.format("#%d: %s", i + 1, clan.getName()));
            ds.writeByte(clan.getMemberCount());
            ds.writeByte(clan.getMaxMemberCount());
            ds.writeUTF(clan.getMasterName());
            ds.writeInt(clan.getXu());
            ds.writeInt(clan.getLuong());
            ds.writeInt(clan.getCup());
            ds.writeByte(clan.getLevel());
            ds.writeByte(clan.getLevelPercentage());
            ds.writeUTF(clan.getDescription());
        }
        ds.flush();
        sendMessage(ms);
    }

    public void getClanMember(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte page = dis.readByte();
        short clanId = dis.readShort();

        Byte memberCount = clanDAO.getMembersOfClan(clanId);
        if (memberCount == null) {
            return;
        }
        byte totalPage = (byte) Math.ceil((double) memberCount / 10);
        if (page >= totalPage) {
            page = 0;
        }
        if (page < 0) {
            page = (byte) (totalPage - 1);
        }

        List<ClanMemDTO> clanMemDTO = clanDAO.getClanMember(clanId, page);

        ms = new Message(Cmd.CLAN_MEMBER);
        DataOutputStream ds = ms.writer();
        ds.writeByte(page);
        ds.writeUTF("BIỆT ĐỘI");
        for (ClanMemDTO memClan : clanMemDTO) {
            ds.writeInt(memClan.getUserId());
            ds.writeUTF(memClan.getUsername());
            ds.writeInt(memClan.getPoint());
            ds.writeByte(memClan.getActiveCharacter());
            ds.writeByte(memClan.getOnline());
            ds.writeByte(memClan.getLevel());
            ds.writeByte(memClan.getLevelPt());
            ds.writeByte(memClan.getIndex());
            ds.writeInt(memClan.getCup());
            for (int j = 0; j < 5; j++) {
                ds.writeShort(memClan.getDataEquip()[j]);
            }
            ds.writeUTF(memClan.getContributeText());
            ds.writeUTF(memClan.getContributeCount());
        }
        ds.flush();
        sendMessage(ms);
    }

    public void getClanIcon(Message ms) throws IOException {
        short clanId = ms.reader().readShort();
        byte[] data = Utils.getFile(String.format(GameConstants.CLAN_ICON_PATH, clanDAO.getClanIcon(clanId)));
        if (data == null) {
            return;
        }
        ms = new Message(Cmd.CLAN_ICON);
        DataOutputStream ds = ms.writer();
        ds.writeShort(clanId);
        ds.writeShort(data.length);
        ds.write(data);
        ds.flush();
        sendMessage(ms);
    }

    public void getInfoClan(Message ms) throws IOException {
        short clanId = ms.reader().readShort();
        ClanInfoDTO clanDetails = clanDAO.getClanInfo(clanId);
        if (clanDetails == null) {
            sendMessageLoginFail(GameString.CLAN_NOT_FOUND);
            return;
        }
        ms = new Message(Cmd.CLAN_INFO);
        DataOutputStream ds = ms.writer();
        ds.writeShort(clanDetails.getClanId());
        ds.writeUTF(clanDetails.getName());
        ds.writeByte(clanDetails.getMemberCount());
        ds.writeByte(clanDetails.getMaxMemberCount());
        ds.writeUTF(clanDetails.getMasterName());
        ds.writeInt(clanDetails.getXu());
        ds.writeInt(clanDetails.getLuong());
        ds.writeInt(clanDetails.getCup());
        ds.writeInt(clanDetails.getExp());
        ds.writeInt(clanDetails.getXpUpLevel());
        ds.writeByte(clanDetails.getLevel());
        ds.writeByte(clanDetails.getLevelPercentage());
        ds.writeUTF(clanDetails.getDescription());
        ds.writeUTF(clanDetails.getCreatedDate());
        ds.writeByte(clanDetails.getItems().size());
        for (ClanItemDTO item : clanDetails.getItems()) {
            ds.writeUTF(item.getName());
            ds.writeInt(item.getTime());
        }
        ds.flush();
        sendMessage(ms);
    }
}
