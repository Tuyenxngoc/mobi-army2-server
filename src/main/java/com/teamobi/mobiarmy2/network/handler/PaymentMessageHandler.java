package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.GiftCodeDAO;
import com.teamobi.mobiarmy2.dao.UserGiftCodeDAO;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;
import com.teamobi.mobiarmy2.dto.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.dto.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.Payment;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.server.PaymentManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentMessageHandler extends BaseMessageHandler {
    private final GiftCodeDAO giftCodeDAO;
    private final UserGiftCodeDAO userGiftCodeDAO;

    public PaymentMessageHandler(Session session, GiftCodeDAO giftCodeDAO, UserGiftCodeDAO userGiftCodeDAO) {
        super(session);
        this.giftCodeDAO = giftCodeDAO;
        this.userGiftCodeDAO = userGiftCodeDAO;
    }

    public void handleCardRecharge(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        String type = dis.readUTF().trim();
        String serial = dis.readUTF().trim();
        String pin = dis.readUTF().trim();

        if (type.equals("giftcode") && !serial.isEmpty()) {
            handleGiftCode(serial);
            return;
        }
        us().sendServerMessage(serial + " " + pin);
    }

    private void handleGiftCode(String code) {
        GiftCodeDTO giftCode = giftCodeDAO.findById(code);
        if (giftCode == null) {
            us().sendServerMessage(GameString.GIFT_CODE_INVALID);
            return;
        }
        if (giftCode.getLimit() <= 0) {
            us().sendServerMessage(GameString.GIFT_CODE_LIMIT_REACHED);
            return;
        }
        if (giftCode.getExpiryDate() != null && LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            String formattedDate = Utils.formatLocalDateTime(giftCode.getExpiryDate());
            us().sendServerMessage(GameString.createGiftCodeExpiryMessage(formattedDate));
            return;
        }

        boolean existsByUserId = userGiftCodeDAO.existsByUserId(us().getUserId());
        if (existsByUserId) {
            us().sendServerMessage(GameString.GIFT_CODE_ALREADY_USED);
            return;
        }

        HikariCPManager hikariCPManager = ApplicationContext
                .getInstance()
                .getBean(HikariCPManager.class);
        boolean success = hikariCPManager.transaction(connection -> {
            giftCodeDAO.decrementUsageLimit(connection, giftCode.getGiftCodeId());
            userGiftCodeDAO.create(connection, giftCode.getGiftCodeId(), us().getUserId());
        });

        if (!success) {
            us().sendServerMessage(GameString.SERVER_ERROR);
            return;
        }

        if (giftCode.getXu() > 0) {
            us().updateXu(giftCode.getXu());
            us().sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getXu()) + " xu"));
        }
        if (giftCode.getLuong() > 0) {
            us().updateLuong(giftCode.getLuong());
            us().sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getLuong()) + " lượng"));
        }
        if (giftCode.getExp() > 0) {
            us().updateXp(giftCode.getExp());
            us().sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getExp()) + " exp"));
        }
        if (giftCode.getItems() != null) {
            List<SpecialItemChest> additionalItems = new ArrayList<>();
            for (SpecialItemChestJson item : giftCode.getItems()) {
                SpecialItemChest newItem = new SpecialItemChest();
                newItem.setItem(SpecialItemManager.getSpecialItemById(item.getId()));
                if (newItem.getItem() == null) {
                    continue;
                }
                newItem.setQuantity(item.getQuantity());
                additionalItems.add(newItem);
                us().sendMessageToUser(GameString.createGiftCodeRewardMessageWithQuantity(code, newItem.getQuantity(), newItem.getItem().getName()));
            }
            us().updateInventory(null, null, additionalItems, null);
        }
        if (giftCode.getEquips() != null) {
            for (EquipmentChestJson json : giftCode.getEquips()) {
                EquipmentChest addEquip = new EquipmentChest();
                addEquip.setEquipment(EquipmentManager.getEquipment(json.getEquipmentId()));
                if (addEquip.getEquipment() == null) {
                    continue;
                }
                addEquip.setAddPoints(json.getAddPoints());
                addEquip.setAddPercents(json.getAddPercents());
                us().addEquipment(addEquip);
                us().sendMessageToUser(GameString.createGiftCodeRewardMessage(code, addEquip.getEquipment().getName()));
            }
        }

        us().sendServerMessage(GameString.GIFT_CODE_SUCCESS);
    }

    public void rechargeMoney(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        switch (type) {
            case 0 -> {
                ms = new Message(Cmd.CHARGE_MONEY_2);
                DataOutputStream ds = ms.writer();
                ds.writeByte(0);
                for (Payment payment : PaymentManager.PAYMENT_MAP.values()) {
                    ds.writeUTF(payment.getId());
                    ds.writeUTF(payment.getInfo());
                    ds.writeUTF(payment.getUrl());
                }
                ds.flush();
                sendMessage(ms);
            }
            case 1 -> {
                String id = dis.readUTF();
                Payment payment = PaymentManager.PAYMENT_MAP.get(id);
                if (payment != null) {
                    ms = new Message(Cmd.CHARGE_MONEY_2);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(2);
                    ds.writeUTF(payment.getMssTo());
                    ds.writeUTF(payment.getMssContent());
                    ds.flush();
                    sendMessage(ms);
                }
            }
        }
    }
}
