package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.HikariCPManager;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.GiftCodeEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * @author tuyen
 */
public class GiftCodeDao implements IGiftCodeDao {

    @Override
    public GiftCodeEntry getGiftCode(String code, int playerId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT gc.gift_code_id, gc.usage_limit, gc.expiration_date, gc.xu, gc.luong, gc.exp, gc.items, gc.equips, " +
                             "IF(pgc.player_id IS NOT NULL, TRUE, FALSE) AS used " +
                             "FROM gift_codes gc " +
                             "LEFT JOIN player_gift_codes pgc ON gc.gift_code_id = pgc.gift_code_id AND pgc.player_id = ? " +
                             "WHERE gc.code = ?"
             )) {
            statement.setInt(1, playerId);
            statement.setString(2, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.getInstance();
                    GiftCodeEntry giftCode = new GiftCodeEntry();
                    giftCode.setUsed(resultSet.getBoolean("used"));
                    if (!giftCode.isUsed()) {
                        giftCode.setId(resultSet.getLong("gift_code_id"));
                        giftCode.setLimit(resultSet.getShort("usage_limit"));
                        Timestamp expirationTimestamp = resultSet.getTimestamp("expiration_date");
                        if (expirationTimestamp != null) {
                            giftCode.setExpiryDate(expirationTimestamp.toLocalDateTime());
                        }
                        giftCode.setXu(resultSet.getInt("xu"));
                        giftCode.setLuong(resultSet.getInt("luong"));
                        giftCode.setExp(resultSet.getInt("exp"));
                        giftCode.setItems(gson.fromJson(resultSet.getString("items"), SpecialItemChestJson[].class));
                        giftCode.setEquips(gson.fromJson(resultSet.getString("equips"), EquipmentChestJson[].class));
                    }
                    return giftCode;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void decrementGiftCodeUsageLimit(long giftCodeId) {
        String sql = "UPDATE gift_codes SET usage_limit = usage_limit - 1 WHERE gift_code_id = ?";
        HikariCPManager.getInstance().update(sql, giftCodeId);

    }

    @Override
    public void logGiftCodeRedemption(long giftCodeId, int playerId) {
        String sql = "INSERT INTO player_gift_codes (redeem_time, gift_code_id, player_id) VALUES (?, ?, ?)";
        HikariCPManager.getInstance().update(sql, LocalDateTime.now(), giftCodeId, playerId);

    }

}
