package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dto.*;
import com.teamobi.mobiarmy2.fight.IFightWait;
import com.teamobi.mobiarmy2.fight.impl.TrainingManager;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.Character;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.IClanService;
import com.teamobi.mobiarmy2.service.ILeaderboardService;
import com.teamobi.mobiarmy2.service.ILoginRateLimiterService;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author tuyen
 */
public class UserService implements IUserService {
    private static final int minimumWaitTime = 5000;

    private final User user;

    private final IServerConfig serverConfig;
    private final IClanService clanService;
    private final ILeaderboardService leaderboardService;
    private final ILoginRateLimiterService loginRateLimiterService;

    private final IUserDAO userDAO;
    private final IAccountDAO accountDAO;
    private final IGiftCodeDAO giftCodeDAO;
    private final IUserGiftCodeDAO userGiftCodeDAO;
    private final IUserCharacterDAO userCharacterDAO;

    private UserAction userAction;
    private int totalTransactionAmount;
    private List<EquipmentChest> selectedEquips;
    private List<SpecialItemChest> selectedSpecialItems;
    private FabricateItem fabricateItem;

    private long timeSinceLeftRoom;
    private long lastSpinTime;

    public UserService(User user, IServerConfig serverConfig, IClanService clanService, ILeaderboardService leaderboardService, ILoginRateLimiterService loginRateLimiterService, IUserDAO userDAO, IAccountDAO accountDAO, IGiftCodeDAO giftCodeDAO, IUserGiftCodeDAO userGiftCodeDAO, IUserCharacterDAO userCharacterDAO) {
        this.user = user;
        this.serverConfig = serverConfig;
        this.clanService = clanService;
        this.leaderboardService = leaderboardService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.giftCodeDAO = giftCodeDAO;
        this.userGiftCodeDAO = userGiftCodeDAO;
        this.userCharacterDAO = userCharacterDAO;
    }

    private static String getFormattedRankDisplay(int rank) {
        if (rank < 10_000) {
            return String.format("Top %s", rank);
        } else if (rank < 100_000) {
            return String.format("Top %s+", Utils.getStringNumber(rank));
        } else {
            return "Top 100k+";
        }
    }

    private List<EquipmentChest> getSelectedEquips() {
        if (selectedEquips == null) {
            selectedEquips = new ArrayList<>();
        }
        return selectedEquips;
    }

    private List<SpecialItemChest> getSelectedSpecialItems() {
        if (selectedSpecialItems == null) {
            selectedSpecialItems = new ArrayList<>();
        }
        return selectedSpecialItems;
    }

    private void sendMessage(IMessage ms) {
        user.sendMessage(ms);
    }

    private boolean isInvalidInput(String input) {
        return !input.matches(CommonConstant.ALPHANUMERIC_PATTERN);
    }

