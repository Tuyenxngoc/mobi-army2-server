package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.PlayerLeaderboardDTO;
import com.teamobi.mobiarmy2.model.EquipmentChestJson;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.PlayerXpManager;
import com.teamobi.mobiarmy2.server.ServerManager;
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
public class RankingDAO implements IRankingDAO {

    private List<PlayerLeaderboardDTO> getTopFromQuery(String query, String detailColumn, boolean applyBonus) {
        Gson gson = GsonUtil.getInstance();
        int[] topBonus = ServerManager.getInstance().getConfig().getTopBonus();
        List<PlayerLeaderboardDTO> top = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            byte index = 1;
            while (resultSet.next()) {
                PlayerLeaderboardDTO entry = new PlayerLeaderboardDTO();

                entry.setIndex(index);
                entry.setPlayerId(resultSet.getInt("player_id"));
                entry.setClanId(resultSet.getShort("clan_id"));
                entry.setActiveCharacter(resultSet.getByte("character_id"));

                int currentLevel = resultSet.getInt("level");
                int currentXp = resultSet.getInt("xp");
                int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
                int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);
                int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                entry.setLevel((byte) currentLevel);
                entry.setLevelPt(levelPercent);
                entry.setDetail(Utils.getStringNumber(resultSet.getInt(detailColumn)));

                if (applyBonus && index <= 3) {
                    entry.setUsername(GameString.createTopBonusMessage(resultSet.getString("username"), Utils.getStringNumber(topBonus[index - 1])));
                } else {
                    entry.setUsername(resultSet.getString("username"));
                }

                EquipmentChestJson[] equipmentData = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                entry.setData(EquipmentManager.getEquipmentData(equipmentData, data, entry.getActiveCharacter()));

                top.add(entry);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<PlayerLeaderboardDTO> getTopHonor() {
        String query = "SELECT " +
                "p.player_id, p.equipment_chest, p.cup, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM players p " +
                "INNER JOIN users u ON p.user_id = u.user_id " +
                "INNER JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                "WHERE p.cup > 0 " +
                "ORDER BY p.cup DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "cup", true);
    }

    @Override
    public List<PlayerLeaderboardDTO> getTopMasters() {
        String query = "SELECT " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "p.player_id, p.equipment_chest, " +
                "u.username, " +
                "cm.clan_id " +
                "FROM player_characters pc " +
                "INNER JOIN players p on pc.player_id = p.player_id " +
                "INNER JOIN users u on p.user_id = u.user_id " +
                "LEFT JOIN clan_members cm on p.player_id = cm.player_id " +
                "WHERE pc.xp > 0 " +
                "ORDER BY pc.xp DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xp", false);
    }

    @Override
    public List<PlayerLeaderboardDTO> getTopRichestXu() {
        String query = "SELECT " +
                "p.player_id, p.equipment_chest, p.xu, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM players p " +
                "INNER JOIN users u ON p.user_id = u.user_id " +
                "INNER JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                "WHERE p.xu > 0 " +
                "ORDER BY p.xu DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xu", false);
    }

    @Override
    public List<PlayerLeaderboardDTO> getTopRichestLuong() {
        String query = "SELECT " +
                "p.player_id, p.equipment_chest, p.luong, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM players p " +
                "INNER JOIN users u ON p.user_id = u.user_id " +
                "INNER JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                "WHERE p.luong > 0 " +
                "ORDER BY p.luong DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "luong", false);
    }

    @Override
    public List<PlayerLeaderboardDTO> getWeeklyTopHonor() {
        String query = "SELECT " +
                "p.player_id, p.equipment_chest, SUM(t.amount) AS cup, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM transactions t " +
                "INNER JOIN players p ON t.player_id = p.player_id " +
                "INNER JOIN users u ON p.user_id = u.user_id " +
                "INNER JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                "WHERE t.transaction_type = 'CUP' AND t.transaction_date >= (CURDATE() - INTERVAL (WEEKDAY(CURDATE())) DAY) " +
                "GROUP BY p.player_id " +
                "HAVING SUM(t.amount) > 0 " +
                "ORDER BY cup DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "cup", false);
    }

    @Override
    public List<PlayerLeaderboardDTO> getWeeklyTopRichest() {
        // language=SQL
        String query = "SELECT " +
                "p.player_id, p.equipment_chest, SUM(t.amount) AS xu, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM transactions t " +
                "INNER JOIN players p ON t.player_id = p.player_id " +
                "INNER JOIN users u ON p.user_id = u.user_id " +
                "INNER JOIN player_characters pc ON pc.player_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.player_id = cm.player_id " +
                "WHERE t.transaction_type = 'XU' AND t.transaction_date >= (CURDATE() - INTERVAL (WEEKDAY(CURDATE())) DAY) " +
                "GROUP BY p.player_id " +
                "HAVING SUM(t.amount) > 0 " +
                "ORDER BY xu DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xu", false);
    }

    @Override
    public void addBonusGift(int playerId, int quantity) {
        // language=SQL
        String sql = "UPDATE players SET top_earnings_xu = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, quantity, playerId);
    }

}
