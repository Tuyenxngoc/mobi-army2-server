package com.teamobi.mobiarmy2.dao;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;
import com.teamobi.mobiarmy2.dto.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.dto.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GiftCodeDAO {
    private final HikariCPManager hikariCPManager;

    public GiftCodeDAO(HikariCPManager hikariCPManager) {
        this.hikariCPManager = hikariCPManager;
    }

    public GiftCodeDTO findById(String code) {
        try (Connection connection = hikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM gift_codes WHERE code = ?")) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = GsonUtil.getInstance();
                    GiftCodeDTO giftCode = new GiftCodeDTO();
                    giftCode.setGiftCodeId(resultSet.getLong("gift_code_id"));
                    giftCode.setLimit(resultSet.getShort("usage_limit"));
                    giftCode.setExpiryDate(Utils.getLocalDateTimeFromTimestamp(resultSet, "expiration_date"));
                    giftCode.setXu(resultSet.getInt("xu"));
                    giftCode.setLuong(resultSet.getInt("luong"));
                    giftCode.setExp(resultSet.getInt("exp"));
                    giftCode.setItems(gson.fromJson(resultSet.getString("items"), SpecialItemChestJson[].class));
                    giftCode.setEquips(gson.fromJson(resultSet.getString("equips"), EquipmentChestJson[].class));
                    return giftCode;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void decrementUsageLimit(long giftCodeId) {
        // language=SQL
        String sql = "UPDATE gift_codes SET usage_limit = usage_limit - 1 WHERE gift_code_id = ?";
        hikariCPManager.update(sql, giftCodeId);
    }
}