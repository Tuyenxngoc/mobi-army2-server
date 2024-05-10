package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.DataCharacter;
import com.teamobi.mobiarmy2.json.Equipment;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.server.BangXHManager;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RankingDao implements IRankingDao {

    private void readInfo(List<BangXHManager.BangXHEntry> top, ResultSet resultSet, boolean isBonus) throws SQLException {
        byte jjjjj = 1;
        Gson gson = new Gson();
        while (resultSet.next()) {
            BangXHManager.BangXHEntry bangXHEntry = new BangXHManager.BangXHEntry();
            bangXHEntry.setPlayerId(resultSet.getInt("player_id"));
            if (jjjjj <= 3 && isBonus) {
                bangXHEntry.setUsername(GameString.topBonus(resultSet.getString("username"), Until.getStringNumber(CommonConstant.TOP_BONUS_DD[jjjjj - 1])));
            } else {
                bangXHEntry.setUsername(resultSet.getString("username"));
            }
            bangXHEntry.setClanId(resultSet.getShort("clan_id"));

            byte nvUsed = resultSet.getByte("NVused");
            DataCharacter character = gson.fromJson(resultSet.getString("NV%s".formatted(nvUsed)), DataCharacter.class);
            Equipment[] trangBi = gson.fromJson(resultSet.getString("ruongTrangBi"), Equipment[].class);

            bangXHEntry.setNvUsed(nvUsed);
            bangXHEntry.setLevel((byte) character.getLevel());
            bangXHEntry.setLevelPt((byte) 0);
            bangXHEntry.setIndex(jjjjj);
            bangXHEntry.setData(NVData.getEquipData(trangBi, character, nvUsed));
            bangXHEntry.setDetail(Until.getStringNumber(resultSet.getInt("dvong")));

            top.add(bangXHEntry);
            jjjjj++;
        }
    }

    @Override
    public List<BangXHManager.BangXHEntry> getTopDanhDu() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY dvong LIMIT 100"
            )) {
                readInfo(top, resultSet, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<BangXHManager.BangXHEntry> getTopCaoThu() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.xpMax LIMIT 100"
            )) {
                readInfo(top, resultSet, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }


    @Override
    public List<BangXHManager.BangXHEntry> getTopDaiGiaXu() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.xu LIMIT 100"
            )) {
                readInfo(top, resultSet, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<BangXHManager.BangXHEntry> getTopDaiGiaLuong() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.luong LIMIT 100"
            )) {
                readInfo(top, resultSet, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<BangXHManager.BangXHEntry> getTopDanhDuTuan() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.point_event LIMIT 100"
            )) {
                readInfo(top, resultSet, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<BangXHManager.BangXHEntry> getTopDaiGiaTuan() {
        List<BangXHManager.BangXHEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.clanpoint LIMIT 100"
            )) {
                readInfo(top, resultSet, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

}
