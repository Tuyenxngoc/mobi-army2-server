package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.CharacterJson;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.entry.user.PlayerLeaderboardEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class RankingDao implements IRankingDao {

    private PlayerLeaderboardEntry createPlayerLeaderboardEntry(ResultSet resultSet, byte index, boolean isBonus) throws SQLException {
        PlayerLeaderboardEntry entry = new PlayerLeaderboardEntry();
        Gson gson = GsonUtil.GSON;

        entry.setPlayerId(resultSet.getInt("player_id"));
        if (index <= 3 && isBonus) {
            entry.setUsername(GameString.topBonus(resultSet.getString("username"), Utils.getStringNumber(CommonConstant.TOP_BONUS[index - 1])));
        } else {
            entry.setUsername(resultSet.getString("username"));
        }
        entry.setClanId(resultSet.getShort("clan_id"));

        byte nvUsed = resultSet.getByte("nv_used");
        CharacterJson character = gson.fromJson(resultSet.getString("NV%s".formatted(nvUsed + 1)), CharacterJson.class);
        EquipmentChestJson[] equipmentData = gson.fromJson(resultSet.getString("ruongTrangBi"), EquipmentChestJson[].class);

        entry.setNvUsed(nvUsed);
        entry.setLevel((byte) character.getLevel());
        entry.setLevelPt((byte) 0);
        entry.setIndex(index);
        entry.setData(NVData.getEquipData(equipmentData, character, nvUsed));

        return entry;
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopDanhDu() {
        List<PlayerLeaderboardEntry> top = new ArrayList<>(100);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT p.*, u.username " +
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.dvong > 0 " +
                            "ORDER BY p.dvong DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, true);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("dvong")));
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
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.xpMax > 0 " +
                            "ORDER BY p.xpMax DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("dvong")));
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
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.xu > 0 " +
                            "ORDER BY p.xu DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("xu")));
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
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.luong > 0 " +
                            "ORDER BY p.luong DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("luong")));
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
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.weekly_earnings_cup IS NOT NULL AND p.weekly_earnings_cup > 0 " +
                            "ORDER BY p.weekly_earnings_cup DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("weekly_earnings_cup")));
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
                            "FROM players p INNER JOIN users u ON p.user_id = u.user_id " +
                            "WHERE p.weekly_earnings_xu IS NOT NULL AND p.weekly_earnings_xu > 0 " +
                            "ORDER BY p.weekly_earnings_xu DESC LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = createPlayerLeaderboardEntry(resultSet, index, false);
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("weekly_earnings_xu")));
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
    public void addBonusGift(int playerId, int quantity) {
        String sql = "UPDATE player SET top_earnings_xu = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, quantity, playerId);
    }

}
