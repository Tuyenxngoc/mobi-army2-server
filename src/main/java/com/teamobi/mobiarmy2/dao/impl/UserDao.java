package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.TransactionType;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.PlayerCharacterEntry;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.user.FriendEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.repository.CharacterRepository;
import com.teamobi.mobiarmy2.repository.FightItemRepository;
import com.teamobi.mobiarmy2.repository.MissionRepository;
import com.teamobi.mobiarmy2.repository.SpecialItemRepository;
import com.teamobi.mobiarmy2.util.GsonUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class UserDao implements IUserDao {

    @Override
    public void save(User user) {
        HikariCPManager.getInstance().update("INSERT INTO `players`(`user_id`, `xu`, `luong`) VALUES (?,?,?)", user.getUserId(), user.getXu(), user.getLuong());
    }

    public static String convertSpecialItemChestEntriesToJson(List<SpecialItemChestEntry> specialItemChestEntries) {
        List<SpecialItemChestJson> specialItemChestJsons = specialItemChestEntries.stream().map(entry -> {
            SpecialItemChestJson jsonItem = new SpecialItemChestJson();
            jsonItem.setId(entry.getItem().getId());
            jsonItem.setQuantity(entry.getQuantity());
            return jsonItem;
        }).collect(Collectors.toList());

        return GsonUtil.getInstance().toJson(specialItemChestJsons);
    }

    public static String convertEquipmentChestEntriesToJson(List<EquipmentChestEntry> equipmentChestEntries) {
        List<EquipmentChestJson> equipmentChestJsons = equipmentChestEntries.stream().map(entry -> {
            EquipmentChestJson jsonItem = new EquipmentChestJson();
            jsonItem.setCharacterId(entry.getEquipEntry().getCharacterId());
            jsonItem.setEquipIndex(entry.getEquipEntry().getEquipIndex());
            jsonItem.setEquipType(entry.getEquipEntry().getEquipType());
            jsonItem.setKey(entry.getKey());
            jsonItem.setInUse((byte) (entry.isInUse() ? 1 : 0));
            jsonItem.setVipLevel(entry.getVipLevel());
            jsonItem.setPurchaseDate(entry.getPurchaseDate());
            jsonItem.setSlots(entry.getSlots());
            jsonItem.setAddPoints(entry.getAddPoints());
            jsonItem.setAddPercents(entry.getAddPercents());

            return jsonItem;
        }).collect(Collectors.toList());

        return GsonUtil.getInstance().toJson(equipmentChestJsons);
    }

    @Override
    public void update(User user) {
        String specialItemChestJson = convertSpecialItemChestEntriesToJson(user.getSpecialItemChest());
        String equipmentChestJson = convertEquipmentChestEntriesToJson(user.getEquipmentChest());

        String sql = "UPDATE `players` SET " +
                "`friends` = ?, " +
                "`xu` = ?, " +
                "`luong` = ?, " +
                "`cup` = ?, " +
                "`clan_id` = ?, " +
                "`item` = ?, " +
                "`equipment_chest` = ?, " +
                "`item_chest` = ? ," +
                "`is_online` = ?, " +
                "`mission` = ?, " +
                "`missionLevel` = ?, " +
                "`top_earnings_xu` = ?, " +
                "`active_character_id` = ?, " +
                "`materials_purchased` = ?, " +
                "`equipment_purchased` = ?, " +
                "`x2_xp_time` = ?, " +
                "`point_event` = ? " +
                //...//
                " WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql,
                user.getFriends().toString(),
                user.getXu(),
                user.getLuong(),
                user.getCup(),
                user.getClanId(),
                Arrays.toString(user.getItems()),
                equipmentChestJson,
                specialItemChestJson,
                false,
                Arrays.toString(user.getMission()),
                Arrays.toString(user.getMissionLevel()),
                user.getTopEarningsXu(),
                user.getPlayerCharacterIds()[user.getActiveCharacterId()],
                user.getMaterialsPurchased(),
                user.getEquipmentPurchased(),
                user.getXpX2Time(),
                user.getPointEvent(),
                //...//
                user.getPlayerId());

        for (int i = 0; i < user.getOwnedCharacters().length; i++) {
            if (user.getOwnedCharacters()[i]) {
                String sqlUpdateCharacter =
                        "UPDATE player_characters SET level = ?, points = ?, xp = ?, data = ?, additional_points = ? " +
                                "WHERE player_id = ? AND character_id = ?";
                HikariCPManager.getInstance().update(
                        sqlUpdateCharacter,
                        user.getLevels()[i],
                        user.getPoints()[i],
                        user.getXps()[i],
                        Arrays.toString(user.getEquipData()[i]),
                        Arrays.toString(user.getAddedPoints()[i]),
                        user.getPlayerId(),
                        i
                );
            }
        }
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        User user = null;
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {

            //Truy vấn để lấy thông tin từ bảng user
            String userQuery = "SELECT `user_id`, `password`, `is_enabled`, `is_locked` FROM users WHERE username = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, username);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        //Kiểm tra mật khẩu đã được mã hóa bằng jBCrypt
                        String hashedPassword = userResultSet.getString("password");
                        if (!BCrypt.checkpw(password, hashedPassword)) {
                            return null;
                        }
                        user = new User();
                        user.setUsername(username);
                        user.setUserId(userResultSet.getString("user_id"));
                        user.setLock(userResultSet.getBoolean("is_locked"));
                        user.setActive(userResultSet.getBoolean("is_enabled"));
                    }
                }
            }

            if (user == null) {
                return null;
            }

            if (user.isLock() || !user.isActive()) {
                return user;
            }

            Gson gson = GsonUtil.getInstance();

            //Truy vấn để lấy thông tin từ bảng player
            String playerQuery =
                    "SELECT " +
                            "p.player_id, p.xu, p.luong, p.cup, p.point_event, " +
                            "p.materials_purchased, p.equipment_purchased, " +
                            "p.item, p.equipment_chest, p.item_chest, " +
                            "p.friends, p.mission, p.missionLevel, " +
                            "p.x2_xp_time, p.last_online, p.top_earnings_xu, " +
                            "p.is_chest_locked, p.is_invitation_locked, " +
                            "pc.character_id, pc.player_character_id, pc.level, " +
                            "pc.xp, pc.points, pc.additional_points, pc.data, " +
                            "cm.clan_id " +
                            "FROM players p " +
                            "INNER JOIN player_characters pc ON p.active_character_id = pc.player_character_id " +
                            "LEFT JOIN clan_members cm on p.player_id = cm.player_id " +
                            "WHERE user_id = ?";

            try (PreparedStatement playerStatement = connection.prepareStatement(playerQuery)) {
                playerStatement.setString(1, user.getUserId());
                try (ResultSet playerResultSet = playerStatement.executeQuery()) {
                    int totalCharacter = CharacterRepository.CHARACTER_ENTRIES.size();
                    user.setPlayerCharacterIds(new long[totalCharacter]);
                    user.setOwnedCharacters(new boolean[totalCharacter]);
                    user.setLevels(new int[totalCharacter]);
                    user.setLevelPercents(new byte[totalCharacter]);
                    user.setXps(new int[totalCharacter]);
                    user.setPoints(new int[totalCharacter]);
                    user.setAddedPoints(new short[totalCharacter][5]);
                    user.setEquipData(new int[totalCharacter][6]);
                    user.setCharacterEquips(new EquipmentChestEntry[totalCharacter][6]);
                    user.setSpecialItemChest(new ArrayList<>());
                    user.setEquipmentChest(new ArrayList<>());

                    if (playerResultSet.next()) {
                        user.setPlayerId(playerResultSet.getInt("player_id"));
                        user.setXu(playerResultSet.getInt("xu"));
                        user.setLuong(playerResultSet.getInt("luong"));
                        user.setCup(playerResultSet.getInt("cup"));
                        user.setActiveCharacterId(playerResultSet.getByte("character_id"));
                        user.setPointEvent(playerResultSet.getInt("point_event"));
                        user.setMaterialsPurchased(playerResultSet.getByte("materials_purchased"));
                        user.setEquipmentPurchased(playerResultSet.getShort("equipment_purchased"));
                        user.setChestLocked(playerResultSet.getBoolean("is_chest_locked"));
                        user.setInvitationLocked(playerResultSet.getBoolean("is_invitation_locked"));

                        Object clanIdObj = playerResultSet.getObject("clan_id");
                        if (clanIdObj != null) {
                            user.setClanId(((Number) clanIdObj).shortValue());
                        } else {
                            user.setClanId(null);
                        }

                        //Đọc dữ liệu item chiến đấu
                        byte[] items = gson.fromJson(playerResultSet.getString("item"), byte[].class);
                        if (items.length != FightItemRepository.FIGHT_ITEM_ENTRIES.size()) {
                            byte[] adjustedItems = new byte[FightItemRepository.FIGHT_ITEM_ENTRIES.size()];
                            System.arraycopy(items, 0, adjustedItems, 0, Math.min(items.length, adjustedItems.length));
                            user.setItems(adjustedItems);
                        } else {
                            user.setItems(items);
                        }

                        //Đọc dữ liệu trang bị
                        EquipmentChestJson[] equipmentChestJsons = gson.fromJson(playerResultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                        for (EquipmentChestJson json : equipmentChestJsons) {
                            EquipmentChestEntry equip = new EquipmentChestEntry();
                            equip.setEquipEntry(CharacterRepository.getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex()));
                            if (equip.getEquipEntry() == null) {
                                continue;
                            }
                            equip.setKey(json.getKey());
                            equip.setPurchaseDate(json.getPurchaseDate());
                            equip.setVipLevel(json.getVipLevel());
                            equip.setInUse(json.getInUse() == 1);
                            equip.setAddPoints(json.getAddPoints());
                            equip.setAddPercents(json.getAddPercents());
                            equip.setSlots(json.getSlots());
                            byte emptySlot = 0;
                            for (int i = 0; i < equip.getSlots().length; i++) {
                                if (equip.getSlots()[i] < 0) {
                                    emptySlot++;
                                }
                            }
                            equip.setEmptySlot(emptySlot);
                            user.getEquipmentChest().add(equip);
                        }

                        //Đọc dữ liệu item
                        SpecialItemChestJson[] specialItemChestJsons = gson.fromJson(playerResultSet.getString("item_chest"), SpecialItemChestJson[].class);
                        for (SpecialItemChestJson item : specialItemChestJsons) {
                            SpecialItemChestEntry specialItemChestEntry = new SpecialItemChestEntry();
                            specialItemChestEntry.setItem(SpecialItemRepository.getSpecialItemById(item.getId()));
                            if (specialItemChestEntry.getItem() == null) {
                                continue;
                            }
                            specialItemChestEntry.setQuantity(item.getQuantity());
                            user.getSpecialItemChest().add(specialItemChestEntry);
                        }

                        //Dữ liệu bạn bè
                        int[] friendsArray = gson.fromJson(playerResultSet.getString("friends"), int[].class);
                        List<Integer> friendsList = Arrays.stream(friendsArray)
                                .boxed().collect(Collectors.toList());
                        user.setFriends(friendsList);

                        //Dữ liệu nhiệm vụ
                        int[] missions = gson.fromJson(playerResultSet.getString("mission"), int[].class);
                        byte[] missionLevels = gson.fromJson(playerResultSet.getString("missionLevel"), byte[].class);

                        if (missions.length != MissionRepository.MISSION_LIST.size()) {
                            int[] adjustedMissions = new int[MissionRepository.MISSION_LIST.size()];
                            System.arraycopy(missions, 0, adjustedMissions, 0, Math.min(missions.length, adjustedMissions.length));
                            user.setMission(adjustedMissions);
                        } else {
                            user.setMission(missions);
                        }
                        if (missionLevels.length != MissionRepository.MISSION_LIST.size()) {
                            byte[] adjustedMissionLevels = new byte[MissionRepository.MISSION_LIST.size()];
                            Arrays.fill(adjustedMissionLevels, (byte) 1);
                            System.arraycopy(missionLevels, 0, adjustedMissionLevels, 0, Math.min(missionLevels.length, adjustedMissionLevels.length));
                            user.setMissionLevel(adjustedMissionLevels);
                        } else {
                            user.setMissionLevel(missionLevels);
                        }

                        Timestamp x2Timestamp = playerResultSet.getTimestamp("x2_xp_time");
                        if (x2Timestamp != null) {
                            user.setXpX2Time(x2Timestamp.toLocalDateTime());
                        } else {
                            user.setXpX2Time(null);
                        }

                        user.setLastOnline(playerResultSet.getTimestamp("last_online").toLocalDateTime());

                        user.setTopEarningsXu(playerResultSet.getInt("top_earnings_xu"));
                    } else {
                        return null;
                    }
                }
            }

            //Truy vấn để lấy thông tin nhân vật
            String playerCharacterQuery = "SELECT * FROM player_characters pc WHERE pc.player_id = ? ORDER BY pc.character_id";
            try (PreparedStatement characterStatement = connection.prepareStatement(playerCharacterQuery)) {
                characterStatement.setInt(1, user.getPlayerId());
                try (ResultSet characterResultSet = characterStatement.executeQuery()) {
                    while (characterResultSet.next()) {
                        int characterId = characterResultSet.getInt("character_id");

                        user.getPlayerCharacterIds()[characterId] = characterResultSet.getLong("player_character_id");
                        user.getOwnedCharacters()[characterId] = true;
                        user.getLevels()[characterId] = characterResultSet.getInt("level");
                        user.getXps()[characterId] = characterResultSet.getInt("xp");
                        user.getLevelPercents()[characterId] = 0;
                        user.getPoints()[characterId] = characterResultSet.getInt("points");
                        user.getAddedPoints()[characterId] = gson.fromJson(characterResultSet.getString("additional_points"), short[].class);
                        user.getEquipData()[characterId] = new int[]{-1, -1, -1, -1, -1, -1};

                        int[] data = gson.fromJson(characterResultSet.getString("data"), int[].class);
                        for (int j = 0; j < data.length; j++) {
                            int key = data[j];
                            EquipmentChestEntry equip = user.getEquipmentByKey(key);
                            if (equip != null) {
                                if (equip.isExpired()) {
                                    equip.setInUse(false);
                                } else {
                                    user.getCharacterEquips()[characterId][j] = equip;
                                    user.getEquipData()[characterId][j] = equip.getKey();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    @Override
    public void updateOnline(boolean flag, int playerId) {
        String sql = "UPDATE `players` SET `is_online` = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, flag, playerId);
    }

    @Override
    public List<FriendEntry> getFriendsList(int playerId, List<Integer> friendIds) {
        List<FriendEntry> friendsList = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "u.username, u.is_locked, u.is_enabled, " +
                        "p.player_id, p.xu, p.is_online, p.equipment_chest, " +
                        "pc.character_id, pc.level, pc.data, " +
                        "cm.clan_id " +
                        "FROM players p " +
                        "INNER JOIN users u ON p.user_id = u.user_id " +
                        "INNER JOIN player_characters pc ON p.active_character_id = pc.player_character_id " +
                        "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                        "WHERE p.player_id IN ("
        );
        for (int i = 0; i < friendIds.size(); i++) {
            queryBuilder.append("?");
            if (i < friendIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < friendIds.size(); i++) {
                statement.setInt(i + 1, friendIds.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    if (resultSet.getBoolean("is_locked") || !resultSet.getBoolean("is_enabled")) {
                        continue;
                    }
                    FriendEntry friend = new FriendEntry();
                    friend.setId(resultSet.getInt("player_id"));
                    friend.setName(resultSet.getString("username"));
                    friend.setXu(resultSet.getInt("xu"));
                    friend.setActiveCharacterId(resultSet.getByte("character_id"));
                    friend.setClanId(resultSet.getShort("clan_id"));
                    friend.setOnline(resultSet.getByte("is_online"));
                    friend.setLevel(resultSet.getByte("level"));
                    friend.setLevelPt((byte) 0);
                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    EquipmentChestJson[] equipmentChests = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    friend.setData(CharacterRepository.getEquipData(equipmentChests, data, friend.getActiveCharacterId()));

                    friendsList.add(friend);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsList;
    }

    @Override
    public boolean existsByUserIdAndPassword(String userId, String oldPass) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM users WHERE user_id = ?")) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    //Check if the provided password matches the hashed password from the database
                    return BCrypt.checkpw(oldPass, hashedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void changePassword(String userId, String newPass) {
        String hashedPassword = BCrypt.hashpw(newPass, BCrypt.gensalt());
        String sql = "UPDATE `user` SET `password` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, hashedPassword, userId);
    }

    @Override
    public Integer findPlayerIdByUsername(String username) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.player_id FROM users u INNER JOIN players p ON u.user_id = p.user_id WHERE u.username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("player_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateLastOnline(LocalDateTime now, int playerId) {
        String sql = "UPDATE `players` SET `last_online` = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, now.toString(), playerId);
    }

    @Override
    public boolean createPlayerCharacter(int playerId, byte characterId) {
        Optional<Integer> result = HikariCPManager.getInstance().update("INSERT INTO `player_characters`(`player_id`, `character_id`) VALUES (?,?)", playerId, characterId);
        return result.isPresent();
    }

    @Override
    public PlayerCharacterEntry getPlayerCharacter(int playerId, byte characterId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `player_characters` pc WHERE pc.player_id = ? AND pc.character_id = ?")) {
            statement.setInt(1, playerId);
            statement.setByte(2, characterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.getInstance();
                    PlayerCharacterEntry characterEntry = new PlayerCharacterEntry();
                    characterEntry.setPlayerId(playerId);
                    characterEntry.setCharacterId(characterId);
                    characterEntry.setId(resultSet.getLong("player_character_id"));
                    characterEntry.setAdditionalPoints(gson.fromJson(resultSet.getString("additional_points"), short[].class));
                    characterEntry.setData(gson.fromJson(resultSet.getString("data"), int[].class));
                    characterEntry.setLevel(resultSet.getInt("level"));
                    characterEntry.setXp(resultSet.getInt("xp"));
                    characterEntry.setPoints(resultSet.getInt("points"));

                    return characterEntry;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createTransaction(TransactionType type, int amount, int playerId) {
        HikariCPManager.getInstance().update("INSERT INTO `transactions`(`transaction_type`, `amount`, `transaction_date`, `player_id`) values (?,?,?,?)", type, amount, LocalDateTime.now(), playerId);
    }

}
