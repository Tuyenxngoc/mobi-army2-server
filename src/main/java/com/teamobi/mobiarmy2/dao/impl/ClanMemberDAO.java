package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanMemberDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanMemberDAO implements IClanMemberDAO {

    @Override
    public Byte count(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS member_count FROM clan_members cm WHERE cm.clan_id = ?")) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getByte("member_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
