package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserDAO;
import com.teamobi.mobiarmy2.dao.IUserEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.MissionManager;
import com.teamobi.mobiarmy2.server.PlayerXpManager;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author tuyen
 */
public class UserDAO implements IUserDAO {

    private final IUserEquipmentDAO userEquipmentDAO;

    public UserDAO(IUserEquipmentDAO userEquipmentDAO) {
        this.userEquipmentDAO = userEquipmentDAO;
    }

    @Override
    public Optional<Integer> create(String accountId, int xu, int luong) {
        byte[] fightItems = new byte[FightItemManager.FIGHT_ITEMS.size()];
        fightItems[0] = 99;
        fightItems[1] = 99;

        int[] missions = new int[MissionManager.MISSIONS.size()];
        byte[] missionLevels = new byte[missions.length];

        Gson gson = GsonUtil.getInstance();
        // language=SQL
        String sql = "INSERT INTO `users`(account_id, xu, luong, created_date, last_modified_date, fight_items, missions, mission_levels) VALUES (?,?,?,?,?,?,?,?)";
        return HikariCPManager.getInstance().update(sql, accountId, xu, luong, LocalDateTime.now(), LocalDateTime.now(), gson.toJson(fightItems), gson.toJson(missions), gson.toJson(missionLevels));
    }

    @Override
    public void update(User user) {
        // language=SQL
        String sql = "UPDATE `users` SET " +
                "`xu` = ?, " +
                "`luong` = ?, " +
                "`cup` = ?, " +
                "`is_online` = ?, " +
                "`materials_purchased` = ?, " +
                "`x2_xp_time` = ?, " +
                "`point_event` = ? " +
                //...//
                " WHERE user_id = ?";

        HikariCPManager.getInstance().update(sql,
                user.getXu(),
                user.getLuong(),
                user.getCup(),
                false,
                user.getMaterialsPurchased(),
                user.getXpX2Time(),
                user.getPointEvent(),
                //...//
                user.getUserId());
    }

    @Override
    public UserDTO findByAccountId(String accountId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String query = "SELECT u.*, cm.clan_id FROM users u LEFT JOIN clan_members cm ON u.user_id = cm.user_id WHERE account_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, accountId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Gson gson = GsonUtil.getInstance();
                        UserDTO userDTO = new UserDTO();
                        userDTO.setUserId(resultSet.getInt("user_id"));
                        Object clanIdObj = resultSet.getObject("clan_id");
                        if (clanIdObj != null) {
                            userDTO.setClanId(((Number) clanIdObj).shortValue());
                        } else {
                            userDTO.setClanId(null);
                        }
                        userDTO.setX2XpTime(Utils.getLocalDateTimeFromTimestamp(resultSet, "x2_xp_time"));
                        userDTO.setLastOnline(Utils.getLocalDateTimeFromTimestamp(resultSet, "last_online"));
                        userDTO.setXu(resultSet.getInt("xu"));
                        userDTO.setLuong(resultSet.getInt("luong"));
                        userDTO.setCup(resultSet.getInt("cup"));
                        userDTO.setPointEvent(resultSet.getInt("point_event"));
                        userDTO.setMaterialsPurchased(resultSet.getByte("materials_purchased"));
                        userDTO.setChestLocked(resultSet.getBoolean("is_chest_locked"));
                        userDTO.setInvitationLocked(resultSet.getBoolean("is_invitation_locked"));

                        //Dữ liệu item chiến
                        byte[] fightItems = gson.fromJson(resultSet.getString("fight_items"), byte[].class);
                        int desiredSizeFightItem = FightItemManager.FIGHT_ITEMS.size();
                        userDTO.setFightItems(
                                fightItems.length != desiredSizeFightItem
                                        ? Utils.adjustArray(fightItems, desiredSizeFightItem, (byte) 0)
                                        : fightItems
                        );

                        //Dữ liệu nhiệm vụ
                        int[] missions = gson.fromJson(resultSet.getString("missions"), int[].class);
                        int desiredSizeMission = MissionManager.MISSIONS.size();
                        userDTO.setMissions(
                                missions.length != desiredSizeMission
                                        ? Utils.adjustArray(missions, desiredSizeMission, 0)
                                        : missions
                        );

                        //Dữ liệu cấp nhiệm vụ
                        byte[] missionLevels = gson.fromJson(resultSet.getString("mission_levels"), byte[].class);
                        userDTO.setMissionLevels(
                                missionLevels.length != desiredSizeMission
                                        ? Utils.adjustArray(missionLevels, desiredSizeMission, (byte) 1)
                                        : missionLevels
                        );

                        //Dữ liệu bạn bè
                        int[] friendsArray = gson.fromJson(resultSet.getString("friends"), int[].class);
                        List<Integer> friendsList = Arrays.stream(friendsArray)
                                .boxed().toList();
                        userDTO.setFriends(friendsList);

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
    public void updateOnline(boolean flag, int userId) {
        // language=SQL
        String sql = "UPDATE `users` SET `is_online` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, flag, userId);
    }

    @Override
    public List<FriendDTO> getFriendsList(int playerId, List<Integer> friendIds) {
        List<FriendDTO> friendsList = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "a.username, a.is_locked, a.is_enabled, " +
                        "u.user_id, u.xu, u.is_online, " +
                        "uc.character_id, uc.level, uc.xp, uc.data, " +
                        "cm.clan_id " +
                        "FROM users u " +
                        "INNER JOIN accounts a ON u.account_id = a.account_id " +
                        "INNER JOIN user_characters uc ON u.user_id = uc.user_id " +
                        "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                        "WHERE u.user_id IN ("
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
                    FriendDTO friend = new FriendDTO();
                    friend.setUserId(resultSet.getInt("user_id"));
                    friend.setName(resultSet.getString("username"));
                    friend.setXu(resultSet.getInt("xu"));
                    friend.setActiveCharacterId(resultSet.getByte("character_id"));
                    friend.setClanId(resultSet.getShort("clan_id"));
                    friend.setOnline(resultSet.getByte("is_online"));

                    int currentLevel = resultSet.getInt("level");
                    int currentXp = resultSet.getInt("xp");
                    int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
                    int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);
                    int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                    int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    friend.setLevel((byte) currentLevel);
                    friend.setLevelPt(levelPercent);

                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    Map<Integer, UserEquipmentDTO> userEquipmentDTOS = userEquipmentDAO.findAllByIdIn(data);
                    friend.setData(EquipmentManager.getEquipmentIndexes(userEquipmentDTOS, data, friend.getActiveCharacterId()));

                    friendsList.add(friend);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsList;
    }

    @Override
    public Integer findUserIdByUsername(String username) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT u.user_id FROM accounts a LEFT JOIN users u ON a.account_id = u.account_id WHERE a.username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateLastOnline(LocalDateTime time, int userId) {
        // language=SQL
        String sql = "UPDATE `users` SET `last_online` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, time, userId);
    }
}
