package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.UserXpManager;
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

    private final IServerConfig serverConfig;

    public RankingDAO(IServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    private List<UserLeaderboardDTO> getTopFromQuery(String query, String detailColumn, boolean applyBonus) {
        Gson gson = GsonUtil.getInstance();
        int[] topBonus = serverConfig.getTopBonus();
        List<UserLeaderboardDTO> top = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            byte index = 1;
            while (resultSet.next()) {
                UserLeaderboardDTO userLeaderboardDTO = new UserLeaderboardDTO();

                userLeaderboardDTO.setIndex(index);
                userLeaderboardDTO.setUserId(resultSet.getInt("user_id"));
                userLeaderboardDTO.setClanId(resultSet.getShort("clan_id"));
                userLeaderboardDTO.setActiveCharacter(resultSet.getByte("character_id"));

                int currentLevel = resultSet.getInt("level");
                int currentXp = resultSet.getInt("xp");
                int requiredXpCurrentLevel = UserXpManager.getRequiredXpLevel(currentLevel - 1);
                int requiredXpNextLevel = UserXpManager.getRequiredXpLevel(currentLevel);
                int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                userLeaderboardDTO.setLevel((byte) currentLevel);
                userLeaderboardDTO.setLevelPt(levelPercent);
                userLeaderboardDTO.setDetail(Utils.getStringNumber(resultSet.getInt(detailColumn)));

                if (applyBonus && index <= 3) {
                    userLeaderboardDTO.setUsername(GameString.createTopBonusMessage(resultSet.getString("username"), Utils.getStringNumber(topBonus[index - 1])));
                } else {
                    userLeaderboardDTO.setUsername(resultSet.getString("username"));
                }

                EquipmentChestJson[] equipmentData = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                userLeaderboardDTO.setData(EquipmentManager.getEquipmentIndexes(equipmentData, data, userLeaderboardDTO.getActiveCharacter()));

                top.add(userLeaderboardDTO);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public List<UserLeaderboardDTO> getTopCup() {
        // language=SQL
        String query = "SELECT " +
                "p.user_id, p.equipment_chest, p.cup, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM users p " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "INNER JOIN user_characters pc ON pc.user_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.user_id = cm.user_id " +
                "WHERE p.cup > 0 " +
                "ORDER BY p.cup DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "cup", true);
    }

    @Override
    public List<UserLeaderboardDTO> getTopMasters() {
        // language=SQL
        String query = "SELECT " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "p.user_id, p.equipment_chest, " +
                "u.username, " +
                "cm.clan_id " +
                "FROM user_characters pc " +
                "INNER JOIN users p on pc.user_id = p.user_id " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "LEFT JOIN clan_members cm on p.user_id = cm.user_id " +
                "WHERE pc.xp > 0 " +
                "ORDER BY pc.xp DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xp", false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestXu() {
        // language=SQL
        String query = "SELECT " +
                "p.user_id, p.equipment_chest, p.xu, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM users p " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "INNER JOIN user_characters pc ON pc.user_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.user_id = cm.user_id " +
                "WHERE p.xu > 0 " +
                "ORDER BY p.xu DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xu", false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestLuong() {
        // language=SQL
        String query = "SELECT " +
                "p.user_id, p.equipment_chest, p.luong, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM users p " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "INNER JOIN user_characters pc ON pc.user_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.user_id = cm.user_id " +
                "WHERE p.luong > 0 " +
                "ORDER BY p.luong DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "luong", false);
    }

    @Override
    public List<UserLeaderboardDTO> getWeeklyTopCup() {
        // language=SQL
        String query = "SELECT " +
                "p.user_id, p.equipment_chest, SUM(t.amount) AS cup, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM transactions t " +
                "INNER JOIN users p ON t.user_id = p.user_id " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "INNER JOIN user_characters pc ON pc.user_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.user_id = cm.user_id " +
                "WHERE t.transaction_type = 'CUP' AND t.transaction_date >= (CURDATE() - INTERVAL (WEEKDAY(CURDATE())) DAY) " +
                "GROUP BY p.user_id " +
                "HAVING SUM(t.amount) > 0 " +
                "ORDER BY cup DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "cup", false);
    }

    @Override
    public List<UserLeaderboardDTO> getWeeklyTopRichest() {
        // language=SQL
        String query = "SELECT " +
                "p.user_id, p.equipment_chest, SUM(t.amount) AS xu, " +
                "pc.data, pc.character_id, pc.level, pc.xp, " +
                "cm.clan_id, " +
                "u.username " +
                "FROM transactions t " +
                "INNER JOIN users p ON t.user_id = p.user_id " +
                "INNER JOIN accounts u ON p.account_id = u.account_id " +
                "INNER JOIN user_characters pc ON pc.user_character_id = p.active_character_id " +
                "LEFT JOIN clan_members cm ON p.user_id = cm.user_id " +
                "WHERE t.transaction_type = 'XU' AND t.transaction_date >= (CURDATE() - INTERVAL (WEEKDAY(CURDATE())) DAY) " +
                "GROUP BY p.user_id " +
                "HAVING SUM(t.amount) > 0 " +
                "ORDER BY xu DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, "xu", false);
    }

    @Override
    public void addBonusGift(int userId, int quantity) {
        // language=SQL
        String sql = "UPDATE users SET top_earnings_xu = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, quantity, userId);
    }

}