    private void sendMessageLoginFail(String message) {
        try {
            IMessage ms = new Message(Cmd.LOGIN_FAIL);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleLogin(IMessage ms) {
        if (user.isLogged()) {
            return;
        }

        if (!leaderboardService.isComplete()) {
            sendMessageLoginFail(GameString.RANKING_NOT_LOADED);
            return;
        }

        ServerManager serverManager = ServerManager.getInstance();
        if (serverManager.isMaintenanceMode()) {
            sendMessageLoginFail(GameString.MAINTENANCE_MODE);
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF().trim();
            String password = dis.readUTF().trim();
            String version = dis.readUTF().trim();

            if (isInvalidInput(username) || isInvalidInput(password)) {
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
            user.setAccountId(accountDTO.getAccountId());

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
            User userLogin = serverManager.getUserByUserId(userDTO.getUserId());
            if (userLogin != null) {
                userLogin.getUserService().sendServerMessage2(GameString.ACCOUNT_OTHER_LOGIN);
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
            sendServerInfoToUser(serverConfig.getMessageLogin());

            serverManager.notifyListeners();
        } catch (IOException ignored) {
        }
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

    @Override
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

    public void sendCharacterData(IServerConfig config) {
        try {
            List<Character> characterEntries = CharacterManager.CHARACTERS;
            int characterCount = characterEntries.size();
            IMessage ms = new Message(Cmd.SKIP_2);
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
        } catch (IOException ignored) {
        }
    }

    public void sendRoomName() {
        String[] names = serverConfig.getBossRoomName();
        int startMapBoss = serverConfig.getStartMapBoss();
        try {
            IMessage ms = new Message(Cmd.CHANGE_ROOM_NAME);
            DataOutputStream ds = ms.writer();
            ds.writeByte(names.length);
            for (int i = 0; i < names.length; i++) {
                ds.writeByte(startMapBoss + i);
                ds.writeUTF(String.format("Phòng %d: %s", startMapBoss + i, names[i]));
                ds.writeByte(5);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private void sendRoomCaption(IServerConfig config) {
        String[] names = config.getRoomNameVi();
        try {
            IMessage ms = new Message(Cmd.ROOM_CAPTION);
            DataOutputStream ds = ms.writer();
            ds.writeByte(names.length);
            for (int i = 0; i < names.length; i++) {
                ds.writeUTF(names[i]);
                ds.writeUTF(config.getRoomNameEn()[i]);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMapCollisionInfo() {
        try {
            IMessage ms = new Message(Cmd.UNDESTROYTILE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapManager.ID_NOT_COLLISIONS.size());
            for (int i : MapManager.ID_NOT_COLLISIONS) {
                ds.writeShort(i);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            IMessage ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendServerMessage2(String message) {
        try {
            IMessage ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleHandshakeMessage() {
        user.getSession().sendKeys();
    }

    @Override
    public void extendItemDuration(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChest equip = user.getEquipmentByKey(key);
            if (equip == null) {
                return;
            }
            int gia = 0;
            for (byte itemId : equip.getSlots()) {
                SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
                if (item != null) {
                    gia += item.getPriceXu();
                }
            }
            gia /= 20;
            if (equip.getEquipment().getPriceXu() > 0) {
                gia += equip.getEquipment().getPriceXu();
            } else if (equip.getEquipment().getPriceLuong() > 0) {
                gia += equip.getEquipment().getPriceLuong() * 1000;
            }
            if (gia <= 0) {
                return;
            }
            if (action == 0) {
                ms = new Message(Cmd.GET_MORE_DAY);
                DataOutputStream ds = ms.writer();
                ds.writeInt(equip.getKey());
                ds.writeUTF(GameString.createEquipmentRenewalRequestMessage(gia));
                ds.flush();
                sendMessage(ms);
            } else {
                if (user.getXu() < gia) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-gia);
                equip.setPurchaseDate(LocalDateTime.now());
                user.updateInventory(equip, null, null, null);
                sendServerMessage(GameString.EXTEND_SUCCESS);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleGetMissions(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte action = ms.reader().readByte();
            if (action == 0) {
                sendMissionInfo();
            } else {
                byte missionId = ms.reader().readByte();
                missionComplete(missionId);
            }
        } catch (IOException ignored) {
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        String message;
        Mission mission = MissionManager.getMissionById(missionId);
        if (mission == null) {
            message = GameString.MISSION_NOT_FOUND;
        } else {
            byte missionType = mission.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = mission.getLevel();

            if (user.getMission()[missionType] < mission.getRequirement()) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else if (missionLevel == requiredLevel) {
                user.getMissionLevel()[mission.getType()]++;
                if (mission.getRewardXu() > 0) {
                    user.updateXu(mission.getRewardXu());
                }
                if (mission.getRewardLuong() > 0) {
                    user.updateLuong(mission.getRewardLuong());
                }
                if (mission.getRewardXp() > 0) {
                    user.updateXp(mission.getRewardXp());
                }
                if (mission.getRewardCup() > 0) {
                    user.updateCup(mission.getRewardCup());
                }
                sendMissionInfo();
                message = GameString.createMissionCompleteMessage(mission.getReward());
            } else if (missionLevel < requiredLevel) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else {
                message = GameString.MISSION_COMPLETED;
            }
        }
        sendServerMessage2(message);
    }

    private void sendMissionInfo() throws IOException {
        IMessage ms = new Message(Cmd.MISSISON);
        DataOutputStream ds = ms.writer();
        int i = 0;
        for (List<Byte> missionIds : MissionManager.MISSIONS_BY_TYPE.values()) {
            int index = user.getMissionLevel()[i] - 1;
            if (index >= missionIds.size()) {
                index = missionIds.size() - 1;
            }
            Mission mission = MissionManager.getMissionById(missionIds.get(index));
            ds.writeByte(mission.getId());
            ds.writeByte(mission.getLevel());
            ds.writeUTF(mission.getName());
            ds.writeUTF(mission.getReward());
            ds.writeInt(mission.getRequirement());
            ds.writeInt(Math.min(user.getMission()[i], mission.getRequirement()));
            ds.writeBoolean(user.getMission()[i] >= mission.getRequirement());
            i++;
        }
        ds.flush();
        sendMessage(ms);
    }

    @Override
    public void sendLoginSuccess() {
        try {
            IMessage ms = new Message(Cmd.LOGIN_SUCESS);
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

            ds.writeUTF(serverConfig.getAddInfo());
            ds.writeUTF(serverConfig.getAddInfoUrl());
            ds.writeUTF(serverConfig.getRegTeamUrl());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void contributeToClan(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }
        if (user.getClanId() == null) {
            return;
        }

        try {
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

                int minXuContributeClan = serverConfig.getMinXuContributeClan();
                if (quantity < minXuContributeClan) {
                    sendServerMessage(GameString.createClanContributionMinXuMessage(minXuContributeClan));
                    return;
                }

                //Update xu user
                user.updateXu(-quantity);

                //Update xu clan
                clanService.contributeClan(user.getClanId(), user.getUserId(), quantity, Boolean.TRUE);
                sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
            } else {
                if (quantity > user.getLuong()) {
                    return;
                }

                //Update lg user
                user.updateLuong(-quantity);

                //Update lg clan
                clanService.contributeClan(user.getClanId(), user.getUserId(), quantity, Boolean.FALSE);
                sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getVersionCode(IMessage ms) {
        try {
            String platform = ms.reader().readUTF();
            user.getSession().setPlatform(platform);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getProvider(IMessage ms) {
        try {
            byte provider = ms.reader().readByte();
            user.getSession().setProvider(provider);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleMergeEquipments(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte id = dis.readByte();
            byte action = dis.readByte();
            if (action == 1) {
                sendFormulaInfo(id);
            } else if (action == 2) {
                byte level = dis.readByte();
                processFormulaCrafting(id, level);
            }
        } catch (IOException ignored) {
        }
    }

    private void processFormulaCrafting(byte id, byte level) {
        Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
        if (formulaMap == null) {
            return;
        }
        List<Formula> formulas = formulaMap.get(user.getActiveCharacterId());
        if (formulas == null) {
            return;
        }
        Formula formula = formulas.get(level);
        if (formula == null) {
            return;
        }

        //Kiểm tra có đủ level chế đồ yêu cầu không
        if (user.getCurrentLevel() < formula.getLevelRequired()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Kiểm tra có trang bị yêu cầu không
        EquipmentChest requiredEquip = user.getEquipment(formula.getRequiredEquip().getEquipIndex(), user.getActiveCharacterId(), formula.getLevel());
        if (requiredEquip == null) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Tạo một danh sách item cần xóa
        List<SpecialItemChest> itemsToRemove = new ArrayList<>();

        //Kiểm tra có đủ item yêu cầu không
        for (SpecialItemChest item : formula.getRequiredItems()) {
            short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
            if (itemCountInInventory < item.getQuantity()) {
                sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
                return;
            }
            itemsToRemove.add(item);
        }

        //Kiểm tra có công thức hoặc đủ xu không
        SpecialItemChest material = user.getSpecialItemById(formula.getMaterial().getId());
        if (material == null && user.getXu() < formula.getMaterial().getPriceXu()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        } else {
            if (material != null) {//Nếu có công thức thì thêm vào danh sách item xóa
                itemsToRemove.add(new SpecialItemChest((short) 1, material.getItem()));
            } else {//Nếu chưu có thì trừ xu
                user.updateXu(-formula.getMaterial().getPriceXu());
            }
        }

        //Xoá trang bị và item yêu cầu
        user.updateInventory(null, requiredEquip, null, itemsToRemove);

        //Random chỉ số
        byte[] addPoints = new byte[5];
        byte[] addPercents = new byte[5];
        for (int i = 0; i < 5; i++) {
            addPoints[i] = (byte) Utils.nextInt(formula.getAddPointsMin()[i], formula.getAddPointsMax()[i]);
            addPercents[i] = (byte) Utils.nextInt(formula.getAddPercentsMin()[i], formula.getAddPercentsMax()[i]);
        }

        //Tạo trang bị mới
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(formula.getResultEquip());
        newEquip.setVipLevel((byte) (formula.getLevel() + 1));
        newEquip.setAddPoints(addPoints);
        newEquip.setAddPercents(addPercents);

        //Thêm trang bị vào rương
        user.addEquipment(newEquip);

        //Gửi thông báo
        sendFormulaProcessingResult(GameString.ITEM_CRAFT_SUCCESS);
    }

    private void sendFormulaProcessingResult(String message) {
        try {
            IMessage ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private void sendFormulaInfo(byte id) {
        try {
            Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
            if (formulaMap == null) {
                return;
            }
            List<Formula> formulaEntries = formulaMap.get(user.getActiveCharacterId());
            if (formulaEntries == null) {
                return;
            }
            IMessage ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(id);
            ds.writeByte(formulaEntries.size());
            for (Formula formula : formulaEntries) {
                boolean hasRequiredItem = true;
                boolean hasRequiredEquip = user.hasEquipment(formula.getRequiredEquip().getEquipIndex(), formula.getLevel());
                boolean hasRequiredLevel = user.getCurrentLevel() >= formula.getLevelRequired();

                ds.writeByte(formula.getResultEquip().getEquipIndex());
                ds.writeUTF("%s cấp %d".formatted(formula.getResultEquip().getName(), (formula.getLevel() + 1)));
                ds.writeByte(formula.getLevelRequired());
                ds.writeByte(formula.getCharacterId());
                ds.writeByte(formula.getEquipType());
                ds.writeByte(formula.getRequiredItems().size());
                for (SpecialItemChest item : formula.getRequiredItems()) {
                    short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
                    ds.writeByte(item.getItem().getId());
                    ds.writeUTF(item.getItem().getName());
                    ds.writeByte(item.getQuantity());
                    if (itemCountInInventory < item.getQuantity()) {
                        hasRequiredItem = false;
                        ds.writeByte(itemCountInInventory);
                    } else {
                        ds.writeByte(item.getQuantity());
                    }
                }
                ds.writeByte(formula.getRequiredEquip().getEquipIndex());
                ds.writeUTF(formula.getRequiredEquip().getName());
                ds.writeByte(formula.getLevel());
                ds.writeBoolean(hasRequiredEquip);
                ds.writeBoolean(hasRequiredEquip && hasRequiredItem && hasRequiredLevel);
                ds.writeByte(formula.getDetails().length);
                for (String detail : formula.getDetails()) {
                    ds.writeUTF(detail);
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void openLuckyGift(IMessage ms) {
        try {
            byte index = ms.reader().readByte();
            user.getGiftBoxService().openGiftBoxAfterFight(index);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void viewLeaderboard(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte page = dis.readByte();

            if (type >= leaderboardService.getCategories().length) {
                return;
            }
            ms = new Message(Cmd.BANGTHANHTICH);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            if (type < 0) {
                ds.writeByte(leaderboardService.getCategories().length);
                for (String name : leaderboardService.getCategories()) {
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
                ds.writeUTF(leaderboardService.getLabels()[type]);
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handlePurchaseClanItem(IMessage ms) {
        if (user.getClanId() == null) {
            sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {
                sendClanShop();
            } else {
                byte unit = dis.readByte();
                byte itemId = dis.readByte();
                buyClanShop(unit, itemId);
            }
        } catch (IOException ignored) {
        }
    }

    private void buyClanShop(byte unit, byte itemId) {
        ClanItemShop clanItemShop = ClanItemManager.getItemClanById(itemId);

        if (clanItemShop == null || clanItemShop.getOnSale() != 1) {
            return;
        }

        int currentLevel = clanService.getClanLevel(user.getClanId());
        if (currentLevel < clanItemShop.getLevel()) {
            sendServerMessage(GameString.CLAN_LEVEL_INSUFFICIENT);
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemShop.getXu() < 0) {
                return;
            }
            int xuClan = clanService.getClanXu(user.getClanId());
            if (xuClan < clanItemShop.getXu()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_XU);
                return;
            }

            clanService.updateItemClan(user.getClanId(), user.getUserId(), clanItemShop, true);
        } else {//Luong
            if (clanItemShop.getLuong() < 0) {
                return;
            }
            int luongClan = clanService.getClanLuong(user.getClanId());
            if (luongClan < clanItemShop.getLuong()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_LUONG);
                return;
            }

            clanService.updateItemClan(user.getClanId(), user.getUserId(), clanItemShop, false);
        }
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    private void sendClanShop() {
        IMessage ms = CacheManager.cachedClanItemShop;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        try {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void enterTrainingMap() {
        try {
            initializeTrainingManager();
            IMessage ms = new Message(Cmd.TRAINING_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getTrainingManager().getMapId());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleLogout(IMessage ms) {
        user.getSession().close();
    }

    @Override
    public void handleSpecialItemShop(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {
                sendSpecialItem();
            } else {
                byte unit = dis.readByte();
                byte itemId = dis.readByte();
                byte quantity = dis.readByte();
                purchaseSpecialItem(unit, itemId, quantity);
            }
        } catch (IOException ignored) {
        }
    }

    private void purchaseSpecialItem(byte unit, byte itemId, byte quantity) {
        //Kiểm tra số lượng mua hợp lệ
        if (quantity < 1) {
            return;
        }

        //Kiểm tra số lượng đang có trong rương
        if (user.getInventorySpecialItemCount(itemId) + quantity > serverConfig.getMaxSpecialItemSlots()) {
            sendServerMessage(GameString.CHEST_MAXIMUM_REACHED);
            return;
        }

        SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
        if (item == null || !item.isOnSale() || (unit == 0 ? item.getPriceXu() : item.getPriceLuong()) < 0) {
            return;
        }

        //Giới hạn số lần mua vật liệu
        if (item.isMaterial()) {
            if (user.getMaterialsPurchased() >= GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.MATERIAL_PURCHASE_LIMIT);
                return;
            } else if (user.getMaterialsPurchased() + quantity > GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.createMaterialPurchaseLimitMessage(GameConstants.MAX_MATERIAL_PURCHASE_LIMIT - user.getMaterialsPurchased()));
                return;
            }
        }

        if (unit == 0) {//Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (user.getXu() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-totalPrice);
        } else {//Mua bằng lượng
            int totalPrice = quantity * item.getPriceLuong();
            if (user.getLuong() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-totalPrice);
        }

        //Xử lý khi mua item đặc biệt
        boolean saveItem = handleSpecialItemPurchase(itemId);

        if (saveItem) {
            //Tạo item mới
            SpecialItemChest newItem = new SpecialItemChest(quantity, item);

            //Thêm vào rương đồ
            user.updateInventory(null, null, List.of(newItem), null);
        }

        //Cập nhật số lượng mua nếu là vật liệu
        if (item.isMaterial()) {
            user.incrementMaterialsPurchased(quantity);
        }

        //Gửi thông báo mua thành công
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    private boolean handleSpecialItemPurchase(byte itemId) {
        if (itemId == 50) {
            user.resetPoints();
            sendCharacterInfo();
            return false;
        }

        return true;
    }

    private void sendSpecialItem() {
        IMessage ms = CacheManager.cachedSpecialItemShop;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        try {
            ms = new Message(Cmd.SHOP_LINHTINH);
            DataOutputStream ds = ms.writer();
            Map<Byte, SpecialItem> sorted = new TreeMap<>(SpecialItemManager.SPECIAL_ITEMS);
            for (SpecialItem specialItem : sorted.values()) {
                if (!specialItem.isOnSale()) {
                    continue;
                }
                ds.writeByte(specialItem.getId());
                ds.writeUTF(specialItem.getName());
                ds.writeUTF(specialItem.getDetail());
                ds.writeInt(specialItem.getPriceXu());
                ds.writeInt(specialItem.getPriceLuong());
                ds.writeByte(specialItem.getExpirationDays());
                ds.writeByte(specialItem.isShowSelection() ? 0 : 1);
            }
            ds.flush();

            CacheManager.cachedSpecialItemShop = ms;

            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void equipVipItems(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChest equip = user.getEquipmentByKey(key);
            if (equip == null ||
                    equip.isExpired() ||
                    !equip.getEquipment().isDisguise() ||
                    equip.getEquipment().getLevelRequirement() > user.getCurrentLevel() ||
                    equip.getEquipment().getCharacterId() != user.getActiveCharacterId()
            ) {
                return;
            }
            EquipmentChest oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][5];
            if (oldEquip != null) {
                oldEquip.setInUse(false);
            }
            ms = new Message(Cmd.VIP_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(action);
            if (action == 0) {
                user.getEquipData()[user.getActiveCharacterId()][5] = -1;
                user.getCharacterEquips()[user.getActiveCharacterId()][5] = null;
            } else {
                equip.setInUse(true);
                user.getEquipData()[user.getActiveCharacterId()][5] = equip.getKey();
                user.getCharacterEquips()[user.getActiveCharacterId()][5] = equip;
                for (short a : equip.getEquipment().getDisguiseEquippedIndexes()) {
                    ds.writeShort(a);
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSendMessage(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            int userId = dis.readInt();
            String content = dis.readUTF().trim();
            if (content.isEmpty() || content.length() > 100) {
                return;
            }
            //Neu la admin -> bo qua
            if (userId == 1) {
                return;
            }
            //Neu la nguoi dua tin -> send Mss 46-> chat The gioi
            if (userId == 2) {
                int priceChatServer = serverConfig.getPriceChatServer();

                if (user.getXu() < priceChatServer) {
                    return;
                }
                user.updateXu(-priceChatServer);
                sendServerInfoToServer(GameString.createMessageFromSender(user.getUsername(), content));
                return;
            }
            User receiver = ServerManager.getInstance().getUserByUserId(userId);
            if (receiver == null) {
                return;
            }
            sendMessageToUser(receiver, content);
        } catch (IOException ignored) {
        }
    }

    private void sendServerInfo(String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            IMessage ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                ServerManager.getInstance().sendToServer(ms);
            } else {
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

    private void sendServerInfoToServer(String message) {
        sendServerInfo(message, true);
    }

    private void sendServerInfoToUser(String message) {
        sendServerInfo(message, false);
    }

    public void sendMessageToUser(String message) {
        sendMessageToUser(null, message);
    }

    private void sendMessageToUser(User userSend, String message) {
        if (message.isEmpty()) {
            return;
        }
        try {
            IMessage ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            if (userSend != null) {
                ds.writeInt(userSend.getUserId());
                ds.writeUTF(userSend.getUsername());
            } else {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            }
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSendRoomList() {
        if (user.isNotWaiting()) {
            return;
        }
        RoomManager roomManager = RoomManager.getInstance();
        try {
            IMessage ms = new Message(Cmd.ROOM_LIST);
            DataOutputStream ds = ms.writer();
            for (Room room : roomManager.getRooms()) {
                ds.writeByte(room.getIndex());
                ds.writeByte(room.getStatus());
                ds.writeByte(room.getFightWaitsAvailable());
                ds.writeByte(room.getType());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleEnteringRoom(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }
        Room[] rooms = RoomManager.getInstance().getRooms();
        try {
            byte roomNumber = ms.reader().readByte();
            if (roomNumber < 0 || roomNumber >= rooms.length) {
                return;
            }
            Room room = rooms[roomNumber];
            if (room.getType() == 6 && user.getClanId() == null) {
                sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
                return;
            }
            ms = new Message(Cmd.BOARD_LIST);
            DataOutputStream ds = ms.writer();
            ds.writeByte(roomNumber);
            for (IFightWait fightWait : room.getFightWaits()) {
                if (fightWait.isFightWaitInvalid()) {
                    continue;
                }
                ds.writeByte(fightWait.getId());
                ds.writeByte(fightWait.getNumPlayers());
                ds.writeByte(fightWait.getMaxSetPlayers());
                ds.writeBoolean(fightWait.isPassSet());
                ds.writeInt(fightWait.getMoney());
                ds.writeBoolean(fightWait.isStarted());
                ds.writeUTF(fightWait.getName());
                ds.writeByte(fightWait.getRoom().getIconType());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleJoinBoard(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }

        long timeRemaining = minimumWaitTime - (System.currentTimeMillis() - timeSinceLeftRoom);
        if (timeRemaining > 0) {
            sendServerMessage(GameString.createJoinAreaErrorMessage((int) (timeRemaining / 1000) + 1));
            return;
        }

        Room[] rooms = RoomManager.getInstance().getRooms();
        try {
            DataInputStream dis = ms.reader();
            byte roomNumber = dis.readByte();
            byte areaNumber = dis.readByte();
            String password = dis.readUTF().trim();
            if (roomNumber < 0 || roomNumber >= rooms.length) {
                return;
            }
            IFightWait[] fightWaits = rooms[roomNumber].getFightWaits();
            if (areaNumber < 0 || areaNumber >= fightWaits.length) {
                return;
            }
            IFightWait fightWait = fightWaits[areaNumber];
            if (fightWait.isPassSet() && !fightWait.getPassword().equals(password)) {
                sendServerMessage(GameString.AREA_INCORRECT_PASSWORD);
                return;
            }
            fightWait.addUser(user);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChatMessage(IMessage ms) {
        try {
            String message = ms.reader().readUTF().trim();
            if (message.isEmpty() || message.length() > 100) {
                return;
            }
            user.getFightWait().chatMessage(user.getUserId(), message);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleKickPlayer(IMessage ms) {
        try {
            int userId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getUserId(), userId);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleLeaveBoard(IMessage ms) {
        if (user.getState() == UserState.WAITING) {
            return;
        }
        user.getFightWait().leaveTeam(user.getUserId());
        timeSinceLeftRoom = System.currentTimeMillis();
    }

    @Override
    public void setReady(IMessage ms) {
        try {
            boolean ready = ms.reader().readBoolean();
            user.getFightWait().setReady(ready, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void imbueGem(IMessage ms) {
        List<EquipmentChest> equipList = getSelectedEquips();
        List<SpecialItemChest> specialItemList = getSelectedSpecialItems();

        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            if (action == 0) {
                //Đặt lại dữ liệu
                userAction = null;
                fabricateItem = null;
                equipList.clear();
                specialItemList.clear();

                byte size = dis.readByte();
                if (size <= 0 || size > 100) {
                    return;
                }
                for (byte i = 0; i < size; i++) {
                    int id = dis.readInt();
                    short quantity = (short) dis.readUnsignedByte();

                    //Kiểm tra dữ liệu hợp lệ
                    if (quantity == 0 || id < 0) {
                        continue;
                    }

                    //Lấy thông tin vật phẩm từ rương người chơi
                    if (id >= Byte.MAX_VALUE) {//Trường hợp trang bị
                        EquipmentChest equipment = user.getEquipmentByKey(id);
                        if (equipment == null || equipList.contains(equipment)) {
                            continue;
                        }
                        equipList.add(equipment);
                    } else {//Trường hợp là ngọc
                        SpecialItemChest specialItem = user.getSpecialItemById((byte) id);
                        if (specialItem == null || specialItem.getItem() == null || specialItem.getQuantity() < quantity || specialItemList.contains(specialItem)) {
                            continue;
                        }
                        specialItemList.add(new SpecialItemChest(quantity, specialItem.getItem()));
                    }
                }

                //Thoát nếu không tồn tại trong rương
                if (equipList.isEmpty() && specialItemList.isEmpty()) {
                    return;
                }

                //Ghép ngọc vào trang bị
                if (!equipList.isEmpty() && !specialItemList.isEmpty()) {
                    if (equipList.size() == 1 &&
                            specialItemList.size() == 1 &&
                            specialItemList.getFirst().getItem().isGem()
                    ) {
                        userAction = UserAction.INSERT_GEM_INTO_EQUIPMENT;
                        sendMessageConfirm(GameString.GEM_COMBINE_REQUEST);
                    } else {
                        sendServerMessage(GameString.COMBINE_ERROR);
                    }
                    return;
                }

                if (!specialItemList.isEmpty()) {
                    fabricateItem = FabricateItemManager.getFabricateItem(specialItemList);
                    if (fabricateItem != null) {
                        userAction = UserAction.COMBINE_SPECIAL_ITEM;
                        sendMessageConfirm(fabricateItem.getConfirmationMessage());
                        return;
                    }

                    if (specialItemList.size() == 1) {
                        SpecialItemChest specialItemChest = specialItemList.getFirst();
                        if (specialItemChest.getItem().isGem()) {
                            if (specialItemChest.getQuantity() == 5 && ((specialItemChest.getItem().getId() + 1) % 10 != 0)) {
                                userAction = UserAction.UPGRADE_GEM;
                                sendMessageConfirm(GameString.createGemFusionRequestMessage((90 - (specialItemChest.getItem().getId() % 10) * 10)));
                            } else {
                                userAction = UserAction.SELL_GEM;
                                totalTransactionAmount = specialItemChest.getSellPrice();
                                sendMessageConfirm(GameString.createGemSellRequestMessage(specialItemChest.getQuantity(), totalTransactionAmount));
                            }
                            return;
                        }

                        if (specialItemChest.getItem().isUsable()) {
                            userAction = UserAction.USE_SPECIAL_ITEM;
                            confirmSpecialItemUse(specialItemChest);
                            return;
                        }
                    }
                }
                sendServerMessage(GameString.COMBINE_ERROR);
            } else {
                switch (userAction) {
                    case INSERT_GEM_INTO_EQUIPMENT -> {
                        EquipmentChest equip = equipList.getFirst();
                        SpecialItemChest specialItem = specialItemList.getFirst();
                        if (equip.getEmptySlot() >= specialItem.getQuantity()) {
                            for (int i = 0; i < specialItem.getQuantity(); i++) {
                                equip.setNewSlot(specialItem.getItem().getId());
                                equip.decrementEmptySlot();
                                equip.addPoints(specialItem.getItem().getAbility());
                            }
                            user.updateInventory(equip, null, null, specialItemList);
                            sendServerMessage(GameString.GEM_COMBINE_SUCCESS);
                        } else {
                            sendServerMessage(GameString.GEM_COMBINE_NO_SLOT);
                        }
                    }

                    case UPGRADE_GEM -> {
                        SpecialItemChest specialItemChest = specialItemList.getFirst();
                        int successRate = (90 - (specialItemChest.getItem().getId() % 10) * 10);
                        int randomNumber = Utils.nextInt(100);
                        if (randomNumber < successRate) {
                            SpecialItemChest newItem = new SpecialItemChest();
                            newItem.setQuantity((short) 1);
                            newItem.setItem(SpecialItemManager.getSpecialItemById((byte) (specialItemChest.getItem().getId() + 1)));

                            user.updateInventory(null, null, List.of(newItem), List.of(specialItemChest));
                            sendServerMessage(GameString.createGemUpgradeSuccessMessage(newItem.getQuantity(), newItem.getItem().getName()));
                        } else {
                            specialItemChest.setQuantity((short) 1);
                            user.updateInventory(null, null, null, List.of(specialItemChest));
                            sendServerMessage(GameString.COMBINE_FAILURE);
                        }
                    }

                    case SELL_GEM -> {
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.CHEST_LOCKED_NO_SELL);
                            return;
                        }
                        user.updateInventory(null, null, null, specialItemList);
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.PURCHASE_SUCCESS);
                    }

                    case USE_SPECIAL_ITEM -> handleUseSpecialItem(specialItemList.getFirst());

                    case COMBINE_SPECIAL_ITEM -> {
                        if (fabricateItem.getRewardXu() > 0) {
                            user.updateXu(fabricateItem.getRewardXu());
                        }
                        if (fabricateItem.getRewardLuong() > 0) {
                            user.updateLuong(fabricateItem.getRewardLuong());
                        }
                        if (fabricateItem.getRewardCup() > 0) {
                            user.updateCup(fabricateItem.getRewardCup());
                        }
                        if (fabricateItem.getRewardExp() > 0) {
                            user.updateXp(fabricateItem.getRewardExp());
                        }

                        List<SpecialItemChest> addItems = fabricateItem.getRewardItem()
                                .stream()
                                .map(SpecialItemChest::new)
                                .toList();

                        user.updateInventory(null, null, addItems, specialItemList);

                        if (!fabricateItem.getCompletionMessage().isEmpty()) {
                            sendServerMessage(fabricateItem.getCompletionMessage());
                        }
                    }
                }

                //Đặt lại dữ liệu
                userAction = null;
            }
        } catch (IOException ignored) {
        }
    }

    private void handleUseSpecialItem(SpecialItemChest specialItemChest) {
        switch (specialItemChest.getItem().getId()) {
            case 54 -> {
                user.addDaysToXpX2Time(1);
                user.updateInventory(null, null, null, List.of(specialItemChest));
                sendServerMessage(GameString.ITEM_X2_XP_USAGE_SUCCESS);
            }

            case 86 -> {
                if (specialItemChest.getQuantity() == 50) {
                    System.out.println("Cong trang bi vang 1");
                } else if (specialItemChest.getQuantity() == 100) {
                    System.out.println("Cong trang bi vang 2");
                } else if (specialItemChest.getQuantity() == 150) {
                    System.out.println("Cong trang bi vang 3");
                } else {
                    user.updateXp(1000 * specialItemChest.getQuantity());
                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.USE_BANH_TRUNG_SUCCESS);
                }
            }

            case 87 -> {
                if (specialItemChest.getQuantity() == 50) {
                    System.out.println("Cong trang bi bac 1");
                } else if (specialItemChest.getQuantity() == 100) {
                    System.out.println("Cong trang bi bac 2");
                } else if (specialItemChest.getQuantity() == 150) {
                    System.out.println("Cong trang bi bac 3");
                } else {
                    user.updateXp(500 * specialItemChest.getQuantity());
                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.USE_BANH_TET_SUCCESS);
                }
            }

            case 93 -> {
                System.out.println("Tobe continue...");
            }
        }
    }

    private void confirmSpecialItemUse(SpecialItemChest specialItemChest) {
        switch (specialItemChest.getItem().getId()) {
            case 54 -> {
                if (specialItemChest.getQuantity() == 1) {
                    sendMessageConfirm(GameString.ITEM_X2_XP_USAGE_REQUEST);
                }
            }

            case 86 -> {
                if (specialItemChest.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_1);
                } else if (specialItemChest.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_2);
                } else if (specialItemChest.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TRUNG_REQUEST);
                }
            }

            case 87 -> {
                if (specialItemChest.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_1);
                } else if (specialItemChest.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_2);
                } else if (specialItemChest.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TET_REQUEST);
                }
            }

            case 93 -> sendMessageConfirm(GameString.BLACK_FRIDAY_GIFT_BOX_REQUEST);

            default -> sendServerMessage(GameString.COMBINE_ERROR);
        }
    }

    private void sendMessageConfirm(String message) {
        try {
            IMessage ms = new Message(Cmd.IMBUE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetPasswordFightWait(IMessage ms) {
        try {
            String password = ms.reader().readUTF().trim();
            if (password.isEmpty() || password.length() > 10) {
                return;
            }
            user.getFightWait().setPassRoom(password, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetMoneyFightWait(IMessage ms) {
        try {
            int xu = ms.reader().readInt();
            if (xu < 0) {
                return;
            }
            user.getFightWait().setMoney(xu, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleStartGame() {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().startGame(user.getUserId());
    }

    @Override
    public void movePlayer(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            short x = dis.readShort();
            short y = dis.readShort();

            if (user.getState() == UserState.FIGHTING) {
                user.getFightWait().getFightManager().changeLocation(user.getUserId(), x, y);
            } else if (user.getState() == UserState.TRAINING) {
                user.getTrainingManager().changeLocation(x, y);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void shoot(IMessage ms) {
        if (user.getState() != UserState.FIGHTING) {
            return;
        }
        DataInputStream dis = ms.reader();
        try {
            byte bullId = dis.readByte();
            short x = dis.readShort();
            short y = dis.readShort();
            short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
            byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
            byte force2 = 0;
            if (bullId == 17 || bullId == 19) {
                force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
            }
            byte numShoot = dis.readByte();

            user.getFightWait().getFightManager().addShoot(user.getUserId(), bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void processShootingResult(IMessage ms) {
        //todo
    }

    @Override
    public void handleUseItem(IMessage ms) {
        try {
            byte itemIndex = ms.reader().readByte();
            if (itemIndex != 100) {
                if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                    return;
                }

                if (user.getItemFightQuantity(itemIndex) < 1) {
                    return;
                }
            }
            user.getFightWait().getFightManager().useItem(user.getUserId(), itemIndex);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleJoinAnyBoard(IMessage ms) {
        Room[] rooms = RoomManager.getInstance().getRooms();
        IFightWait fightWait = null;
        try {
            int type = ms.reader().readByte();
            switch (type) {
                // Đấu trùm
                case 5 -> {
                    int start = serverConfig.getStartMapBoss();
                    int end = start + serverConfig.getRoomQuantity()[5];

                    outerLoop:
                    for (int i = start; i < end; i++) {
                        Room room = rooms[i];
                        for (IFightWait fight : room.getFightWaits()) {
                            if (!fight.isStarted() &&
                                    !fight.isPassSet() &&
                                    !fight.isContinuous() &&
                                    fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                    fight.getMoney() <= user.getXu()
                            ) {
                                fightWait = fight;
                                break outerLoop;
                            }
                        }
                    }
                }

                //4vs4->1vs1
                case 4, 3, 2, 1 -> {
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                fight.getMoney() <= user.getXu() &&
                                fight.getMaxSetPlayers() == type * 2
                        ) {
                            fightWait = fight;
                            break;
                        }
                    }
                }

                //Khu vực trống
                case 0 -> {
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                fight.getMoney() <= user.getXu() &&
                                fight.getNumPlayers() == 0
                        ) {
                            fightWait = fight;
                            break;
                        }
                    }
                }

                //Ngẫu nhiên
                case -1 -> {
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                fight.getMoney() <= user.getXu()
                        ) {
                            fightWait = fight;
                            break;
                        }
                    }
                }
            }

            if (fightWait == null) {
                sendServerMessage2(GameString.AREA_NOT_FOUND);
            } else {
                fightWait.sendInfo(user);
                fightWait.addUser(user);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleViewFriendList() {
        try {
            IMessage ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();
            if (!user.getFriends().isEmpty()) {
                List<FriendDTO> friends = userDAO.getFriendsList(user.getUserId(), user.getFriends());
                for (FriendDTO friend : friends) {
                    ds.writeInt(friend.getUserId());
                    ds.writeUTF(friend.getName());
                    ds.writeInt(friend.getXu());
                    ds.writeByte(friend.getActiveCharacterId());
                    ds.writeShort(friend.getClanId());
                    ds.writeByte(friend.getOnline());
                    ds.writeByte(friend.getLevel());
                    ds.writeByte(friend.getLevelPt());
                    for (short i : friend.getData()) {
                        ds.writeShort(i);
                    }
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleAddFriend(IMessage ms) {
        int maxFriends = serverConfig.getMaxFriends();
        Set<Integer> friends = user.getFriends();
        try {
            int id = ms.reader().readInt();

            // Kiểm tra số lượng bạn bè đã đạt giới hạn
            if (friends.size() >= maxFriends) {
                sendAddFriendMessage(2);
                return;
            }

            // Kiểm tra nếu bạn bè đã tồn tại
            if (!friends.add(id)) {
                sendAddFriendMessage(1);
                return;
            }

            sendAddFriendMessage(0);
        } catch (IOException e) {
            sendAddFriendMessage(1);
        }
    }

    @Override
    public void handleRemoveFriend(IMessage ms) {
        try {
            Integer id = ms.reader().readInt();
            user.getFriends().remove(id);
            sendDeleteFriendMessage(0);
        } catch (IOException e) {
            sendDeleteFriendMessage(1);
        }
    }

    private void sendDeleteFriendMessage(int status) {
        try {
            IMessage ms = new Message(Cmd.DELETE_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private void sendAddFriendMessage(int status) {
        try {
            IMessage ms = new Message(Cmd.ADD_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleGetFlayerDetail(IMessage ms) {
        try {
            int userId = ms.reader().readInt();
            User us = null;
            if (userId == user.getUserId()) {
                us = user;
            } else if (user.isNotWaiting()) {
                us = user.getFightWait().getUserByUserId(userId);
            }
            ms = new Message(Cmd.PLAYER_DETAIL);
            DataOutputStream ds = ms.writer();
            if (us == null) {
                ds.writeInt(-1);
            } else {
                String rankDisplayText = GameString.NO_RANKING;
                if (us.getRank() == 0) {
                    Optional<Integer> userRankOptional = userDAO.getUserRankByCup(us.getCup());
                    if (userRankOptional.isPresent()) {
                        us.setRank(userRankOptional.get());

                        rankDisplayText = getFormattedRankDisplay(us.getRank());
                    }
                } else {
                    rankDisplayText = getFormattedRankDisplay(us.getRank());
                }

                ds.writeInt(us.getUserId());
                ds.writeUTF(us.getUsername());
                ds.writeInt(us.getXu());
                ds.writeByte(us.getCurrentLevel());
                ds.writeByte(us.getCurrentLevelPercent());
                ds.writeInt(us.getLuong());
                ds.writeInt(us.getCurrentXp());
                ds.writeInt(us.getCurrentRequiredXp());
                ds.writeInt(us.getCup());
                ds.writeUTF(rankDisplayText);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleFindPlayer(IMessage ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.isEmpty()) {
                sendMessageLoginFail(GameString.FRIEND_ADD_MISSING_NAME);
                return;
            }
            if (isInvalidInput(username)) {
                sendMessageLoginFail(GameString.FRIEND_ADD_INVALID_NAME);
                return;
            }
            Optional<Integer> foundUserId = userDAO.findUserIdByUsername(username);
            ms = new Message(Cmd.SEARCH);
            DataOutputStream ds = ms.writer();
            if (foundUserId.isPresent()) {
                ds.writeInt(foundUserId.get());
                ds.writeUTF(username);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void skipTurn() {
        user.getFightWait().getFightManager().skipTurn(user.getUserId());
    }

    @Override
    public void updateCoordinates(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            short x = dis.readShort();
            short y = dis.readShort();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetFightWaitName(IMessage ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getUserId(), name);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetMaxPlayerFightWait(IMessage ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getUserId(), maxPlayers);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChoseItemFight(IMessage ms) {
        DataInputStream dis = ms.reader();
        byte[] items = new byte[8];

        try {
            for (int i = 0; i < items.length; i++) {
                byte index = dis.readByte();
                if (user.getItemFightQuantity(index) > 0) {
                    items[i] = index;
                } else {
                    items[i] = -1;
                }
            }
            user.getFightWait().setItems(user.getUserId(), items);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChoseCharacter(IMessage ms) {
        try {
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
        } catch (IOException ignored) {
        }
    }

    private void sendEquipInfo() {
        try {
            IMessage ms = new Message(Cmd.CURR_EQUIP_DBKEY);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChangeTeam(IMessage ms) {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().changeTeam(user);
    }

    @Override
    public void handlePurchaseItem(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte unit = dis.readByte();
            byte itemIndex = dis.readByte();
            byte quantity = dis.readByte();
            if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                return;
            }
            if (user.getFightItems()[itemIndex] + quantity > serverConfig.getMaxItem()) {
                return;
            }
            if (unit == 0) {
                int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyXu() * quantity;
                if (user.getXu() < total || total < 0) {
                    return;
                }
                user.updateXu(-total);
            } else {
                int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyLuong() * quantity;
                if (user.getLuong() < total || total < 0) {
                    return;
                }
                user.updateLuong(-total);
            }
            user.updateFightItems(itemIndex, quantity);
            ms = new Message(Cmd.BUY_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(itemIndex);
            ds.writeByte(user.getFightItems()[itemIndex]);
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            sendMessage(ms);
            sendServerMessage(GameString.PURCHASE_SUCCESS);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleBuyCharacter(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte index = dis.readByte();
            byte unit = dis.readByte();
            if (index < 0 || index >= user.getOwnedCharacters().length - 3) {
                return;
            }
            index += 3;
            if (user.getOwnedCharacters()[index]) {
                return;
            }
            Character character = CharacterManager.CHARACTERS.get(index);
            if (unit == 0) {
                if (character.getPriceXu() <= 0) {
                    return;
                }
                if (user.getXu() < character.getPriceXu()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-character.getPriceXu());
            } else {
                if (character.getPriceLuong() <= 0) {
                    return;
                }
                if (user.getLuong() < character.getPriceLuong()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateLuong(-character.getPriceLuong());
            }

            Optional<Integer> result = userCharacterDAO.create(user.getUserId(), index);
            if (result.isPresent()) {
                UserCharacterDTO userCharacterDTO = userCharacterDAO.findByUserIdAndCharacterId(user.getUserId(), index);
                if (userCharacterDTO != null) {
                    user.getLevels()[index] = userCharacterDTO.getLevel();
                    user.getXps()[index] = userCharacterDTO.getXp();
                    user.getPoints()[index] = userCharacterDTO.getPoints();
                    user.getAddedPoints()[index] = userCharacterDTO.getAdditionalPoints();
                    user.getUserCharacterIds()[index] = userCharacterDTO.getUserCharacterId();
                    user.getOwnedCharacters()[index] = true;
                    user.getEquipData()[index] = userCharacterDTO.getData();

                    ms = new Message(Cmd.BUY_GUN);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(index - 3);
                    ds.flush();
                    sendMessage(ms);
                }
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSelectMap(IMessage ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getUserId(), mapId);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleCardRecharge(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            String type = dis.readUTF().trim();
            String serial = dis.readUTF().trim();
            String pin = dis.readUTF().trim();

            if (type.equals("giftcode") && !serial.isEmpty()) {
                handleGiftCode(serial);
                return;
            }
            sendServerMessage(serial + " " + pin);
        } catch (IOException ignored) {
        }
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

    @Override
    public void handleFindPlayerWait(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            boolean find = dis.readBoolean();
            if (find) {
                user.getFightWait().findPlayer(user.getUserId());
            } else {
                int userId = dis.readInt();
                user.getFightWait().inviteToRoom(userId);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void clearBullet(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            int size = dis.readByte();
            int[] x = new int[size];
            int[] y = new int[size];
            for (byte i = 0; i < size; i++) {
                x[i] = dis.readInt();
                y[i] = dis.readInt();
            }
            //todo
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChangePassword(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            String oldPass = dis.readUTF().trim();
            String newPass = dis.readUTF().trim();

            if (isInvalidInput(oldPass) || isInvalidInput(newPass)) {
                sendServerMessage(GameString.PASSWORD_INVALID_CHARACTER);
                return;
            }

            if (!accountDAO.existsByAccountIdAndPassword(user.getAccountId(), oldPass)) {
                sendServerMessage(GameString.PASSWORD_INCORRECT_OLD);
                return;
            }

            accountDAO.changePassword(user.getAccountId(), newPass);
            sendServerMessage(GameString.PASSWORD_CHANGE_SUCCESS);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getFilePack(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte version = dis.readByte();

            switch (type) {
                case 1 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getIconVersion2());
                    if (version != serverConfig.getIconVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.ICON_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }

                case 2 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getValuesVersion2());
                    if (version != serverConfig.getValuesVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.MAP_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 3 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getPlayerVersion2());
                    if (version != serverConfig.getPlayerVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.PLAYER_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 4 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getEquipVersion2());
                    if (version != serverConfig.getEquipVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.EQUIP_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeInt(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 5 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getLevelCVersion2());
                    if (version != serverConfig.getLevelCVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.LEVEL_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 6 -> {
                    sendCharacterInfo();
                    sendInventoryInfo();
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void sendInventoryInfo() {
        try {
            IMessage ms = new Message(Cmd.INVENTORY);
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

    @Override
    public void handleAddPoints(IMessage ms) {
        try {
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
        } catch (IOException ignored) {
        }
        sendCharacterInfo();
    }

    @Override
    public void sendCharacterInfo() {
        try {
            IMessage ms = new Message(Cmd.CHARACTOR_INFO);
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

    @Override
    public void handleChangeEquipment(IMessage ms) {
        try {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSendShopEquipments() {
        IMessage ms = CacheManager.cachedShopEquipments;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        try {
            ms = new Message(Cmd.SHOP_EQUIP);
            DataOutputStream ds = ms.writer();
            List<Short> equipIds = EquipmentManager.SALE_INDEX_TO_ID;
            ds.writeShort(equipIds.size());
            for (Short id : equipIds) {
                Equipment equip = EquipmentManager.getEquipment(id);
                ds.writeByte(equip.getCharacterId());
                ds.writeByte(equip.getEquipType());
                ds.writeShort(equip.getEquipIndex());
                ds.writeUTF(equip.getName());
                ds.writeInt(equip.getPriceXu());
                ds.writeInt(equip.getPriceLuong());
                ds.writeByte(equip.getExpirationDays());
                ds.writeByte(equip.getLevelRequirement());
            }
            ds.flush();

            CacheManager.cachedShopEquipments = ms;

            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleEquipmentTransactions(IMessage ms) {
        List<EquipmentChest> equipList = getSelectedEquips();
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            switch (action) {
                case 0 -> {//Mua trang bị
                    short saleIndex = dis.readShort();
                    byte unit = dis.readByte();
                    purchaseEquipment(saleIndex, unit);
                }
                case 1 -> {//Gửi lệnh bán trang bị
                    //Đặt lại giá trị
                    userAction = null;
                    totalTransactionAmount = 0;
                    equipList.clear();

                    //Lấy dữ liệu và tính tiền
                    byte size = dis.readByte();
                    if (size <= 0 || size > 100) {
                        return;
                    }
                    for (int i = 0; i < size; i++) {
                        int key = dis.readInt();
                        EquipmentChest equip = user.getEquipmentByKey(key);
                        if (equip == null || equipList.contains(equip)) {
                            continue;
                        }
                        int remainingDays = equip.getRemainingDays();
                        if (remainingDays > 0) {
                            if (equip.getEquipment().getPriceXu() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipment().getPriceXu() * remainingDays) / (equip.getEquipment().getExpirationDays() * 2));
                            } else if (equip.getEquipment().getPriceLuong() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipment().getPriceLuong() * 1000 * remainingDays) / (equip.getEquipment().getExpirationDays() * 2));
                            }
                        }
                        equipList.add(equip);
                    }

                    //Gửi thông báo
                    ms = new Message(Cmd.BUY_EQUIP);
                    DataOutputStream ds = ms.writer();
                    if (!equipList.isEmpty()) {//Trường hợp có trang bị hợp lệ
                        ds.writeByte(1);
                        if (equipList.size() == 1 && equipList.getFirst().getEmptySlot() < 3) {//Tháo ngọc
                            userAction = UserAction.REMOVE_GEM_FROM_EQUIPMENT;
                            totalTransactionAmount = 0;

                            //Tính tiền gia hạn theo 25% giá ngọc
                            for (byte slotItemId : equipList.getFirst().getSlots()) {
                                SpecialItem item = SpecialItemManager.getSpecialItemById(slotItemId);
                                if (item != null) {
                                    totalTransactionAmount += (int) (item.getPriceXu() * 0.25);
                                }
                            }
                            ds.writeUTF(GameString.createGemRemovalRequestMessage(totalTransactionAmount));
                        } else {//Bán trang bị
                            userAction = UserAction.SELL_EQUIPMENT;
                            ds.writeUTF(GameString.createEquipmentSellRequestMessage(equipList.size(), totalTransactionAmount));
                        }
                    } else {//Trường hợp không trang bị nào hợp lệ
                        ds.writeByte(0);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 2 -> {//Xác nhận bán trang bị
                    if (userAction == UserAction.REMOVE_GEM_FROM_EQUIPMENT) {//Xác nhận tháo ngọc
                        if (user.getXu() < totalTransactionAmount) {
                            sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                            return;
                        }

                        //Trừ phí tháo ngọc
                        user.updateXu(-totalTransactionAmount);

                        EquipmentChest selectedEquipment = equipList.getFirst();
                        if (selectedEquipment == null) {
                            return;
                        }

                        //Lấy lại ngọc đã ghép
                        List<SpecialItemChest> recoveredGems = new ArrayList<>();
                        for (byte slotItemId : selectedEquipment.getSlots()) {
                            if (slotItemId > -1) {
                                SpecialItemChest gem = new SpecialItemChest((short) 1, SpecialItemManager.getSpecialItemById(slotItemId));
                                if (gem.getItem() != null) {
                                    recoveredGems.add(gem);

                                    //Trừ điểm đã cộng vào trang bị
                                    selectedEquipment.subtractPoints(gem.getItem().getAbility());
                                }
                            }
                        }

                        //Đặt các slot ngọc thành trống
                        selectedEquipment.setSlots(new byte[]{-1, -1, -1});
                        selectedEquipment.setEmptySlot((byte) 3);

                        //Cập nhật rương
                        user.updateInventory(selectedEquipment, null, recoveredGems, null);

                        //Gửi thông báo thành công
                        sendServerMessage(GameString.GEM_REMOVAL_SUCCESS);
                    } else if (userAction == UserAction.SELL_EQUIPMENT) {//Xác nhận bán trang bị
                        //Kiểm tra có khóa rương không
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.CHEST_LOCKED_NO_SELL);
                            return;
                        }

                        for (EquipmentChest equipment : equipList) {
                            if (equipment.isInUse()) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_IN_USE);
                                return;
                            }
                            if (equipment.getEmptySlot() < 3) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_REMOVE_GEMS);
                                return;
                            }
                        }
                        for (EquipmentChest validEquipment : equipList) {
                            user.updateInventory(null, validEquipment, null, null);
                        }
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.PURCHASE_SUCCESS);
                    }
                    userAction = null;
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void purchaseEquipment(short saleIndex, byte unit) {
        if (user.getEquipmentChest().size() >= serverConfig.getMaxEquipmentSlots()) {
            sendServerMessage(GameString.CHEST_NO_SPACE);
            return;
        }
        Equipment equipment = EquipmentManager.getEquipmentBySaleIndex(saleIndex);
        if (equipment == null || (unit == 0 ? equipment.getPriceXu() : equipment.getPriceLuong()) < 0) {
            return;
        }
        if (unit == 0) {
            if (user.getXu() < equipment.getPriceXu()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-equipment.getPriceXu());
        } else {
            if (user.getLuong() < equipment.getPriceLuong()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-equipment.getPriceLuong());
        }
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(equipment);
        user.addEquipment(newEquip);
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    @Override
    public void handleSpinWheel(IMessage ms) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }
        try {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getClanIcon(IMessage ms) {
        try {
            short clanId = ms.reader().readShort();
            byte[] data = clanService.getClanIcon(clanId);
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getTopClan(IMessage ms) {
        try {
            byte page = ms.reader().readByte();

            byte totalPages = clanService.getTotalPagesClan();
            if (page > totalPages) {
                page = 0;
            }

            List<ClanDTO> topClan = clanService.getTopTeams(page);
            ms = new Message(Cmd.TOP_CLAN);
            DataOutputStream ds = ms.writer();
            ds.writeByte(page);
            for (ClanDTO clan : topClan) {
                ds.writeShort(clan.getClanId());
                ds.writeUTF(clan.getName());
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getInfoClan(IMessage ms) {
        try {
            short clanId = ms.reader().readShort();
            ClanInfoDTO clanDetails = clanService.getClanInfo(clanId);
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getClanMember(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte page = dis.readByte();
            short clanId = dis.readShort();

            byte totalPage = clanService.getTotalPage(clanId);
            if (totalPage == -1) {
                return;
            }
            if (page >= totalPage) {
                page = 0;
            }
            if (page < 0) {
                page = (byte) (totalPage - 1);
            }

            List<ClanMemDTO> clanMemDTO = clanService.getMemberClan(clanId, page);

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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getBigImage(IMessage ms) {
        try {
            int id = ms.reader().readByte();
            ms = new Message(Cmd.GET_BIG_IMAGE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(id);
            byte[] file = Utils.getFile(String.format(GameConstants.BIG_IMAGE_PATH, id));
            if (file != null) {
                ds.writeShort(file.length);
                ds.write(file);
            } else {
                ds.writeShort(0);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleRegister(IMessage ms) {
        sendMessageLoginFail(GameString.REGISTRATION_REQUIRED);
    }

    @Override
    public void rechargeMoney(IMessage ms) {
        try {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getMaterialIconMessage(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte typeIcon = dis.readByte();
            byte iconId = dis.readByte();

            byte indexIcon = 0;
            byte[] data = null;
            switch (typeIcon) {
                case 0, 1 -> data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
                case 2 -> data = Utils.getFile(String.format(GameConstants.MAP_ICON_PATH, iconId));
                case 3, 4 -> {
                    indexIcon = dis.readByte();
                    data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
                }
            }
            if (data == null) {
                data = new byte[0];
            }

            ms = new Message(Cmd.MATERIAL_ICON);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeIcon);
            ds.writeByte(iconId);
            ds.writeShort(data.length);
            ds.write(data);
            if (typeIcon == 3 || typeIcon == 4) {
                ds.writeByte(indexIcon);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void startTraining(IMessage ms) {
        try {
            byte type = ms.reader().readByte();

            initializeTrainingManager();

            if (type == 0) {//Start game
                if (user.isNotWaiting()) {
                    return;
                }

                user.setState(UserState.TRAINING);
                user.getTrainingManager().startTraining();
            } else {//Out game
                if (user.getState() != UserState.TRAINING) {
                    return;
                }

                user.setState(UserState.WAITING);
                user.getTrainingManager().stopTraining();

                ms = new Message(Cmd.TRAINING);
                DataOutputStream ds = ms.writer();
                ds.writeByte(1);
                ds.flush();
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

    private void initializeTrainingManager() {
        if (user.getTrainingManager() == null) {
            user.setTrainingManager(new TrainingManager(user, serverConfig.getTrainingMapId()));
        }
    }

    @Override
    public void trainShooting(IMessage ms) {
        if (user.getState() != UserState.TRAINING) {
            return;
        }

        DataInputStream dis = ms.reader();
        try {
            byte bullId = dis.readByte();
            short x = dis.readShort();
            short y = dis.readShort();
            short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
            byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
            byte force2 = 0;
            if (bullId == 17 || bullId == 19) {
                force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
            }
            byte numShoot = dis.readByte();

            user.getTrainingManager().addShoot(user, bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendUpdateMoney() {
        try {
            IMessage ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendUpdateCup(int cupUp) {
        try {
            IMessage ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(cupUp);
            ds.writeInt(user.getCup());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendUpdateXp(int xpUp, boolean updateLevel) {
        try {
            IMessage ms = new Message(Cmd.UPDATE_EXP);
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void ping(IMessage ms) {
    }

    @Override
    public void getMoreGame() {
        try {
            IMessage ms = new Message(Cmd.MORE_GAME);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(serverConfig.getDownloadTitle());
            ds.writeUTF(serverConfig.getDownloadInfo());
            ds.writeUTF(serverConfig.getDownloadUrl());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSendAgentAndProviders() {
        try {
            IMessage ms = new Message(Cmd.GET_AGENT_PROVIDER);
            DataOutputStream ds = ms.writer();
            ds.writeUTF("none");
            ds.writeByte(0);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getStringMessage(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            String str = dis.readUTF();
        } catch (IOException ignored) {
        }
    }
}
