package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.DataCharacter;
import com.teamobi.mobiarmy2.json.Equipment;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.PlayerLeaderboardEntry;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RankingDao implements IRankingDao {

    private final Gson gson;

    public RankingDao() {
        this.gson = new Gson();
    }

    private PlayerLeaderboardEntry createPlayerLeaderboardEntry(ResultSet resultSet, byte index, boolean isBonus) throws SQLException {
        PlayerLeaderboardEntry entry = new PlayerLeaderboardEntry();
        entry.setPlayerId(resultSet.getInt("player_id"));
        if (index <= 3 && isBonus) {
            entry.setUsername(GameString.topBonus(resultSet.getString("username"), Until.getStringNumber(CommonConstant.TOP_BONUS[index - 1])));
        } else {
            entry.setUsername(resultSet.getString("username"));
        }
        entry.setClanId(resultSet.getShort("clan_id"));

        byte nvUsed = resultSet.getByte("NVused");
        DataCharacter character = gson.fromJson(resultSet.getString("NV%s".formatted(nvUsed)), DataCharacter.class);
        Equipment[] equipment = gson.fromJson(resultSet.getString("ruongTrangBi"), Equipment[].class);

        entry.setNvUsed(nvUsed);
        entry.setLevel((byte) character.getLevel());
        entry.setLevelPt((byte) 0);
        entry.setIndex(index);
        entry.setData(NVData.getEquipData(equipment, character, nvUsed));

        return entry;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDanhDu() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT p.*, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "WHERE p.dvong > 0 " +
                            "ORDER BY dvong LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, true);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("dvong")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopCaoThu() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.xpMax LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("dvong")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDaiGiaXu() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.xu LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("xu")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDaiGiaLuong() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.luong LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("luong")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDanhDuTuan() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.point_event LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("dvong")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDaiGiaTuan() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT *, u.username " +
                            "FROM player p INNER JOIN user u ON p.user_id = u.user_id " +
                            "ORDER BY p.clanpoint LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Until.getStringNumber(resultSet.getInt("dvong")));
                    top.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

}
