package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IUserGiftCodeDAO;
import com.teamobi.mobiarmy2.server.HikariCPManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserGiftCodeDAO implements IUserGiftCodeDAO {

    @Override
    public boolean existsByUserId(int userId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_gift_codes WHERE user_id = ?")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void create(long giftCodeId, int userId) {
        // language=SQL
        String sql = "INSERT INTO user_gift_codes (created_date, gift_code_id, user_id) VALUES (?, ?, ?)";
        HikariCPManager.getInstance().update(sql, LocalDateTime.now(), giftCodeId, userId);
    }

}
