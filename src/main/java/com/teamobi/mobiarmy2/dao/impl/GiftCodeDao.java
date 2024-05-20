package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.GiftCode.GetGiftCode;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GiftCodeDao implements IGiftCodeDao {

    @Override
    public GetGiftCode getGiftCode(String code) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM gift_code WHERE code = ?")) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    GetGiftCode giftCode = new GetGiftCode();
                    giftCode.setLimit(resultSet.getShort("usage_limit"));
                    giftCode.setCode(code);
                    String usedPlayerIdsJson = resultSet.getString("used_player_ids");
                    int[] usedPlayerIds = GsonUtil.GSON.fromJson(usedPlayerIdsJson, int[].class);
                    giftCode.setUsedPlayerIds(usedPlayerIds);
                    giftCode.setExpiryDate(resultSet.getTimestamp("expiration_date").toLocalDateTime());
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
    public void updateGiftCode(GetGiftCode giftCode) {
        String sql = "UPDATE gift_code SET usage_limit = usage_limit - 1, used_player_ids = ? WHERE code = ?";
        HikariCPManager.getInstance().update(sql, GsonUtil.GSON.toJson(giftCode.getUsedPlayerIds()), giftCode.getCode());
    }

}
