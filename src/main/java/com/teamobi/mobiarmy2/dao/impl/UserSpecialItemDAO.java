package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IUserSpecialItemDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserSpecialItemDTO;
import com.teamobi.mobiarmy2.model.SpecialItemChest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserSpecialItemDAO implements IUserSpecialItemDAO {

    @Override
    public List<UserSpecialItemDTO> findAllByUserId(int userId) {
        List<UserSpecialItemDTO> result = new ArrayList<>();

        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String query = "SELECT special_item_id, quantity FROM user_special_items WHERE user_id = ? AND (expiration_time IS NULL OR expiration_time > CURRENT_TIMESTAMP)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        UserSpecialItemDTO userSpecialItemDTO = new UserSpecialItemDTO();
                        userSpecialItemDTO.setSpecialItemId(resultSet.getByte("special_item_id"));
                        userSpecialItemDTO.setQuantity(resultSet.getShort("quantity"));

                        result.add(userSpecialItemDTO);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Optional<Integer> create(int userId, SpecialItemChest specialItemChest) {
        // language=SQL
        String sql =
                "INSERT INTO `user_special_items` " +
                        "(user_id, special_item_id, quantity, expiration_time) " +
                        "VALUES (?,?,?,?)";

        LocalDateTime expirationTime = null;
        if (specialItemChest.getItem().getExpirationDays() != 0) {
            expirationTime = LocalDateTime.now().plusDays(specialItemChest.getItem().getExpirationDays());
        }

        return HikariCPManager.getInstance().update(
                sql,
                userId,
                specialItemChest.getItem().getId(),
                specialItemChest.getQuantity(),
                expirationTime
        );
    }

    @Override
    public Optional<Integer> delete(int userId, byte specialItemId) {
        // language=SQL
        String sql = "DELETE FROM `user_special_items` WHERE user_id = ? AND special_item_id = ?";
        return HikariCPManager.getInstance().update(sql, userId, specialItemId);
    }

}
