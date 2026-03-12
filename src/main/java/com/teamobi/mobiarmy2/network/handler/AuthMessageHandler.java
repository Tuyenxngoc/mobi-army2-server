package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.AccountStatus;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
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
import com.teamobi.mobiarmy2.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        session.closeChannel();
    }

    public void handleUserLogoutCleanup() {
        if (us().getState() == UserState.FIGHTING || us().getState() == UserState.WAIT_FIGHT) {
            us().getFightWait().leaveTeam(us().getUserId());
        }

        HikariCPManager hikariCPManager = ApplicationContext.getInstance().getBean(HikariCPManager.class);
        boolean success = hikariCPManager.transaction(connection -> {
            // Cập nhật thông tin tài khoản
            userDAO.update(connection, us());

            // Cập nhật thông tin nhân vật
            List<UserCharacterDTO> userCharacterDTOS = new ArrayList<>();
            for (byte i = 0; i < us().getOwnedCharacters().length; i++) {
                if (us().getOwnedCharacters()[i]) {
                    UserCharacterDTO userCharacterDTO = getUserCharacterDTO(i);
                    userCharacterDTOS.add(userCharacterDTO);
                }
            }
            userCharacterDAO.updateAll(connection, userCharacterDTOS);
        });

        if (success) {
            us().setLogged(false);
            // Lưu thời gian đăng xuất gần nhất
            loginRateLimiterService.saveLogoutTime(us().getUsername());
        } else {
            log.error("Failed to save user data on logout for user: {}", us().getUserId());
        }
    }

    private UserCharacterDTO getUserCharacterDTO(byte i) {
        UserCharacterDTO userCharacterDTO = new UserCharacterDTO();
        userCharacterDTO.setCharacterId(i);
        userCharacterDTO.setUserId(us().getUserId());
        userCharacterDTO.setLevel(us().getLevels()[i]);
        userCharacterDTO.setXp(us().getXps()[i]);
        userCharacterDTO.setPoints(us().getPoints()[i]);
        userCharacterDTO.setAdditionalPoints(us().getAddedPoints()[i]);
        userCharacterDTO.setData(us().getEquipData()[i]);
        return userCharacterDTO;
    }

    public void sendMessageLoginFail(String message) {
        try {
            Message ms = new Message(Cmd.LOGIN_FAIL);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogin(Message ms) throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);

        if (us() != null && us().isLogged()) {
            return;
        }

        if (ApplicationContext.getInstance()
                .getBean(ServerManager.class).isMaintenanceMode()) {
            sendMessageLoginFail(GameString.MAINTENANCE_MODE);
            return;
        }

        ServerManager serverManager = ApplicationContext.getInstance().getBean(ServerManager.class);
        if (serverManager.getUserCount() >= serverConfig.getMaxClients()) {
            sendMessageLoginFail(GameString.SERVER_FULL);
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
        if (accountDTO.getStatus().equals(AccountStatus.LOCKED)) {
            sendMessageLoginFail(GameString.ACCOUNT_LOCKED);
            return;
        }
        if (!accountDTO.getStatus().equals(AccountStatus.ACTIVE)) {
            sendMessageLoginFail(GameString.ACCOUNT_INACTIVE);
            return;
        }

        // Tạo người dùng mới
        User user = new User(session);
        user.setAccountId(accountDTO.getAccountId());

        // Đặt người dùng vào session
        session.setUser(user);
        session.setVersion(version);
        session.registerHandlers();

        UserDTO userDTO = userDAO.findByAccountId(us().getAccountId());
        if (userDTO == null) {
            // Tạo mới người dùng và nhân vật mặc định
            HikariCPManager hikariCPManager = ApplicationContext.getInstance().getBean(HikariCPManager.class);
            boolean success = hikariCPManager.transaction(connection -> {
                int userId = userDAO.create(connection, us().getAccountId(), 1000, 0);
                userCharacterDAO.create(connection, userId, CharacterManager.CHARACTERS.get(0).getId());
                userCharacterDAO.create(connection, userId, CharacterManager.CHARACTERS.get(1).getId());
                userCharacterDAO.create(connection, userId, CharacterManager.CHARACTERS.get(2).getId());
            });

            if (!success) {
                sendMessageLoginFail(GameString.LOGIN_FAILED);
                return;
            }

            userDTO = userDAO.findByAccountId(us().getAccountId());
            if (userDTO == null) {
                sendMessageLoginFail(GameString.LOGIN_FAILED);
                return;
            }
        }

        //Kiểm tra có đang đăng nhập hay không
        User userLogin = ApplicationContext.getInstance()
                .getBean(ServerManager.class).getUserByUserId(userDTO.getUserId());
        if (userLogin != null) {
            userLogin.sendMoneyErrorMessage(GameString.ACCOUNT_OTHER_LOGIN);
            userLogin.getSession().closeChannel();

            sendMessageLoginFail(GameString.LOGIN_ANOTHER_DEVICE);
            return;
        }

        //Dữ liệu tài khoản
        updateUserFromDTO(userDTO);

        //Dữ liệu nhân vật
        List<UserCharacterDTO> userCharacterDTOS = userCharacterDAO.findAllByUserId(us().getUserId());
        if (userCharacterDTOS.isEmpty()) {
            sendMessageLoginFail(GameString.LOGIN_FAILED);
            return;
        }
        updateUserCharacters(userCharacterDTOS);

        us().setUsername(username);
        us().setLogged(true);

        // Register into online index for fast lookup
        ApplicationContext.getInstance()
                .getBean(ServerManager.class)
                .registerUser(us());

        //Tặng quà hằng ngày
        if (Utils.canReceiveDailyReward(userDTO.getDailyRewardTime())) {
            //Gửi item
            byte indexItem = FightItemManager.getRandomItem();
            byte quantity = 1;
            us().updateFightItems(indexItem, quantity);
            us().sendMessageToUser(GameString.createDailyRewardMessage(quantity, FightItemManager.FIGHT_ITEMS.get(indexItem).getName()));

            //Cập nhật quà top
            if (us().getTopEarningsXu() > 0) {
                us().updateXu(us().getTopEarningsXu());
                us().sendMessageToUser(GameString.createDailyTopRewardMessage(us().getTopEarningsXu()));
                us().setTopEarningsXu(0);
            }

            //Tặng quà tết
            if (serverConfig.isTet()) {
                int luckyXu = Utils.getNonLinearRandom(1000, 50999);
                int xuUp = (luckyXu / 1000) * 1000;
                us().updateXu(xuUp);
                us().sendMessageToUser(GameString.createDailyRewardMessage(xuUp));
            }

            //Đặt lại số lần mua nguyên liệu
            us().setMaterialsPurchased((byte) 0);

            //Gửi message khi login
            for (String msg : serverConfig.getMessage()) {
                us().sendMessageToUser(msg);
            }

            //Cập nhật nhiệm vụ đăng nhập
            us().updateMission(16, 1);

            // Cập nhật thời gian nhận quà
            userDAO.setDailyRewardTime(us().getUserId(), LocalDateTime.now());
        }

        //Đánh dấu trạng thái online
        userDAO.setOnline(userDTO.getUserId(), Boolean.TRUE);

        sendLoginSuccess();
        sendCharacterData();
        sendRoomCaption();
        sendMapCollisionInfo();
        us().sendServerInfo(serverConfig.getMessageLogin(), false);
    }

    private void updateUserFromDTO(UserDTO userDTO) {
        us().setUserId(userDTO.getUserId());
        us().setXu(userDTO.getXu());
        us().setLuong(userDTO.getLuong());
        us().setCup(userDTO.getCup());
        us().setEventPoint(userDTO.getEventPoint());
        us().setClanId(userDTO.getClanId());
        us().setActiveCharacterId(userDTO.getActiveCharacterId());
        us().setFriends(userDTO.getFriends());
        us().setMission(userDTO.getMission());
        us().setMissionLevel(userDTO.getMissionLevel());
        us().setSpecialItemChest(userDTO.getSpecialItemChest());
        us().setEquipmentChest(userDTO.getEquipmentChest());
        us().setFightItems(userDTO.getItems());
        us().setXpX2Time(userDTO.getXpX2Time());
        us().setTopEarningsXu(userDTO.getTopEarningsXu());
        us().setMaterialsPurchased(userDTO.getMaterialsPurchased());
        us().setEquipmentPurchased(userDTO.getEquipmentPurchased());
        us().setChestLocked(userDTO.isChestLocked());
        us().setInvitationLocked(userDTO.isInvitationLocked());
    }

    private void updateUserCharacters(List<UserCharacterDTO> userCharacterDTOS) {
        int totalCharacter = CharacterManager.CHARACTERS.size();

        us().setUserCharacterIds(new long[totalCharacter]);
        us().setOwnedCharacters(new boolean[totalCharacter]);
        us().setLevels(new int[totalCharacter]);
        us().setXps(new int[totalCharacter]);
        us().setPoints(new int[totalCharacter]);
        us().setAddedPoints(new short[totalCharacter][5]);
        us().setCharacterEquips(new EquipmentChest[totalCharacter][6]);
        us().setEquipData(new int[totalCharacter][6]);

        for (UserCharacterDTO userCharacterDTO : userCharacterDTOS) {
            byte characterId = userCharacterDTO.getCharacterId();
            us().getUserCharacterIds()[characterId] = userCharacterDTO.getUserCharacterId();
            us().getOwnedCharacters()[characterId] = true;
            us().getLevels()[characterId] = userCharacterDTO.getLevel();
            us().getXps()[characterId] = userCharacterDTO.getXp();
            us().getPoints()[characterId] = userCharacterDTO.getPoints();
            us().getAddedPoints()[characterId] = userCharacterDTO.getAdditionalPoints();
            us().getEquipData()[characterId] = new int[]{-1, -1, -1, -1, -1, -1};

            int[] data = userCharacterDTO.getData();
            for (int j = 0; j < data.length; j++) {
                EquipmentChest equip = us().getEquipmentByKey(data[j]);
                if (equip == null) {
                    continue;
                }
                if (equip.isExpired()) {
                    equip.setInUse(false);
                } else {
                    us().getCharacterEquips()[characterId][j] = equip;
                    us().getEquipData()[characterId][j] = equip.getKey();
                }
            }
        }
    }

    public void sendLoginSuccess() throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        Message ms = new Message(Cmd.LOGIN_SUCESS);
        DataOutputStream ds = ms.writer();
        ds.writeInt(us().getUserId());
        ds.writeInt(us().getXu());
        ds.writeInt(us().getLuong());
        ds.writeByte(us().getActiveCharacterId());
        ds.writeShort(us().getClanId());
        ds.writeByte(0);//clan rights

        for (int i = 0; i < 10; i++) {
            EquipmentChest equip = us().getCharacterEquips()[i][5];
            if (equip != null) {
                ds.writeBoolean(true);
                for (short s : equip.getEquipment().getDisguiseEquippedIndexes()) {
                    ds.writeShort(s);
                }
            } else {
                ds.writeBoolean(false);
            }

            for (int j = 0; j < 5; j++) {
                if (us().getCharacterEquips()[i][j] != null) {
                    ds.writeShort(us().getCharacterEquips()[i][j].getEquipment().getEquipIndex());
                } else if (EquipmentManager.equipDefault[i][j] != null) {
                    ds.writeShort(EquipmentManager.equipDefault[i][j].getEquipIndex());
                } else {
                    ds.writeShort(-1);
                }
            }
        }

        for (int i = 0; i < FightItemManager.FIGHT_ITEMS.size(); i++) {
            ds.writeByte(us().getFightItems()[i]);
            FightItem fightItem = FightItemManager.FIGHT_ITEMS.get(i);
            ds.writeInt(fightItem.getBuyXu());
            ds.writeInt(fightItem.getBuyLuong());
        }

        for (int i = 0; i < 10; i++) {
            if (i > 2) {
                ds.writeByte(us().getOwnedCharacters()[i] ? 1 : 0);
                Character character = CharacterManager.CHARACTERS.get(i);
                ds.writeShort(character.getPriceXu() / 1000);
                ds.writeShort(character.getPriceLuong());
            }
        }

        ds.writeUTF(serverConfig.getAddInfo());
        ds.writeUTF(serverConfig.getAddInfoUrl());
        ds.writeUTF(serverConfig.getRegTeamUrl());
        ds.flush();
        sendMessage(ms);
    }

    public void sendCharacterData() throws IOException {
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
        ds.writeByte(RoomManager.MAX_ELEMENT_FIGHT);
        ds.writeByte(RoomManager.BOSS_ROOM_MAP_ID.length);
        for (byte mapId : RoomManager.BOSS_ROOM_MAP_ID) {
            ds.writeByte(mapId);
        }
        for (byte bossId : RoomManager.BOSS_ROOM_BOSS_ID) {
            ds.writeByte(bossId);
        }
        ds.writeByte(RoomManager.NUM_PLAYER_PER_ROOM);
        ds.flush();
        sendMessage(ms);
    }

    private void sendRoomCaption() throws IOException {
        String[] roomNameVi = RoomManager.ROOM_NAME_VI;
        String[] roomNameEn = RoomManager.ROOM_NAME_EN;
        Message ms = new Message(Cmd.ROOM_CAPTION);
        DataOutputStream ds = ms.writer();
        ds.writeByte(roomNameVi.length);
        for (int i = 0; i < roomNameVi.length; i++) {
            ds.writeUTF(roomNameVi[i]);
            ds.writeUTF(roomNameEn[i]);
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

    public void handleSendAgentAndProviders() throws IOException {
        String agent = session.getAgent();
        if (agent == null || agent.isEmpty()) {
            return;
        }
        byte provider = session.getProvider();
        if (provider == -1) {
            return;
        }

        Message ms = new Message(Cmd.GET_AGENT_PROVIDER);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(agent);
        ds.writeByte(provider);
        ds.flush();
        sendMessage(ms);
    }

    public void ping(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        int pingId = dis.readInt();

        Message response = new Message(Cmd.PING);
        DataOutputStream ds = response.writer();
        ds.writeInt(pingId);
        ds.flush();
        sendMessage(response);
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

    public void handleChangePassword(Message ms) throws IOException {
        DataInputStream dis = ms.reader();

        String oldPass = dis.readUTF().trim();
        String newPass = dis.readUTF().trim();

        if (Utils.isAlphanumeric(oldPass) || Utils.isAlphanumeric(newPass)) {
            us().sendServerMessage(GameString.PASSWORD_INVALID_CHARACTER);
            return;
        }

        if (!accountDAO.existsByAccountIdAndPassword(us().getAccountId(), oldPass)) {
            us().sendServerMessage(GameString.PASSWORD_INCORRECT_OLD);
            return;
        }

        accountDAO.changePassword(us().getAccountId(), newPass);
        us().sendServerMessage(GameString.PASSWORD_CHANGE_SUCCESS);
    }

    public void getAgent(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        String agent = dis.readUTF();
        if (agent.isEmpty()) {
            return;
        }
        session.setAgent(agent);
    }
}
