package com.teamobi.mobiarmy2.dao;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserCharacterDAO {
    private final HikariCPManager hikariCPManager;

    public UserCharacterDAO(HikariCPManager hikariCPManager) {
        this.hikariCPManager = hikariCPManager;
    }

    private static UserCharacterDTO mapToUserCharacterDTO(ResultSet resultSet) throws SQLException {
        Gson gson = GsonUtil.getInstance();
        UserCharacterDTO userCharacterDTO = new UserCharacterDTO();
        userCharacterDTO.setUserCharacterId(resultSet.getLong("user_character_id"));
        userCharacterDTO.setCharacterId(resultSet.getByte("character_id"));
        userCharacterDTO.setUserId(resultSet.getInt("user_id"));
        userCharacterDTO.setAdditionalPoints(gson.fromJson(resultSet.getString("additional_points"), short[].class));
        userCharacterDTO.setData(gson.fromJson(resultSet.getString("data"), int[].class));
        userCharacterDTO.setLevel(resultSet.getInt("level"));
        userCharacterDTO.setPoints(resultSet.getInt("points"));
        userCharacterDTO.setXp(resultSet.getInt("xp"));
        return userCharacterDTO;
    }

    public List<UserCharacterDTO> findAllByUserId(int userId) {
        List<UserCharacterDTO> result = new ArrayList<>();

        try (Connection connection = hikariCPManager.getConnection()) {
            String query = "SELECT * FROM user_characters WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(mapToUserCharacterDTO(resultSet));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public UserCharacterDTO findByUserIdAndCharacterId(int userId, byte characterId) {
        try (Connection connection = hikariCPManager.getConnection()) {
            return findByUserIdAndCharacterId(connection, userId, characterId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserCharacterDTO findByUserIdAndCharacterId(Connection connection, int userId, byte characterId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `user_characters` WHERE user_id = ? AND character_id = ?")) {
            statement.setInt(1, userId);
            statement.setByte(2, characterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToUserCharacterDTO(resultSet);
                }
            }
        }
        return null;
    }

    public Optional<Integer> create(int userId, byte characterId) {
        try (Connection connection = hikariCPManager.getConnection()) {
            return create(connection, userId, characterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Integer> create(Connection connection, int userId, byte characterId) throws SQLException {
        // language=SQL
        String sql = "INSERT INTO `user_characters`(`user_id`, `character_id`) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setByte(2, characterId);
            int rowsUpdated = statement.executeUpdate();
            return Optional.of(rowsUpdated);
        }
    }

    public void update(UserCharacterDTO userCharacterDTO) {
        Gson gson = GsonUtil.getInstance();

        // language=SQL
        String sql = "UPDATE user_characters SET level = ?, points = ?, xp = ?, data = ?, additional_points = ? WHERE user_id = ? AND character_id = ?";
        hikariCPManager.update(
                sql,
                userCharacterDTO.getLevel(),
                userCharacterDTO.getPoints(),
                userCharacterDTO.getXp(),
                gson.toJson(userCharacterDTO.getData()),
                gson.toJson(userCharacterDTO.getAdditionalPoints()),
                userCharacterDTO.getUserId(),
                userCharacterDTO.getCharacterId()
        );
    }

    public void updateAll(Connection connection, List<UserCharacterDTO> userCharacterDTOs) throws SQLException {
        Gson gson = GsonUtil.getInstance();

        // language=SQL
        String sql = "UPDATE user_characters SET level = ?, points = ?, xp = ?, data = ?, additional_points = ? WHERE user_id = ? AND character_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (UserCharacterDTO userCharacterDTO : userCharacterDTOs) {
                statement.setInt(1, userCharacterDTO.getLevel());
                statement.setInt(2, userCharacterDTO.getPoints());
                statement.setInt(3, userCharacterDTO.getXp());
                statement.setString(4, gson.toJson(userCharacterDTO.getData()));
                statement.setString(5, gson.toJson(userCharacterDTO.getAdditionalPoints()));
                statement.setInt(6, userCharacterDTO.getUserId());
                statement.setByte(7, userCharacterDTO.getCharacterId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
