package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGiftCodeDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;

import java.sql.*;

/**
 * @author tuyen
 */
public class GiftCodeDAO implements IGiftCodeDAO {

    @Override
    public GiftCodeDTO findById(String code) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM gift_codes WHERE code = ?")) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    GiftCodeDTO giftCode = new GiftCodeDTO();
                    giftCode.setId(resultSet.getLong("gift_code_id"));
                    giftCode.setLimit(resultSet.getShort("usage_limit"));
                    Timestamp expirationTimestamp = resultSet.getTimestamp("expiration_date");
                    if (expirationTimestamp != null) {
                        giftCode.setExpiryDate(expirationTimestamp.toLocalDateTime());
                    }
                    giftCode.setXu(resultSet.getInt("xu"));
                    giftCode.setLuong(resultSet.getInt("luong"));
                    giftCode.setExp(resultSet.getInt("exp"));

                    return giftCode;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void decrementUsageLimit(long giftCodeId) {
        // language=SQL
        String sql = "UPDATE gift_codes SET usage_limit = usage_limit - 1 WHERE gift_code_id = ?";
        HikariCPManager.getInstance().update(sql, giftCodeId);
    }

}
