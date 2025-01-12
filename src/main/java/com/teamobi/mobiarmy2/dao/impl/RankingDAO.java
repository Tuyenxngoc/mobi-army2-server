package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IRankingDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
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

    private List<UserLeaderboardDTO> getTopFromQuery(String query, boolean applyBonus) {
        Gson gson = GsonUtil.getInstance();
        int[] topBonus = ServerManager.getInstance().getConfig().getTopBonus();
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
                if (applyBonus && index <= 3) {
                    userLeaderboardDTO.setUsername(GameString.createTopBonusMessage(resultSet.getString("username"), Utils.getStringNumber(topBonus[index - 1])));
                } else {
                    userLeaderboardDTO.setUsername(resultSet.getString("username"));
                }

                int currentLevel = resultSet.getInt("level");
                int currentXp = resultSet.getInt("xp");
                int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
                int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);
                int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                userLeaderboardDTO.setLevel((byte) currentLevel);
                userLeaderboardDTO.setLevelPt(levelPercent);
                userLeaderboardDTO.setDetail(Utils.getStringNumber(resultSet.getInt("point")));

                int[] data = gson.fromJson(resultSet.getString("data"), int[].class);

                //      userLeaderboardDTO.setData(EquipmentManager.getEquipmentData());

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
                "u.user_id, u.cup as point, " +
                "a.username, " +
                "cm.clan_id, " +
                "uc.character_id, uc.level, uc.xp, uc.data " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON u.user_id = uc.user_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE uc.is_active = 1 " +
                "AND u.cup > 0 " +
                "ORDER BY u.cup DESC";
        return getTopFromQuery(query, true);
    }

    @Override
    public List<UserLeaderboardDTO> getTopMasters() {
        // language=SQL
        String query = "SELECT " +
                "u.user_id, u.cup as point, " +
                "a.username, " +
                "cm.clan_id, " +
                "uc.character_id, uc.level, uc.xp, uc.data " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON u.user_id = uc.user_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE uc.is_active = 1 " +
                "AND uc.xp > 0 " +
                "ORDER BY uc.xp DESC";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestXu() {
        // language=SQL
        String query = "SELECT " +
                "u.user_id, u.cup as point, " +
                "a.username, " +
                "cm.clan_id, " +
                "uc.character_id, uc.level, uc.xp, uc.data " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON u.user_id = uc.user_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE uc.is_active = 1 " +
                "AND u.xu > 0 " +
                "ORDER BY u.xu DESC";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getTopRichestLuong() {
        // language=SQL
        String query = "SELECT " +
                "u.user_id, u.cup as point, " +
                "a.username, " +
                "cm.clan_id, " +
                "uc.character_id, uc.level, uc.xp, uc.data " +
                "FROM users u " +
                "INNER JOIN accounts a ON u.account_id = a.account_id " +
                "INNER JOIN user_characters uc ON u.user_id = uc.user_id " +
                "LEFT JOIN clan_members cm ON u.user_id = cm.user_id " +
                "WHERE uc.is_active = 1 " +
                "AND u.luong > 0 " +
                "ORDER BY u.luong DESC";
        return getTopFromQuery(query, false);
    }

    @Override
    public List<UserLeaderboardDTO> getWeeklyTopHonor() {
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
