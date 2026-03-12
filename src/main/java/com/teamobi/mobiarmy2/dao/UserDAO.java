package com.teamobi.mobiarmy2.dao;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.dto.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.dto.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.entity.EquipmentChest;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UserDAO {
    private final HikariCPManager hikariCPManager;

    public UserDAO(HikariCPManager hikariCPManager) {
        this.hikariCPManager = hikariCPManager;
    }

    public static String convertSpecialItemChestEntriesToJson(Map<Byte, SpecialItemChest> specialItemChests) {
        List<SpecialItemChestJson> specialItemChestJsons = new ArrayList<>();

        for (Map.Entry<Byte, SpecialItemChest> entry : specialItemChests.entrySet()) {
            Byte id = entry.getKey();
            SpecialItemChest specialItemChest = entry.getValue();

            SpecialItemChestJson jsonItem = new SpecialItemChestJson();
            jsonItem.setId(id);
            jsonItem.setQuantity(specialItemChest.getQuantity());

            specialItemChestJsons.add(jsonItem);
        }

        return GsonUtil.getInstance().toJson(specialItemChestJsons);
    }

    public static String convertEquipmentChestEntriesToJson(Map<Integer, EquipmentChest> equipmentChests) {
        List<EquipmentChestJson> equipmentChestJsons = new ArrayList<>();

        for (Map.Entry<Integer, EquipmentChest> entry : equipmentChests.entrySet()) {
            Integer key = entry.getKey();
            EquipmentChest equipmentChest = entry.getValue();

            EquipmentChestJson jsonItem = new EquipmentChestJson();
            jsonItem.setKey(key);
            jsonItem.setEquipmentId(equipmentChest.getEquipment().getEquipmentId());
            jsonItem.setInUse((byte) (equipmentChest.isInUse() ? 1 : 0));
            jsonItem.setVipLevel(equipmentChest.getVipLevel());
            jsonItem.setPurchaseDate(equipmentChest.getPurchaseDate());
            jsonItem.setSlots(equipmentChest.getSlots());
            jsonItem.setAddPoints(equipmentChest.getAddPoints());
            jsonItem.setAddPercents(equipmentChest.getAddPercents());

            equipmentChestJsons.add(jsonItem);
        }

        return GsonUtil.getInstance().toJson(equipmentChestJsons);
    }

    public int create(Connection connection, String accountId, int xu, int luong) throws SQLException {
        Gson gson = GsonUtil.getInstance();

        byte[] fightItems = new byte[FightItemManager.FIGHT_ITEMS.size()];
        fightItems[0] = GameConstants.MAX_FIGHT_ITEM_QUANTITY;
        fightItems[1] = GameConstants.MAX_FIGHT_ITEM_QUANTITY;

        int[] missions = new int[MissionManager.MISSIONS.size()];
        byte[] missionLevels = new byte[missions.length];
        Arrays.fill(missionLevels, (byte) 1);

        int[] friends = {2};// Người đưa tin

        // language=SQL
        String sql = "INSERT INTO `users` " +
                "(account_id, xu, luong, created_date, " +
                "fight_items, missions, mission_levels, friends, equipment_chest, special_item_chest) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, accountId);
            statement.setInt(2, xu);
            statement.setInt(3, luong);
            statement.setObject(4, LocalDateTime.now());
            statement.setString(5, gson.toJson(fightItems));
            statement.setString(6, gson.toJson(missions));
            statement.setString(7, gson.toJson(missionLevels));
            statement.setString(8, gson.toJson(friends));
            statement.setString(9, "[]");
            statement.setString(10, "[]");

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // user_id
                }
            }
        }

        throw new SQLException("Insert succeeded but no generated key returned");
    }

    public void update(User user) {
        try (Connection connection = hikariCPManager.getConnection()) {
            update(connection, user);
        } catch (SQLException e) {
            log.error("Failed to get connection for user update: {}", e.getMessage(), e);
        }
    }

    public void update(Connection connection, User user) throws SQLException {
        Gson gson = GsonUtil.getInstance();

        String specialItemChestJson = convertSpecialItemChestEntriesToJson(user.getSpecialItemChest());
        String equipmentChestJson = convertEquipmentChestEntriesToJson(user.getEquipmentChest());

        // language=SQL
        String sql = "UPDATE `users` SET " +
                "`friends` = ?, " +
                "`xu` = ?, " +
                "`luong` = ?, " +
                "`cup` = ?, " +
                "`fight_items` = ?, " +
                "`equipment_chest` = ?, " +
                "`special_item_chest` = ? ," +
                "`is_online` = ?, " +
                "`missions` = ?, " +
                "`mission_levels` = ?, " +
                "`top_earnings_xu` = ?, " +
                "`active_user_character_id` = ?, " +
                "`materials_purchased` = ?, " +
                "`equipment_purchased` = ?, " +
                "`x2_xp_time` = ?, " +
                "`last_online` = ?, " +
                "`event_points` = ? " +
                " WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gson.toJson(user.getFriends()));
            statement.setInt(2, user.getXu());
            statement.setInt(3, user.getLuong());
            statement.setInt(4, user.getCup());
            statement.setString(5, gson.toJson(user.getFightItems()));
            statement.setString(6, equipmentChestJson);
            statement.setString(7, specialItemChestJson);
            statement.setBoolean(8, false);
            statement.setString(9, gson.toJson(user.getMission()));
            statement.setString(10, gson.toJson(user.getMissionLevel()));
            statement.setInt(11, user.getTopEarningsXu());
            statement.setLong(12, user.getUserCharacterIds()[user.getActiveCharacterId()]);
            statement.setByte(13, user.getMaterialsPurchased());
            statement.setShort(14, user.getEquipmentPurchased());
            statement.setObject(15, user.getXpX2Time());
            statement.setObject(16, LocalDateTime.now());
            statement.setInt(17, user.getEventPoint());
            statement.setInt(18, user.getUserId());
            statement.executeUpdate();
        }
    }

    public UserDTO findByAccountId(String accountId) {
        try (Connection connection = hikariCPManager.getConnection()) {
            return findByAccountId(connection, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserDTO findByAccountId(Connection connection, String accountId) {
        String playerQuery = "SELECT " +
                "u.user_id, u.xu, u.luong, u.cup, u.event_points, " +
                "u.materials_purchased, u.equipment_purchased, " +
                "u.fight_items, u.equipment_chest, u.special_item_chest, " +
                "u.friends, u.missions, u.mission_levels, " +
                "u.x2_xp_time, u.daily_reward_time, u.top_earnings_xu, " +
                "u.is_chest_locked, u.is_invitation_locked, " +
                "uc.character_id, uc.user_character_id, uc.level, " +
                "uc.xp, uc.points, uc.additional_points, uc.data, " +
                "cm.clan_id " +
                "FROM users u " +
                "LEFT JOIN user_characters uc ON u.active_user_character_id = uc.user_character_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE account_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(playerQuery)) {
            statement.setString(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.getInstance();
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(resultSet.getInt("user_id"));
                    userDTO.setXu(resultSet.getInt("xu"));
                    userDTO.setLuong(resultSet.getInt("luong"));
                    userDTO.setCup(resultSet.getInt("cup"));
                    userDTO.setActiveCharacterId(resultSet.getByte("character_id"));
                    userDTO.setEventPoint(resultSet.getInt("event_points"));
                    userDTO.setMaterialsPurchased(resultSet.getByte("materials_purchased"));
                    userDTO.setEquipmentPurchased(resultSet.getShort("equipment_purchased"));
                    userDTO.setChestLocked(resultSet.getBoolean("is_chest_locked"));
                    userDTO.setInvitationLocked(resultSet.getBoolean("is_invitation_locked"));
                    userDTO.setClanId(resultSet.getShort("clan_id"));

                    //Đọc dữ liệu trang bị
                    EquipmentChestJson[] equipmentChestJsons = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    for (EquipmentChestJson json : equipmentChestJsons) {
                        EquipmentChest equip = new EquipmentChest();
                        equip.setEquipment(EquipmentManager.getEquipment(json.getEquipmentId()));
                        if (equip.getEquipment() == null) {
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
                        userDTO.getEquipmentChest().put(equip.getKey(), equip);
                    }

                    //Đọc dữ liệu item
                    SpecialItemChestJson[] specialItemChestJsons = gson.fromJson(resultSet.getString("special_item_chest"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson item : specialItemChestJsons) {
                        SpecialItemChest specialItemChest = new SpecialItemChest();
                        specialItemChest.setItem(SpecialItemManager.getSpecialItemById(item.getId()));
                        if (specialItemChest.getItem() == null) {
                            continue;
                        }
                        specialItemChest.setQuantity(item.getQuantity());
                        userDTO.getSpecialItemChest().put(specialItemChest.getItem().getId(), specialItemChest);
                    }

                    //Dữ liệu bạn bè
                    int[] friendsArray = gson.fromJson(resultSet.getString("friends"), int[].class);
                    Set<Integer> friendsList = Arrays.stream(friendsArray)
                            .boxed().collect(Collectors.toSet());
                    userDTO.setFriends(friendsList);

                    //Đọc dữ liệu item chiến đấu
                    byte[] items = gson.fromJson(resultSet.getString("fight_items"), byte[].class);
                    int desiredSizeItem = FightItemManager.FIGHT_ITEMS.size();
                    userDTO.setItems(
                            items.length != desiredSizeItem
                                    ? Utils.adjustArray(items, desiredSizeItem, (byte) 0)
                                    : items
                    );

                    //Dữ liệu nhiệm vụ
                    int[] missions = gson.fromJson(resultSet.getString("missions"), int[].class);
                    int desiredSizeMission = MissionManager.MISSIONS.size();
                    userDTO.setMission(
                            missions.length != desiredSizeMission
                                    ? Utils.adjustArray(missions, desiredSizeMission, 0)
                                    : missions
                    );

                    //Dữ liệu cấp nhiệm vụ
                    byte[] missionLevels = gson.fromJson(resultSet.getString("mission_levels"), byte[].class);
                    userDTO.setMissionLevel(
                            missionLevels.length != desiredSizeMission
                                    ? Utils.adjustArray(missionLevels, desiredSizeMission, (byte) 1)
                                    : missionLevels
                    );

                    userDTO.setXpX2Time(Utils.getLocalDateTimeFromTimestamp(resultSet, "x2_xp_time"));
                    userDTO.setDailyRewardTime(Utils.getLocalDateTimeFromTimestamp(resultSet, "daily_reward_time"));
                    userDTO.setTopEarningsXu(resultSet.getInt("top_earnings_xu"));

                    return userDTO;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setOnline(int userId, boolean online) {
        // language=SQL
        String sql = "UPDATE `users` SET `is_online` = ? WHERE user_id = ?";
        hikariCPManager.update(sql, online, userId);
    }

    public void setDailyRewardTime(int userId, LocalDateTime now) {
        // language=SQL
        String sql = "UPDATE `users` SET `daily_reward_time` = ? WHERE user_id = ?";
        hikariCPManager.update(sql, now, userId);
    }

    public List<FriendDTO> getFriendsList(int userId, Set<Integer> friendIds) {
        List<FriendDTO> friendsList = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "a.username, a.is_locked, a.is_enabled, " +
                        "u.user_id, u.xu, u.is_online, u.equipment_chest, " +
                        "uc.character_id, uc.level, uc.xp, uc.data, " +
                        "cm.clan_id " +
                        "FROM users u " +
                        "INNER JOIN accounts a ON u.account_id = a.account_id " +
                        "INNER JOIN user_characters uc ON u.active_user_character_id = uc.user_character_id " +
                        "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                        "WHERE a.is_locked = 0 AND a.is_enabled = 1 AND " +
                        "u.user_id IN ("
        );
        for (int i = 0; i < friendIds.size(); i++) {
            queryBuilder.append("?");
            if (i < friendIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        try (Connection connection = hikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
            int parameterIndex = 1;
            for (int friendId : friendIds) {
                statement.setInt(parameterIndex++, friendId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    FriendDTO friend = new FriendDTO();
                    friend.setUserId(resultSet.getInt("user_id"));
                    friend.setName(resultSet.getString("username"));
                    friend.setXu(resultSet.getInt("xu"));
                    friend.setActiveCharacterId(resultSet.getByte("character_id"));
                    friend.setClanId(resultSet.getShort("clan_id"));
                    friend.setOnline(resultSet.getByte("is_online"));

                    int currentLevel = resultSet.getInt("level");
                    int currentXp = resultSet.getInt("xp");
                    int requiredXpCurrentLevel = UserXpManager.getRequiredXpLevel(currentLevel - 1);
                    int requiredXpNextLevel = UserXpManager.getRequiredXpLevel(currentLevel);
                    int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                    int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    friend.setLevel((byte) currentLevel);
                    friend.setLevelPt(levelPercent);

                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    EquipmentChestJson[] equipmentChests = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    friend.setData(EquipmentManager.getEquipmentIndexes(equipmentChests, data, friend.getActiveCharacterId()));

                    friendsList.add(friend);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsList;
    }

    public Optional<Integer> findUserIdByUsername(String username) {
        try (Connection connection = hikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT u.user_id FROM accounts a LEFT JOIN users u ON a.account_id = u.account_id WHERE a.username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Integer> getUserRankByCup(int cup) {
        try (Connection connection = hikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS top FROM users WHERE cup > ?")) {
            statement.setInt(1, cup);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt("top") + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
