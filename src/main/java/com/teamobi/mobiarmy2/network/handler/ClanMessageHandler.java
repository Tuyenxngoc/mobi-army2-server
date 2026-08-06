package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dto.*;
import com.teamobi.mobiarmy2.entity.ClanItemShop;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.CacheManager;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.service.ClanService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ClanMessageHandler extends BaseMessageHandler {
    private static final int MIN_XU_CONTRIBUTE_CLAN = 1000;

    private final ClanService clanService;

    public ClanMessageHandler(Session session, MessageSender messageSender, ClanService clanService) {
        super(session, messageSender);
        this.clanService = clanService;
    }

    public void contributeToClan(Message ms) throws IOException {
        if (us().isNotWaiting()) {
            return;
        }
        if (!us().hasClan()) {
            return;
        }

        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        int quantity = dis.readInt();

        if (quantity <= 0) {
            return;
        }

        if (type == 0) {
            if (quantity > us().getXu()) {
                return;
            }

            if (quantity < MIN_XU_CONTRIBUTE_CLAN) {
                messageSender.sendServerMessage(us(), GameString.createClanContributionMinXuMessage(MIN_XU_CONTRIBUTE_CLAN));
                return;
            }

            //Update xu user
            us().updateXu(-quantity);

            //Update xu clan
            clanService.contributeToClan(us().getClanId(), us().getUserId(), quantity, true);
            messageSender.sendServerMessage(us(), GameString.CONTRIBUTION_SUCCESS);
        } else {
            if (quantity > us().getLuong()) {
                return;
            }

            //Update lg user
            us().updateLuong(-quantity);

            //Update lg clan
            clanService.contributeToClan(us().getClanId(), us().getUserId(), quantity, false);
            messageSender.sendServerMessage(us(), GameString.CONTRIBUTION_SUCCESS);
        }
    }

    public void handlePurchaseClanItem(Message ms) throws IOException {
        if (!us().hasClan()) {
            messageSender.sendServerMessage(us(), GameString.NO_CLAN_MEMBERSHIP);
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

        if (!clanService.canUnlockClanItem(us().getClanId(), clanItemShop)) {
            messageSender.sendServerMessage(us(), GameString.CLAN_LEVEL_INSUFFICIENT);
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemShop.getXu() < 0) {
                return;
            }
            if (!clanService.hasEnoughFundsForClanItem(us().getClanId(), clanItemShop, true)) {
                messageSender.sendServerMessage(us(), GameString.CLAN_NOT_ENOUGH_XU);
                return;
            }

            clanService.purchaseClanItem(us().getClanId(), us().getUserId(), clanItemShop, true);
        } else {//Luong
            if (clanItemShop.getLuong() < 0) {
                return;
            }

            if (!clanService.hasEnoughFundsForClanItem(us().getClanId(), clanItemShop, false)) {
                messageSender.sendServerMessage(us(), GameString.CLAN_NOT_ENOUGH_LUONG);
                return;
            }

            clanService.purchaseClanItem(us().getClanId(), us().getUserId(), clanItemShop, false);
        }
        messageSender.sendServerMessage(us(), GameString.PURCHASE_SUCCESS);
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
        byte requestedPage = ms.reader().readByte();

        TopClanResultDTO result = clanService.getTopClan(requestedPage);
        byte page = result.getPage();
        List<ClanDTO> topClan = result.getClans();

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
        byte requestedPage = dis.readByte();
        short clanId = dis.readShort();

        ClanMembersResultDTO result = clanService.getClanMembers(clanId, requestedPage);
        if (result == null) {
            return;// not found or invalid
        }
        byte page = result.getPage();
        List<ClanMemDTO> clanMemDTO = result.getMembers();

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
        byte[] data = clanService.getClanIconBytes(clanId);
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
        ClanInfoDTO clanDetails = clanService.getClanInfo(clanId);
        if (clanDetails == null) {
            messageSender.sendServerMessage(us(), GameString.CLAN_NOT_FOUND);
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
