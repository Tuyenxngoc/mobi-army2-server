package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
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

    @Override
    public List<PlayerLeaderboardEntry> getTopHonor() {
        Gson gson = GsonUtil.GSON;
        List<PlayerLeaderboardEntry> top = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT p.*, pc.*, cm.*, u.username " +
                            "FROM players p " +
                            "INNER JOIN users u ON p.user_id = u.user_id " +
                            "JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                            "JOIN clan_members cm ON p.player_id = cm.player_id " +
                            "WHERE p.cup > 0 " +
                            "ORDER BY p.cup DESC " +
                            "LIMIT 100"
            )) {
                byte index = 1;
                while (resultSet.next()) {
                    PlayerLeaderboardEntry entry = new PlayerLeaderboardEntry();

                    entry.setPlayerId(resultSet.getInt("player_id"));
                    if (index <= 3) {
                        entry.setUsername(GameString.topBonus(resultSet.getString("username"), Utils.getStringNumber(CommonConstant.TOP_BONUS[index - 1])));
                    } else {
                        entry.setUsername(resultSet.getString("username"));
                    }
                    entry.setClanId(resultSet.getShort("clan_id"));

                    EquipmentChestJson[] equipmentData = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);

                    entry.setActiveCharacter(resultSet.getByte("active_character_id"));
                    entry.setLevel((byte) resultSet.getInt("level"));
                    entry.setLevelPt((byte) 0);
                    entry.setIndex(index);
                    entry.setData(NVData.getEquipData(equipmentData, data, entry.getActiveCharacter()));
                    entry.setDetail(Utils.getStringNumber(resultSet.getInt("cup")));

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
    public List<PlayerLeaderboardEntry> getTopMasters() {
        return List.of();
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopRichestXu() {
        return List.of();
    }

    @Override
    public List<PlayerLeaderboardEntry> getTopRichestLuong() {
        return List.of();
    }

    @Override
    public List<PlayerLeaderboardEntry> getWeeklyTopHonor() {
        return List.of();
    }

    @Override
    public List<PlayerLeaderboardEntry> getWeeklyTopRichest() {
        return List.of();
    }

    @Override
    public void addBonusGift(int playerId, int quantity) {
        String sql = "UPDATE player SET top_earnings_xu = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, quantity, playerId);
    }

}
