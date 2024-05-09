package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanDao implements IClanDao {

    @Override
    public Short getClanIcon(int clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT icon FROM clan WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getShort("icon");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void updateXu(int clanId, int xu) {
        String sql = "UPDATE clan SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu, clanId);
    }

    @Override
    public void updateLuong(int clanId, int luong) {
        String sql = "UPDATE clan SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong, clanId);
    }

    @Override
    public void gopClanContribute(String txtContribute, int playerId, int xu, int luong) {
        String sql = "UPDATE `clanmem` SET " +
                "`contribute_count` = `contribute_count` + 1, " +
                "`contribute_time` = ?, " +
                "`contribute_text` = ?, " +
                "`xu` = `xu` + ?, " +
                "`luong` = `luong` + ? " +
                "WHERE `player_id` = ?";
        HikariCPManager.getInstance().update(sql, Until.toDateString(new Date()), txtContribute, xu, luong, playerId);
    }

    @Override
    public ClanManager.ClanInfo getClanInfo(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT c.*, u.username " +
                             "FROM clan c " +
                             "INNER JOIN player p ON c.master_id = p.player_id " +
                             "INNER JOIN user u ON p.user_id = u.user_id " +
                             "WHERE c.clan_id = ?")
        ) {

            statement.setShort(1, clanId);

            try (ResultSet red = statement.executeQuery()) {
                if (red.next()) {
                    ClanManager.ClanInfo clanInfo = new ClanManager.ClanInfo();
                    clanInfo.setId(red.getShort("clan_id"));
                    clanInfo.setName(red.getString("name"));
                    clanInfo.setMemberCount((byte) red.getInt("mem"));
                    clanInfo.setMaxMemberCount((byte) red.getInt("mem_max"));
                    clanInfo.setMasterName(red.getString("username"));
                    clanInfo.setXu(red.getInt("xu"));
                    clanInfo.setLuong(red.getInt("luong"));
                    clanInfo.setCup(red.getInt("cup"));
                    clanInfo.setLevel((byte) 1);
                    clanInfo.setXpUpLevel(1000);
                    clanInfo.setDescription(red.getString("description"));
                    clanInfo.setDateCreated(red.getString("date_created"));
                    red.getString("item");
                    clanInfo.setItems(new ArrayList<>());
                    return clanInfo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClanManager.ClanMemEntry> getClanMember(short clanId, byte page) {
        List<ClanManager.ClanMemEntry> entries = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM clanmem WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ClanManager.ClanMemEntry entry = new ClanManager.ClanMemEntry();
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

}
