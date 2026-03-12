package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.server.HikariCPManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserGiftCodeDAO {
    private final HikariCPManager hikariCPManager;

    public UserGiftCodeDAO(HikariCPManager hikariCPManager) {
        this.hikariCPManager = hikariCPManager;
    }

    public boolean existsByUserId(int userId) {
        try (Connection connection = hikariCPManager.getConnection();
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

    public void create(Connection connection, long giftCodeId, int userId) throws SQLException {
        // language=SQL
        String sql = "INSERT INTO user_gift_codes (created_date, gift_code_id, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, LocalDateTime.now());
            statement.setLong(2, giftCodeId);
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }
}
