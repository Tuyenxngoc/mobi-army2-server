package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanItemDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.ClanItem;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanItemDAO implements IClanItemDAO {

    @Override
    public ClanItem[] getClanItems(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT item FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return GsonUtil.getInstance().fromJson(resultSet.getString("item"), ClanItem[].class);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void updateClanItems(short clanId, ClanItem[] items) {
        // language=SQL
        String sql = "UPDATE clans SET item = ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, GsonUtil.getInstance().toJson(items), clanId);
    }

}
