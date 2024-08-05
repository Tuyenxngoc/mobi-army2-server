package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.entry.GiftCodeEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.*;
import java.util.Arrays;

public class GiftCodeDao implements IGiftCodeDao {

    @Override
    public GiftCodeEntry getGiftCode(String code) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT gc.usage_limit, gc,used_player_ids, gc.expiration_date, gc.reward FROM gift_codes gc WHERE gc.code = ?")) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    GiftCodeEntry giftCode = new GiftCodeEntry();
                    giftCode.setLimit(resultSet.getShort("usage_limit"));
                    giftCode.setCode(code);
                    giftCode.setUsedPlayerIds(GsonUtil.GSON.fromJson(resultSet.getString("used_player_ids"), int[].class));
                    Timestamp expirationTimestamp = resultSet.getTimestamp("expiration_date");
                    if (expirationTimestamp != null) {
                        giftCode.setExpiryDate(expirationTimestamp.toLocalDateTime());
                    }
                    giftCode.setReward(resultSet.getString("reward"));

                    return giftCode;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateGiftCode(String code, int playerId) {
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        try {
            connection = HikariCPManager.getInstance().getConnection();
            connection.setAutoCommit(false);

            selectStatement = connection.prepareStatement("SELECT used_player_ids FROM gift_codes WHERE code = ? FOR UPDATE");
            selectStatement.setString(1, code);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.GSON;
                    int[] usedPlayerIds = gson.fromJson(resultSet.getString("used_player_ids"), int[].class);
                    int[] newUsedPlayerIds = Arrays.copyOf(usedPlayerIds, usedPlayerIds.length + 1);
                    newUsedPlayerIds[newUsedPlayerIds.length - 1] = playerId;

                    updateStatement = connection.prepareStatement("UPDATE gift_codes SET usage_limit = usage_limit - 1, used_player_ids = ? WHERE code = ?");
                    updateStatement.setString(1, gson.toJson(newUsedPlayerIds));
                    updateStatement.setString(2, code);
                    updateStatement.executeUpdate();

                    connection.commit();  // Commit transaction
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
                if (updateStatement != null) {
                    updateStatement.close();
                }
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
