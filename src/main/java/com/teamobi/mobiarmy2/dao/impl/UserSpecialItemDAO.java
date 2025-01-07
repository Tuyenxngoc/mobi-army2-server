package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IUserSpecialItemDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserSpecialItemDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserSpecialItemDAO implements IUserSpecialItemDAO {
    @Override
    public List<UserSpecialItemDTO> findAllByUserId(int userId) {
        List<UserSpecialItemDTO> result = new ArrayList<>();

        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String query = "SELECT special_item_id, quantity FROM user_special_items WHERE user_id = ?";
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
}
