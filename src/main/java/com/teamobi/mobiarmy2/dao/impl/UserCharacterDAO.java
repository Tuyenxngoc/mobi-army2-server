package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserCharacterDAO;
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

public class UserCharacterDAO implements IUserCharacterDAO {

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

    @Override
    public List<UserCharacterDTO> findAllByUserId(int userId) {
        List<UserCharacterDTO> result = new ArrayList<>();

        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
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

    @Override
    public UserCharacterDTO findByUserIdAndCharacterId(int userId, byte characterId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `user_characters` WHERE user_id = ? AND character_id = ?")) {
            statement.setInt(1, userId);
            statement.setByte(2, characterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToUserCharacterDTO(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Optional<Integer> create(int userId, byte characterId) {
        // language=SQL
        String sql = "INSERT INTO `user_characters`(`user_id`, `character_id`) VALUES (?,?)";
        return HikariCPManager.getInstance().update(sql, userId, characterId);
    }

    @Override
    public void update(UserCharacterDTO userCharacterDTO) {
        Gson gson = GsonUtil.getInstance();

        // language=SQL
        String sql = "UPDATE user_characters SET level = ?, points = ?, xp = ?, data = ?, additional_points = ? WHERE user_id = ? AND character_id = ?";
        HikariCPManager.getInstance().update(
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

    @Override
    public void updateAll(List<UserCharacterDTO> userCharacterDTOs) {
        Gson gson = GsonUtil.getInstance();

        // language=SQL
        String sql = "UPDATE user_characters SET level = ?, points = ?, xp = ?, data = ?, additional_points = ? WHERE user_id = ? AND character_id = ?";

        HikariCPManager.getInstance().executeBatch(sql, statement -> {
            for (UserCharacterDTO userCharacterDTO : userCharacterDTOs) {
                try {
                    statement.setInt(1, userCharacterDTO.getLevel());
                    statement.setInt(2, userCharacterDTO.getPoints());
                    statement.setInt(3, userCharacterDTO.getXp());
                    statement.setString(4, gson.toJson(userCharacterDTO.getData()));
                    statement.setString(5, gson.toJson(userCharacterDTO.getAdditionalPoints()));
                    statement.setInt(6, userCharacterDTO.getUserId());
                    statement.setByte(7, userCharacterDTO.getCharacterId());
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
