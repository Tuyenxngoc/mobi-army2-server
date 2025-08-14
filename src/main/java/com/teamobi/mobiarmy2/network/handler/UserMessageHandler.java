package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.constant.UserState;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.Payment;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserMessageHandler extends BaseMessageHandler {
    private final ServerConfig serverConfig;
    private final LeaderboardService leaderboardService;
    private final LoginRateLimiterService loginRateLimiterService;

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final GiftCodeDAO giftCodeDAO;
    private final UserGiftCodeDAO userGiftCodeDAO;
    private final UserCharacterDAO userCharacterDAO;

    private long lastSpinTime;

    public UserMessageHandler(
            Session session,
            ServerConfig serverConfig,
            LeaderboardService leaderboardService,
            LoginRateLimiterService loginRateLimiterService,
            UserDAO userDAO, AccountDAO accountDAO,
            GiftCodeDAO giftCodeDAO, UserGiftCodeDAO userGiftCodeDAO,
            UserCharacterDAO userCharacterDAO) {
        super(session);
        this.serverConfig = serverConfig;
        this.leaderboardService = leaderboardService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.giftCodeDAO = giftCodeDAO;
        this.userGiftCodeDAO = userGiftCodeDAO;
        this.userCharacterDAO = userCharacterDAO;
    }

    public void handleLogout() {
        if (user.getState() == UserState.FIGHTING || user.getState() == UserState.WAIT_FIGHT) {
            user.getFightWait().leaveTeam(user.getUserId());
        }

        //Cập nhật thông tin tài khoản
        userDAO.update(user);

        //Cập nhật thông tin nhân vật
        List<UserCharacterDTO> userCharacterDTOS = new ArrayList<>();
        for (byte i = 0; i < user.getOwnedCharacters().length; i++) {
            if (user.getOwnedCharacters()[i]) {
                UserCharacterDTO userCharacterDTO = getUserCharacterDTO(i);
                userCharacterDTOS.add(userCharacterDTO);
            }
        }
        userCharacterDAO.updateAll(userCharacterDTOS);

        user.setLogged(false);

        //Lưu thời gian đăng xuất gần nhất
        loginRateLimiterService.saveLogoutTime(user.getUsername());
    }

    private UserCharacterDTO getUserCharacterDTO(byte i) {
        UserCharacterDTO userCharacterDTO = new UserCharacterDTO();
        userCharacterDTO.setCharacterId(i);
        userCharacterDTO.setUserId(user.getUserId());
        userCharacterDTO.setLevel(user.getLevels()[i]);
        userCharacterDTO.setXp(user.getXps()[i]);
        userCharacterDTO.setPoints(user.getPoints()[i]);
        userCharacterDTO.setAdditionalPoints(user.getAddedPoints()[i]);
        userCharacterDTO.setData(user.getEquipData()[i]);
        return userCharacterDTO;
    }

    public void openLuckyGift(Message ms) throws IOException {
        byte index = ms.reader().readByte();
        user.getGiftBoxService().openGiftBoxAfterFight(index);
    }

    public void viewLeaderboard(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte type = dis.readByte();
        byte page = dis.readByte();

        if (type >= LeaderboardService.CATEGORIES.length) {
            return;
        }
        ms = new Message(Cmd.BANGTHANHTICH);
        DataOutputStream ds = ms.writer();
        ds.writeByte(type);
        if (type < 0) {
            ds.writeByte(LeaderboardService.CATEGORIES.length);
            for (String name : LeaderboardService.CATEGORIES) {
                ds.writeUTF(name);
            }
        } else {
            //Kiểm tra page num
            int maxPage = leaderboardService.getTotalPageByType(type);
            if (page > maxPage || page >= 10) {
                page = 0;
            }
            if (page < 0) {
                page = (byte) maxPage;
            }
            //Gửi dữ liệu
            ds.writeByte(page);
            ds.writeUTF(LeaderboardService.LABELS[type]);
            List<UserLeaderboardDTO> bangXH = leaderboardService.getUsers(type, page, 10);
            if (bangXH != null) {
                for (UserLeaderboardDTO pl : bangXH) {
                    ds.writeInt(pl.getUserId());
                    ds.writeUTF(pl.getUsername());
                    ds.writeByte(pl.getActiveCharacter());
                    ds.writeShort(pl.getClanId());
                    ds.writeByte(pl.getLevel());
                    ds.writeByte(pl.getLevelPt());
                    ds.writeByte(pl.getIndex());
                    for (short i : pl.getData()) {
                        ds.writeShort(i);
                    }
                    ds.writeUTF(pl.getDetail());
                }
            }
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleChoseCharacter(Message ms) throws IOException {
        byte characterId = ms.reader().readByte();
        if (characterId >= CharacterManager.CHARACTERS.size() || characterId < 0 || !user.getOwnedCharacters()[characterId]) {
            return;
        }
        user.setActiveCharacterId(characterId);

        ms = new Message(Cmd.CHOOSE_GUN);
        DataOutputStream ds = ms.writer();
        ds.writeInt(user.getUserId());
        ds.writeByte(characterId);
        ds.flush();
        sendMessage(ms);

        sendCharacterInfo();
        sendEquipInfo();
    }

    private void sendEquipInfo() throws IOException {
        Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
        DataOutputStream ds = ms.writer();
        for (int i = 0; i < 5; i++) {
            ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
        }
        ds.flush();
        sendMessage(ms);
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
        sendServerMessage(serial + " " + pin);
    }

    private void handleGiftCode(String code) {
        GiftCodeDTO giftCode = giftCodeDAO.findById(code);
        if (giftCode == null) {
            sendServerMessage(GameString.GIFT_CODE_INVALID);
            return;
        }
        if (giftCode.getLimit() <= 0) {
            sendServerMessage(GameString.GIFT_CODE_LIMIT_REACHED);
            return;
        }
        if (giftCode.getExpiryDate() != null && LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            String formattedDate = Utils.formatLocalDateTime(giftCode.getExpiryDate());
            sendServerMessage(GameString.createGiftCodeExpiryMessage(formattedDate));
            return;
        }

        boolean existsByUserId = userGiftCodeDAO.existsByUserId(user.getUserId());
        if (existsByUserId) {
            sendServerMessage(GameString.GIFT_CODE_ALREADY_USED);
            return;
        }

        giftCodeDAO.decrementUsageLimit(giftCode.getGiftCodeId());
        userGiftCodeDAO.create(giftCode.getGiftCodeId(), user.getUserId());

        if (giftCode.getXu() > 0) {
            user.updateXu(giftCode.getXu());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getXu()) + " xu"));
        }
        if (giftCode.getLuong() > 0) {
            user.updateLuong(giftCode.getLuong());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getLuong()) + " lượng"));
        }
        if (giftCode.getExp() > 0) {
            user.updateXp(giftCode.getExp());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getExp()) + " exp"));
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
                sendMessageToUser(GameString.createGiftCodeRewardMessageWithQuantity(code, newItem.getQuantity(), newItem.getItem().getName()));
            }
            user.updateInventory(null, null, additionalItems, null);
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
                user.addEquipment(addEquip);
                sendMessageToUser(GameString.createGiftCodeRewardMessage(code, addEquip.getEquipment().getName()));
            }
        }

        sendServerMessage(GameString.GIFT_CODE_SUCCESS);
    }

    public void handleChangePassword(Message ms) throws IOException {
        DataInputStream dis = ms.reader();

        String oldPass = dis.readUTF().trim();
        String newPass = dis.readUTF().trim();

        if (Utils.isAlphanumeric(oldPass) || Utils.isAlphanumeric(newPass)) {
            sendServerMessage(GameString.PASSWORD_INVALID_CHARACTER);
            return;
        }

        if (!accountDAO.existsByAccountIdAndPassword(user.getAccountId(), oldPass)) {
            sendServerMessage(GameString.PASSWORD_INCORRECT_OLD);
            return;
        }

        accountDAO.changePassword(user.getAccountId(), newPass);
        sendServerMessage(GameString.PASSWORD_CHANGE_SUCCESS);
    }

    public void handleAddPoints(Message ms) throws IOException {
        short[] points = new short[5];
        int totalPoints = 0;
        for (int i = 0; i < points.length; i++) {
            points[i] = ms.reader().readShort();
            if (points[i] < 0) {
                return;
            }
            totalPoints += points[i];
        }
        if (totalPoints <= user.getCurrentPoint()) {
            user.updatePoints(points, totalPoints);
        }

        sendCharacterInfo();
    }

    public void handleChangeEquipment(Message ms) throws IOException {
        boolean changeSuccessful = false;
        for (int i = 0; i < 5; i++) {
            int key = ms.reader().readInt();
            EquipmentChest equip = user.getEquipmentByKey(key);
            if (equip == null ||
                    equip.isInUse() ||
                    equip.isExpired() ||
                    equip.getEquipment().isDisguise() ||
                    equip.getEquipment().getLevelRequirement() > user.getCurrentLevel() ||
                    equip.getEquipment().getCharacterId() != user.getActiveCharacterId() || equip.getEquipment().getEquipType() != i
            ) {
                continue;
            }
            EquipmentChest oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][i];
            if (oldEquip != null) {
                oldEquip.setInUse(false);
            }
            equip.setInUse(true);
            user.getCharacterEquips()[user.getActiveCharacterId()][i] = equip;
            user.getEquipData()[user.getActiveCharacterId()][i] = equip.getKey();
            changeSuccessful = true;
        }
        ms = new Message(Cmd.CHANGE_EQUIP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(changeSuccessful ? 1 : 0);
        ds.flush();
        sendMessage(ms);
    }

    public void handleSpinWheel(Message ms) throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }

        byte unit = ms.reader().readByte();
        if (unit == 0) {
            if (user.getXu() < serverConfig.getSpinXuCost()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-serverConfig.getSpinXuCost());
        } else {
            if (user.getLuong() < serverConfig.getSpinLuongCost()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-serverConfig.getSpinLuongCost());
        }
        ms = new Message(Cmd.RULET);
        DataOutputStream ds = ms.writer();
        int luckyIndex = Utils.nextInt(10);
        for (byte i = 0; i < 10; i++) {
            byte type = (byte) Utils.nextInt(serverConfig.getSpinTypeProbabilities());
            byte itemId = 0;
            int quantity = 0;

            switch (type) {
                case 0 -> {
                    itemId = FightItemManager.getRandomItem();
                    quantity = serverConfig.getSpinItemCounts()[0][Utils.nextInt(serverConfig.getSpinItemCounts()[1])];
                    if (i == luckyIndex) {
                        user.updateFightItems(itemId, (byte) quantity);
                    }
                }
                case 1 -> {
                    quantity = serverConfig.getSpinXuCounts()[0][Utils.nextInt(serverConfig.getSpinXuCounts()[1])];
                    if (i == luckyIndex) {
                        user.updateXu(quantity);
                    }
                }
                case 2 -> {
                    quantity = serverConfig.getSpinXpCounts()[0][Utils.nextInt(serverConfig.getSpinXpCounts()[1])];
                    if (i == luckyIndex) {
                        user.updateXp(quantity);
                    }
                }
            }
            ds.writeByte(type);
            ds.writeByte(itemId);
            ds.writeInt(quantity);
        }
        ds.writeByte(luckyIndex);
        ds.flush();
        sendMessage(ms);

        lastSpinTime = currentTime;
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

    public void sendUpdateMoney() throws IOException {
        Message ms = new Message(Cmd.UPDATE_MONEY);
        DataOutputStream ds = ms.writer();
        ds.writeInt(user.getXu());
        ds.writeInt(user.getLuong());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateCup(int cupUp) throws IOException {
        Message ms = new Message(Cmd.CUP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(cupUp);
        ds.writeInt(user.getCup());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateXp(int xpUp, boolean updateLevel) throws IOException {
        Message ms = new Message(Cmd.UPDATE_EXP);
        DataOutputStream ds = ms.writer();
        ds.writeInt(xpUp);
        ds.writeInt(user.getCurrentXp());
        ds.writeInt(user.getCurrentRequiredXp());
        if (updateLevel) {
            ds.writeByte(1);
            ds.writeByte(user.getCurrentLevel());
            ds.writeByte(user.getCurrentLevelPercent());
            ds.writeShort(user.getCurrentPoint());
        } else {
            ds.writeByte(0);
            ds.writeByte(user.getCurrentLevelPercent());
        }
        ds.flush();
        sendMessage(ms);
    }

    public void getMoreGame() throws IOException {
        Message ms = new Message(Cmd.MORE_GAME);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(serverConfig.getDownloadTitle());
        ds.writeUTF(serverConfig.getDownloadInfo());
        ds.writeUTF(serverConfig.getDownloadUrl());
        ds.flush();
        sendMessage(ms);
    }
}
