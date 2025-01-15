package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.EquipmentChest;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class UserDAO implements IUserDAO {

    public static String convertSpecialItemChestEntriesToJson(List<SpecialItemChest> specialItemChests) {
        List<SpecialItemChestJson> specialItemChestJsons = specialItemChests.stream().map(e -> {
            SpecialItemChestJson jsonItem = new SpecialItemChestJson();
            jsonItem.setId(e.getItem().getId());
            jsonItem.setQuantity(e.getQuantity());
            return jsonItem;
        }).toList();

        return GsonUtil.getInstance().toJson(specialItemChestJsons);
    }

    public static String convertEquipmentChestEntriesToJson(List<EquipmentChest> equipmentChests) {
        List<EquipmentChestJson> equipmentChestJsons = equipmentChests.stream().map(e -> {
            EquipmentChestJson jsonItem = new EquipmentChestJson();
            jsonItem.setKey(e.getKey());
            jsonItem.setEquipmentId(e.getEquipment().getEquipmentId());
            jsonItem.setInUse((byte) (e.isInUse() ? 1 : 0));
            jsonItem.setVipLevel(e.getVipLevel());
            jsonItem.setPurchaseDate(e.getPurchaseDate());
            jsonItem.setSlots(e.getSlots());
            jsonItem.setAddPoints(e.getAddPoints());
            jsonItem.setAddPercents(e.getAddPercents());
            return jsonItem;
        }).toList();

        return GsonUtil.getInstance().toJson(equipmentChestJsons);
    }

    @Override
    public Optional<Integer> create(String accountId, int xu, int luong) {
        Gson gson = GsonUtil.getInstance();

        byte[] fightItems = new byte[FightItemManager.FIGHT_ITEMS.size()];
        fightItems[0] = 99;
        fightItems[1] = 99;

        int[] missions = new int[MissionManager.MISSIONS.size()];
        byte[] missionLevels = new byte[missions.length];
        Arrays.fill(missionLevels, (byte) 1);

        int[] friends = {2};

        // language=SQL
        String sql = "INSERT INTO `users` " +
                "(account_id, xu, luong, created_date, " +
                "fight_items, missions, mission_levels, friends, equipment_chest, special_item_chest) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        return HikariCPManager.getInstance().update(
                sql,
                accountId,
                xu,
                luong,
                LocalDateTime.now(),
                gson.toJson(fightItems),
                gson.toJson(missions),
                gson.toJson(missionLevels),
                gson.toJson(friends),
                "[]",
                "[]"
        );
    }

    @Override
    public void update(User user) {
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
                "`point_event` = ? " +
                " WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql,
                gson.toJson(user.getFriends()),
                user.getXu(),
                user.getLuong(),
                user.getCup(),
                gson.toJson(user.getFightItems()),
                equipmentChestJson,
                specialItemChestJson,
                false,
                gson.toJson(user.getMission()),
                gson.toJson(user.getMissionLevel()),
                user.getTopEarningsXu(),
                user.getUserCharacterIds()[user.getActiveCharacterId()],
                user.getMaterialsPurchased(),
                user.getEquipmentPurchased(),
                user.getXpX2Time(),
                LocalDateTime.now(),
                user.getPointEvent(),
                user.getUserId()
        );
    }

    @Override
    public UserDTO findByAccountId(String accountId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String playerQuery = "SELECT " +
                    "u.user_id, u.xu, u.luong, u.cup, u.point_event, " +
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
                        userDTO.setPointEvent(resultSet.getInt("point_event"));
                        userDTO.setMaterialsPurchased(resultSet.getByte("materials_purchased"));
                        userDTO.setEquipmentPurchased(resultSet.getShort("equipment_purchased"));
                        userDTO.setChestLocked(resultSet.getBoolean("is_chest_locked"));
                        userDTO.setInvitationLocked(resultSet.getBoolean("is_invitation_locked"));

                        Object clanIdObj = resultSet.getObject("clan_id");
                        if (clanIdObj != null) {
                            userDTO.setClanId(((Number) clanIdObj).shortValue());
                        } else {
                            userDTO.setClanId(null);
                        }

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
                            userDTO.getEquipmentChest().add(equip);
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
                            userDTO.getSpecialItemChest().add(specialItemChest);
                        }

                        //Dữ liệu bạn bè
                        int[] friendsArray = gson.fromJson(resultSet.getString("friends"), int[].class);
                        List<Integer> friendsList = Arrays.stream(friendsArray)
                                .boxed().collect(Collectors.toList());
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setOnline(int userId, boolean online) {
        // language=SQL
        String sql = "UPDATE `users` SET `is_online` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, online, userId);
    }

    @Override
    public void setDailyRewardTime(int userId, LocalDateTime now) {
        // language=SQL
        String sql = "UPDATE `users` SET `daily_reward_time` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, now, userId);
    }

    @Override
    public List<FriendDTO> getFriendsList(int userId, List<Integer> friendIds) {
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

        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < friendIds.size(); i++) {
                statement.setInt(i + 1, friendIds.get(i));
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

    @Override
    public Optional<Integer> findUserIdByUsername(String username) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
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

}
