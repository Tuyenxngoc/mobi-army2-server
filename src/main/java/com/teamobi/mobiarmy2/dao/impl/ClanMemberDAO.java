package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IClanMemberDAO;
import com.teamobi.mobiarmy2.dao.IUserEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.PlayerXpManager;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClanMemberDAO implements IClanMemberDAO {

    private final IUserEquipmentDAO userEquipmentDAO;

    public ClanMemberDAO(IUserEquipmentDAO userEquipmentDAO) {
        this.userEquipmentDAO = userEquipmentDAO;
    }

    @Override
    public Byte count(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS member_count FROM clan_members cm WHERE cm.clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getByte("member_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClanMemDTO> getClanMember(short clanId, byte page) {
        List<ClanMemDTO> entries = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.rights, c.clan_point, c.contribute_count, c.contribute_text, c.contribute_time, c.xu, c.luong, " +
                             "u.user_id, u.is_online, u.cup, " +
                             "uc.character_id, uc.level, uc.xp, uc.data, " +
                             "a.username " +
                             "FROM clan_members c " +
                             "INNER JOIN users u on c.user_id = u.user_id " +
                             "INNER JOIN user_characters uc on u.user_id = uc.user_id " +
                             "INNER JOIN accounts a on u.account_id = a.account_id " +
                             "WHERE c.clan_id = ? " +
                             "AND uc.is_active = 1 " +
                             "ORDER BY c.rights DESC " +
                             "LIMIT 10 OFFSET ?")) {
            statement.setShort(1, clanId);
            statement.setInt(2, page * 10);

            try (ResultSet resultSet = statement.executeQuery()) {
                byte index = 0;
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    ClanMemDTO clanMemDTO = new ClanMemDTO();
                    clanMemDTO.setUserId(resultSet.getInt("user_id"));

                    byte rights = resultSet.getByte("rights");
                    switch (rights) {
                        case 2 -> clanMemDTO.setUsername(resultSet.getString("username") + " (Đội trưởng)");
                        case 1 ->
                                clanMemDTO.setUsername(resultSet.getString("username") + " (Đội phó %d)".formatted(index));
                        default -> clanMemDTO.setUsername(resultSet.getString("username"));
                    }
                    clanMemDTO.setPoint(resultSet.getInt("clan_point"));
                    clanMemDTO.setActiveCharacter(resultSet.getByte("character_id"));
                    clanMemDTO.setOnline(resultSet.getByte("is_online"));

                    int currentLevel = resultSet.getInt("level");
                    int currentXp = resultSet.getInt("xp");
                    int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
                    int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);
                    int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                    int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    clanMemDTO.setLevel((byte) currentLevel);
                    clanMemDTO.setLevelPt(levelPercent);

                    clanMemDTO.setIndex((byte) ((page * 10) + index));
                    clanMemDTO.setCup(resultSet.getInt("cup"));

                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    Map<Integer, UserEquipmentDTO> userEquipmentDTOS = userEquipmentDAO.findAllByIdIn(data);
                    clanMemDTO.setDataEquip(EquipmentManager.getEquipmentIndexes(userEquipmentDTOS, data, clanMemDTO.getActiveCharacter()));

                    short contributeCount = resultSet.getShort("contribute_count");
                    if (contributeCount > 0) {
                        LocalDateTime currentTime = LocalDateTime.now();
                        LocalDateTime contributionTime = resultSet.getTimestamp("contribute_time").toLocalDateTime();
                        String contributionText = resultSet.getString("contribute_text");
                        int xuContribution = resultSet.getInt("xu");
                        int luongContribution = resultSet.getInt("luong");

                        String formattedTime = Utils.getStringTimeBySecond(Duration.between(currentTime, contributionTime).getSeconds());

                        clanMemDTO.setContributeText(String.format("%s %s trước", contributionText, formattedTime));
                        clanMemDTO.setContributeCount("%d lần: %s xu và %s lượng".formatted(contributeCount, Utils.getStringNumber(xuContribution), Utils.getStringNumber(luongContribution)));
                    } else {
                        clanMemDTO.setContributeText("Chưa đóng góp");
                        clanMemDTO.setContributeCount("");
                    }

                    entries.add(clanMemDTO);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

}
