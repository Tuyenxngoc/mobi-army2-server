package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.ServerManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public abstract class BaseMessageHandler {
    protected final Session session;

    protected BaseMessageHandler(Session session) {
        this.session = session;
    }

    protected void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    protected User us() {
        return session.getUser();
    }

    protected FightWait fw() {
        return us() != null ? us().getFightWait() : null;
    }

    protected FightManager fm() {
        return fw() != null ? fw().getFightManager() : null;
    }

    public void sendServerMessage(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMoneyErrorMessage(String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMessageLoginFail(String message) {
        try {
            Message ms = new Message(Cmd.LOGIN_FAIL);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMessageToUser(String message) {
        sendMessageToUser(true, session.getUser(), message);
    }

    public void sendMessageToUser(boolean isAdminSender, User recipient, String message) {
        try {
            Message ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            if (isAdminSender) {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            } else {
                ds.writeInt(us().getUserId());
                ds.writeUTF(us().getUsername());
            }
            ds.writeUTF(message);
            ds.flush();
            recipient.sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendServerInfo(String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                ApplicationContext.getInstance()
                        .getBean(ServerManager.class).sendToServer(ms);
            } else {
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

    public void sendCharacterInfo() {
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(us().getCurrentLevel());
            ds.writeByte(us().getCurrentLevelPercent());
            ds.writeShort(us().getCurrentPoint());
            for (short point : us().getCurrentAddedPoints()) {
                ds.writeShort(point);
            }
            ds.writeInt(us().getCurrentXp());
            ds.writeInt(us().getCurrentRequiredXp());
            ds.writeInt(us().getCup());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendInventoryInfo() {
        try {
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            Map<Integer, EquipmentChest> equipmentChest = us().getEquipmentChest();
            ds.writeByte(equipmentChest.size());
            for (EquipmentChest equipment : equipmentChest.values()) {
                ds.writeInt(equipment.getKey());
                ds.writeByte(equipment.getEquipment().getCharacterId());
                ds.writeByte(equipment.getEquipment().getEquipType());
                ds.writeShort(equipment.getEquipment().getEquipIndex());
                ds.writeUTF(equipment.getEquipment().getName());
                ds.writeByte(equipment.getAddPoints().length * 2);
                for (int j = 0; j < equipment.getAddPoints().length; j++) {
                    ds.writeByte(equipment.getAddPoints()[j]);
                    ds.writeByte(equipment.getAddPercents()[j]);
                }
                ds.writeByte(equipment.getRemainingDays());
                ds.writeByte(equipment.getEmptySlot());
                ds.writeByte(equipment.getEquipment().isDisguise() ? 1 : 0);
                ds.writeByte(equipment.getVipLevel());
            }
            for (int i = 0; i < 5; i++) {
                ds.writeInt(us().getEquipData()[us().getActiveCharacterId()][i]);
            }
            ds.flush();
            sendMessage(ms);

            ms = new Message(Cmd.MATERIAL);
            ds = ms.writer();
            ds.writeByte(0);
            Map<Byte, SpecialItemChest> specialItemChest = us().getSpecialItemChest();
            ds.writeByte(specialItemChest.size());
            for (SpecialItemChest item : specialItemChest.values()) {
                ds.writeByte(item.getItem().getId());
                ds.writeShort(item.getQuantity());
                ds.writeUTF(item.getItem().getName());
                ds.writeUTF(item.getItem().getDetail());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendUpdateMoney() throws IOException {
        Message ms = new Message(Cmd.UPDATE_MONEY);
        DataOutputStream ds = ms.writer();
        ds.writeInt(us().getXu());
        ds.writeInt(us().getLuong());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateCup(int cupUp) throws IOException {
        Message ms = new Message(Cmd.CUP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(cupUp);
        ds.writeInt(us().getCup());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateXp(int xpUp, boolean updateLevel) throws IOException {
        Message ms = new Message(Cmd.UPDATE_EXP);
        DataOutputStream ds = ms.writer();
        ds.writeInt(xpUp);
        ds.writeInt(us().getCurrentXp());
        ds.writeInt(us().getCurrentRequiredXp());
        if (updateLevel) {
            ds.writeByte(1);
            ds.writeByte(us().getCurrentLevel());
            ds.writeByte(us().getCurrentLevelPercent());
            ds.writeShort(us().getCurrentPoint());
        } else {
            ds.writeByte(0);
            ds.writeByte(us().getCurrentLevelPercent());
        }
        ds.flush();
        sendMessage(ms);
    }
}
