package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.ServerManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public abstract class BaseMessageHandler {
    protected final Session session;
    protected User user;

    protected BaseMessageHandler(Session session) {
        this.session = session;
    }

    protected void sendMessage(Message ms) {
        session.sendMessage(ms);
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
                User user = session.getUser();
                ds.writeInt(user.getUserId());
                ds.writeUTF(user.getUsername());
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
        User user = session.getUser();
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getCurrentLevel());
            ds.writeByte(user.getCurrentLevelPercent());
            ds.writeShort(user.getCurrentPoint());
            for (short point : user.getCurrentAddedPoints()) {
                ds.writeShort(point);
            }
            ds.writeInt(user.getCurrentXp());
            ds.writeInt(user.getCurrentRequiredXp());
            ds.writeInt(user.getCup());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendInventoryInfo() {
        User user = session.getUser();
        try {
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            Map<Integer, EquipmentChest> equipmentChest = user.getEquipmentChest();
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
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            sendMessage(ms);

            ms = new Message(Cmd.MATERIAL);
            ds = ms.writer();
            ds.writeByte(0);
            Map<Byte, SpecialItemChest> specialItemChest = user.getSpecialItemChest();
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
}
