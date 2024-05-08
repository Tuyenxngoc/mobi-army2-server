package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanDao implements IClanDao {

    @Override
    public Short getClanIcon(int clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT icon FROM clan WHERE id = ?")) {
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
    public void gopXu(int clanId, int xu) {
        String sql = "UPDATE clan SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu);
    }

    @Override
    public void gopLuong(int clanId, int luong) {
        String sql = "UPDATE clan SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong);
    }

    @Override
    public void gopClanContribute(String txtContribute, int userId) {
        String sql = "UPDATE `clanmem` " +
                "SET `n_contribute` = `n_contribute` + 1, " +
                "`contribute_time` = ?, " +
                "`contribute_text` = ? " +
                "WHERE `user` = ?";
        HikariCPManager.getInstance().update(sql, Until.toDateString(new Date()), txtContribute, userId);
    }

    @Override
    public ClanManager.ClanInfo getClanInfo(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet red = statement.executeQuery("SELECT * FROM clan WHERE id = ?")) {
                if (red.next()) {
                    ClanManager.ClanInfo clanInfo = new ClanManager.ClanInfo();
                    clanInfo.setId(red.getShort("id"));
                    clanInfo.setName(red.getString("name"));
                    clanInfo.setMemberCount((byte) red.getInt("mem"));
                    clanInfo.setMaxMemberCount((byte) red.getInt("memmax"));
                    clanInfo.setMasterName(red.getString("masterName"));
                    clanInfo.setXu(red.getInt("xu"));
                    clanInfo.setLuong(red.getInt("luong"));
                    clanInfo.setCup(red.getInt("cup"));
                    clanInfo.setXpUpLevel(red.getInt("xp"));
                    clanInfo.setDescription(red.getString("desc"));
                    clanInfo.setDateCreated(red.getString("dateCreat"));
                    red.getString("Item");
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
        List<ClanManager.ClanMemEntry> entries = null;
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet red = statement.executeQuery("SELECT * FROM clanmem WHERE id = ?")) {
                if (red.next()) {

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

}
