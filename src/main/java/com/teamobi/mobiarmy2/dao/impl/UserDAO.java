package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.TransactionType;
import com.teamobi.mobiarmy2.dao.IUserDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.PlayerCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.model.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.util.GsonUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author tuyen
 */
public class UserDAO implements IUserDAO {

    @Override
    public Optional<Integer> create(String accountId, int xu, int luong) {
        // language=SQL
        String sql = "INSERT INTO `users`(`account_id`, `xu`, `luong`, `created_date`, `last_modified_date`) VALUES (?,?,?,?,?)";
        return HikariCPManager.getInstance().update(sql, accountId, xu, luong, LocalDateTime.now(), LocalDateTime.now());
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
                user.getFriends().toString(),
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
            String query = "SELECT * FROM users WHERE account_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, accountId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        UserDTO userDTO = new UserDTO();
                        userDTO.setUserId(resultSet.getInt("user_id"));
                        userDTO.setX2XpTime(
                                resultSet.getTimestamp("x2_xp_time") != null
                                        ? resultSet.getTimestamp("x2_xp_time").toLocalDateTime()
                                        : null
                        );
                        userDTO.setXu(resultSet.getInt("xu"));
                        userDTO.setLuong(resultSet.getInt("luong"));
                        userDTO.setCup(resultSet.getInt("cup"));
                        userDTO.setPointEvent(resultSet.getInt("point_event"));
                        userDTO.setMaterialsPurchased(resultSet.getByte("materials_purchased"));
                        userDTO.setChestLocked(resultSet.getBoolean("is_chest_locked"));
                        userDTO.setInvitationLocked(resultSet.getBoolean("is_invitation_locked"));
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
                    FriendDTO friend = new FriendDTO();
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
                    friend.setData(EquipmentManager.getEquipmentData(equipmentChests, data, friend.getActiveCharacterId()));

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
        // language=SQL
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
    public void updateLastOnline(LocalDateTime time, int playerId) {
        // language=SQL
        String sql = "UPDATE `players` SET `last_online` = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, time.toString(), playerId);
    }

    @Override
    public boolean createPlayerCharacter(int playerId, byte characterId) {
        // language=SQL
        Optional<Integer> result = HikariCPManager.getInstance().update("INSERT INTO `player_characters`(`player_id`, `character_id`) VALUES (?,?)", playerId, characterId);
        return result.isPresent();
    }

    @Override
    public PlayerCharacterDTO getPlayerCharacter(int playerId, byte characterId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `player_characters` pc WHERE pc.player_id = ? AND pc.character_id = ?")) {
            statement.setInt(1, playerId);
            statement.setByte(2, characterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.getInstance();
                    PlayerCharacterDTO characterEntry = new PlayerCharacterDTO();
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
        // language=SQL
        HikariCPManager.getInstance().update("INSERT INTO `transactions`(`transaction_type`, `amount`, `transaction_date`, `player_id`) values (?,?,?,?)", type, amount, LocalDateTime.now(), playerId);
    }

}
