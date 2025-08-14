package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.AccountDAO;
import com.teamobi.mobiarmy2.dao.UserCharacterDAO;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.dto.AccountDTO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.entity.Character;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.FightItem;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuthMessageHandler extends BaseMessageHandler {
    private final LoginRateLimiterService loginRateLimiterService;

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final UserCharacterDAO userCharacterDAO;

    public AuthMessageHandler(Session session, LoginRateLimiterService loginRateLimiterService, UserDAO userDAO, AccountDAO accountDAO, UserCharacterDAO userCharacterDAO) {
        super(session);
        this.loginRateLimiterService = loginRateLimiterService;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.userCharacterDAO = userCharacterDAO;
    }

    public void handleLogout() {
        session.close();
    }

    public void handleLogin(Message ms) throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);

        User currentUser = session.getUser();
        if (currentUser != null && currentUser.isLogged()) {
            return;
        }

        if (ApplicationContext.getInstance()
                .getBean(ServerManager.class).isMaintenanceMode()) {
            sendMessageLoginFail(GameString.MAINTENANCE_MODE);
            return;
        }

        DataInputStream dis = ms.reader();
        String username = dis.readUTF().trim();
        String password = dis.readUTF().trim();
        String version = dis.readUTF().trim();

        if (Utils.isAlphanumeric(username) || Utils.isAlphanumeric(password)) {
            sendMessageLoginFail(GameString.INVALID_ACCOUNT_PASSWORD);
            return;
        }

        //Kiểm tra thời gian đăng xuất gần nhất
        long remainingTime = loginRateLimiterService.getRemainingLoginTime(username);
        if (remainingTime > 0) {
            sendMessageLoginFail(GameString.createLoginCooldownMessage(remainingTime));
            return;
        }

        AccountDTO accountDTO = accountDAO.findByUsernameAndPassword(username, password);
        if (accountDTO == null) {
            sendMessageLoginFail(GameString.LOGIN_FAILED);
            return;
        }
        if (accountDTO.isLock()) {
            sendMessageLoginFail(GameString.ACCOUNT_LOCKED);
            return;
        }
        if (!accountDTO.isActive()) {
            sendMessageLoginFail(GameString.ACCOUNT_INACTIVE);
            return;
        }

        // Tạo người dùng mới
        User user = new User(session);
        user.setAccountId(accountDTO.getAccountId());
        session.setUser(user);
        session.initService();

        UserDTO userDTO = userDAO.findByAccountId(user.getAccountId());
        if (userDTO == null) {
            // Tạo mới người dùng
            Optional<Integer> result = userDAO.create(accountDTO.getAccountId(), 1000, 0);

            if (result.isPresent()) {
                userDTO = userDAO.findByAccountId(accountDTO.getAccountId());
            }

            if (userDTO == null) {
                sendMessageLoginFail(GameString.LOGIN_FAILED);
                return;
            }
        }

        //Kiểm tra có đang đăng nhập hay không
        User userLogin = ApplicationContext.getInstance()
                .getBean(ServerManager.class).getUserByUserId(userDTO.getUserId());
        if (userLogin != null) {
            userLogin.getUserMessageHandler().sendMoneyErrorMessage(GameString.ACCOUNT_OTHER_LOGIN);
            userLogin.getSession().close();

            sendMessageLoginFail(GameString.LOGIN_ANOTHER_DEVICE);
            return;
        }

        //Dữ liệu tài khoản
        updateUserFromDTO(userDTO);

        //Dữ liệu nhân vật
        List<UserCharacterDTO> userCharacterDTOS = userCharacterDAO.findAllByUserId(user.getUserId());
        if (userCharacterDTOS.isEmpty()) {
            //Tạo mới nhân vật
            Optional<Integer> result1 = userCharacterDAO.create(user.getUserId(), CharacterManager.CHARACTERS.get(0).getId());
            Optional<Integer> result2 = userCharacterDAO.create(user.getUserId(), CharacterManager.CHARACTERS.get(1).getId());
            Optional<Integer> result3 = userCharacterDAO.create(user.getUserId(), CharacterManager.CHARACTERS.get(2).getId());
            if (result1.isPresent() && result2.isPresent() && result3.isPresent()) {
                userCharacterDTOS = userCharacterDAO.findAllByUserId(user.getUserId());
            }

            if (userCharacterDTOS.isEmpty()) {
                sendMessageLoginFail(GameString.LOGIN_FAILED);
                return;
            }
        }
        updateUserCharacters(userCharacterDTOS);

        user.setUsername(username);
        user.getSession().setVersion(version);
        user.setLogged(true);

        //Tặng quà hằng ngày
        if (Utils.canReceiveDailyReward(userDTO.getDailyRewardTime())) {
            //Gửi item
            byte indexItem = FightItemManager.getRandomItem();
            byte quantity = 1;
            user.updateFightItems(indexItem, quantity);
            sendMessageToUser(GameString.createDailyRewardMessage(quantity, FightItemManager.FIGHT_ITEMS.get(indexItem).getName()));

            //Cập nhật quà top
            if (user.getTopEarningsXu() > 0) {
                user.updateXu(user.getTopEarningsXu());
                sendMessageToUser(GameString.createDailyTopRewardMessage(user.getTopEarningsXu()));
                user.setTopEarningsXu(0);
            }

            //Tặng quà tết
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(serverConfig.getTetStartTime()) && now.isBefore(serverConfig.getTetEndTime())) {
                int luckyXu = Utils.getNonLinearRandom(1000, 50999);
                int xuUp = (luckyXu / 1000) * 1000;
                user.updateXu(xuUp);
                sendMessageToUser(GameString.createDailyRewardMessage(xuUp));
            }

            //Đặt lại số lần mua nguyên liệu
            user.setMaterialsPurchased((byte) 0);

            //Gửi message khi login
            for (String msg : serverConfig.getMessage()) {
                sendMessageToUser(msg);
            }

            //Cập nhật nhiệm vụ đăng nhập
            user.updateMission(16, 1);

            // Cập nhật thời gian nhận quà
            userDAO.setDailyRewardTime(user.getUserId(), LocalDateTime.now());
        }

        //Đánh dấu trạng thái online
        userDAO.setOnline(userDTO.getUserId(), Boolean.TRUE);

        sendLoginSuccess();
        sendCharacterData(serverConfig);
        sendRoomCaption(serverConfig);
        sendMapCollisionInfo();
        sendServerInfo(serverConfig.getMessageLogin(), false);
    }

    private void updateUserFromDTO(UserDTO userDTO) {
        user.setUserId(userDTO.getUserId());
        user.setXu(userDTO.getXu());
        user.setLuong(userDTO.getLuong());
        user.setCup(userDTO.getCup());
        user.setPointEvent(userDTO.getPointEvent());
        user.setClanId(userDTO.getClanId());
        user.setActiveCharacterId(userDTO.getActiveCharacterId());
        user.setFriends(userDTO.getFriends());
        user.setMission(userDTO.getMission());
        user.setMissionLevel(userDTO.getMissionLevel());
        user.setSpecialItemChest(userDTO.getSpecialItemChest());
        user.setEquipmentChest(userDTO.getEquipmentChest());
        user.setFightItems(userDTO.getItems());
        user.setXpX2Time(userDTO.getXpX2Time());
        user.setTopEarningsXu(userDTO.getTopEarningsXu());
        user.setMaterialsPurchased(userDTO.getMaterialsPurchased());
        user.setEquipmentPurchased(userDTO.getEquipmentPurchased());
        user.setChestLocked(userDTO.isChestLocked());
        user.setInvitationLocked(userDTO.isInvitationLocked());
    }

    private void updateUserCharacters(List<UserCharacterDTO> userCharacterDTOS) {
        int totalCharacter = CharacterManager.CHARACTERS.size();

        user.setUserCharacterIds(new long[totalCharacter]);
        user.setOwnedCharacters(new boolean[totalCharacter]);
        user.setLevels(new int[totalCharacter]);
        user.setXps(new int[totalCharacter]);
        user.setPoints(new int[totalCharacter]);
        user.setAddedPoints(new short[totalCharacter][5]);
        user.setCharacterEquips(new EquipmentChest[totalCharacter][6]);
        user.setEquipData(new int[totalCharacter][6]);

        for (UserCharacterDTO userCharacterDTO : userCharacterDTOS) {
            byte characterId = userCharacterDTO.getCharacterId();
            user.getUserCharacterIds()[characterId] = userCharacterDTO.getUserCharacterId();
            user.getOwnedCharacters()[characterId] = true;
            user.getLevels()[characterId] = userCharacterDTO.getLevel();
            user.getXps()[characterId] = userCharacterDTO.getXp();
            user.getPoints()[characterId] = userCharacterDTO.getPoints();
            user.getAddedPoints()[characterId] = userCharacterDTO.getAdditionalPoints();
            user.getEquipData()[characterId] = new int[]{-1, -1, -1, -1, -1, -1};

            int[] data = userCharacterDTO.getData();
            for (int j = 0; j < data.length; j++) {
                EquipmentChest equip = user.getEquipmentByKey(data[j]);
                if (equip == null) {
                    continue;
                }
                if (equip.isExpired()) {
                    equip.setInUse(false);
                } else {
                    user.getCharacterEquips()[characterId][j] = equip;
                    user.getEquipData()[characterId][j] = equip.getKey();
                }
            }
        }
    }

    public void sendLoginSuccess() throws IOException {
        Message ms = new Message(Cmd.LOGIN_SUCESS);
        DataOutputStream ds = ms.writer();
        ds.writeInt(user.getUserId());
        ds.writeInt(user.getXu());
        ds.writeInt(user.getLuong());
        ds.writeByte(user.getActiveCharacterId());
        ds.writeShort(user.getClanId() != null ? user.getClanId() : 0);
        ds.writeByte(0);//clan rights

        for (int i = 0; i < 10; i++) {
            EquipmentChest equip = user.getCharacterEquips()[i][5];
            if (equip != null) {
                ds.writeBoolean(true);
                for (short s : equip.getEquipment().getDisguiseEquippedIndexes()) {
                    ds.writeShort(s);
                }
            } else {
                ds.writeBoolean(false);
            }

            for (int j = 0; j < 5; j++) {
                if (user.getCharacterEquips()[i][j] != null) {
                    ds.writeShort(user.getCharacterEquips()[i][j].getEquipment().getEquipIndex());
                } else if (EquipmentManager.equipDefault[i][j] != null) {
                    ds.writeShort(EquipmentManager.equipDefault[i][j].getEquipIndex());
                } else {
                    ds.writeShort(-1);
                }
            }
        }

        for (int i = 0; i < FightItemManager.FIGHT_ITEMS.size(); i++) {
            ds.writeByte(user.getFightItems()[i]);
            FightItem fightItem = FightItemManager.FIGHT_ITEMS.get(i);
            ds.writeInt(fightItem.getBuyXu());
            ds.writeInt(fightItem.getBuyLuong());
        }

        for (int i = 0; i < 10; i++) {
            if (i > 2) {
                ds.writeByte(user.getOwnedCharacters()[i] ? 1 : 0);
                Character character = CharacterManager.CHARACTERS.get(i);
                ds.writeShort(character.getPriceXu() / 1000);
                ds.writeShort(character.getPriceLuong());
            }
        }

        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        ds.writeUTF(serverConfig.getAddInfo());
        ds.writeUTF(serverConfig.getAddInfoUrl());
        ds.writeUTF(serverConfig.getRegTeamUrl());
        ds.flush();
        sendMessage(ms);
    }

    public void sendCharacterData(ServerConfig config) throws IOException {
        List<Character> characterEntries = CharacterManager.CHARACTERS;
        int characterCount = characterEntries.size();
        Message ms = new Message(Cmd.SKIP_2);
        DataOutputStream ds = ms.writer();
        ds.writeByte(characterCount);
        for (Character character : characterEntries) {
            ds.writeByte(character.getWindResistance());
        }
        ds.writeByte(characterCount);
        for (Character character : characterEntries) {
            ds.writeShort(character.getMinAngle());
        }
        ds.writeByte(characterCount);
        for (Character character : characterEntries) {
            ds.writeByte(character.getBulletDamage());
        }
        ds.writeByte(characterCount);
        for (Character character : characterEntries) {
            ds.writeByte(character.getBulletCount());
        }
        ds.writeByte(config.getMaxElementFight());
        ds.writeByte(config.getBossRoomMapId().length);
        for (byte mapId : config.getBossRoomMapId()) {
            ds.writeByte(mapId);
        }
        for (byte bossId : config.getBossRoomBossId()) {
            ds.writeByte(bossId);
        }
        ds.writeByte(config.getNumPlayer());
        ds.flush();
        sendMessage(ms);
    }

    private void sendRoomCaption(ServerConfig config) throws IOException {
        String[] names = config.getRoomNameVi();
        Message ms = new Message(Cmd.ROOM_CAPTION);
        DataOutputStream ds = ms.writer();
        ds.writeByte(names.length);
        for (int i = 0; i < names.length; i++) {
            ds.writeUTF(names[i]);
            ds.writeUTF(config.getRoomNameEn()[i]);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void sendMapCollisionInfo() throws IOException {
        Message ms = new Message(Cmd.UNDESTROYTILE);
        DataOutputStream ds = ms.writer();
        ds.writeShort(MapManager.ID_NOT_COLLISIONS.size());
        for (int i : MapManager.ID_NOT_COLLISIONS) {
            ds.writeShort(i);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleHandshakeMessage() {
        session.sendKeys();
    }

    public void getProvider(Message ms) throws IOException {
        byte provider = ms.reader().readByte();
        session.setProvider(provider);
    }

    public void getVersionCode(Message ms) throws IOException {
        String platform = ms.reader().readUTF();
        session.setPlatform(platform);
    }

    public void handleRegister(Message ms) {
        sendMessageLoginFail(GameString.REGISTRATION_REQUIRED);
    }

    public void getStringMessage(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        String str = dis.readUTF();
        if (str.isEmpty()) {
            return;
        }
    }
}
