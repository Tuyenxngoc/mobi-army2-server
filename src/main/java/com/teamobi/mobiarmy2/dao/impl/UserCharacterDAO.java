package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserCharacterDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserCharacterDAO implements IUserCharacterDAO {

    private UserCharacterDTO mapToUserCharacterDTO(ResultSet resultSet) throws SQLException {
        Gson gson = GsonUtil.getInstance();
        UserCharacterDTO userCharacterDTO = new UserCharacterDTO();
        userCharacterDTO.setCharacterId(resultSet.getByte("character_id"));
        userCharacterDTO.setAdditionalPoints(gson.fromJson(resultSet.getString("additional_points"), short[].class));
        userCharacterDTO.setData(gson.fromJson(resultSet.getString("data"), int[].class));
        userCharacterDTO.setLevel(resultSet.getInt("level"));
        userCharacterDTO.setPoints(resultSet.getInt("points"));
        userCharacterDTO.setXp(resultSet.getInt("xp"));
        userCharacterDTO.setActive(resultSet.getBoolean("is_active"));
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
    public Optional<Integer> create(int userId, byte characterId, boolean isActive) {
        // language=SQL
        String sql = "INSERT INTO `user_characters`(`user_id`, `character_id`, `is_active`) VALUES (?,?,?)";
        return HikariCPManager.getInstance().update(sql, userId, characterId, isActive);
    }
}
