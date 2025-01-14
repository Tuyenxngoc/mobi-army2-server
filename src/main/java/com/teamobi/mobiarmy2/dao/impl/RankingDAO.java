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

    private List<UserLeaderboardDTO> getTopFromQuery(String query, boolean applyBonus) {
        List<UserLeaderboardDTO> top = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            byte index = 1;
            int[] topBonus = serverConfig.getTopBonus();
            Gson gson = GsonUtil.getInstance();
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
                userLeaderboardDTO.setDetail(Utils.getStringNumber(resultSet.getInt("points")));

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
                "u.user_id, u.equipment_chest, u.cup as points, " +
                "uc.data, uc.character_id, uc.level, uc.xp, " +
                "cm.clan_id, " +
                "a.username " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON uc.user_character_id = u.active_character_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE u.cup > 0 " +
                "ORDER BY u.cup DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, true);
    }

    @Override
    public List<UserLeaderboardDTO> getTopMasters() {
        // language=SQL
        String query = "SELECT " +
                "uc.data, uc.character_id, uc.level, uc.xp, uc.xp as points, " +
                "u.user_id, u.equipment_chest, " +
                "a.username, " +
                "cm.clan_id " +
                "FROM user_characters uc " +
                "INNER JOIN users u on uc.user_id = u.user_id " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "LEFT JOIN clan_members cm on u.user_id = cm.user_id " +
                "WHERE uc.xp > 0 " +
                "ORDER BY uc.xp DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestXu() {
        // language=SQL
        String query = "SELECT " +
                "u.user_id, u.equipment_chest, u.xu as points, " +
                "uc.data, uc.character_id, uc.level, uc.xp, " +
                "cm.clan_id, " +
                "a.username " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON uc.user_character_id = u.active_character_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE u.xu > 0 " +
                "ORDER BY u.xu DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestLuong() {
        // language=SQL
        String query = "SELECT " +
                "u.user_id, u.equipment_chest, u.luong as points, " +
                "uc.data, uc.character_id, uc.level, uc.xp, " +
                "cm.clan_id, " +
                "a.username " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON uc.user_character_id = u.active_character_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE u.luong > 0 " +
                "ORDER BY u.luong DESC " +
                "LIMIT 100";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getWeeklyTopCup() {
        return List.of();
    }

    @Override
    public List<UserLeaderboardDTO> getWeeklyTopRichest() {
        return List.of();
    }

    @Override
    public void addBonusGift(int userId, int quantity) {
        // language=SQL
        String sql = "UPDATE users SET top_earnings_xu = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, quantity, userId);
    }

}
