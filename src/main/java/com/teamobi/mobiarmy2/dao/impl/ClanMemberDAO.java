package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IClanMemberDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.ClanMemDTO;
import com.teamobi.mobiarmy2.model.EquipmentChestJson;
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

public class ClanMemberDAO implements IClanMemberDAO {

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
                             "p.player_id, p.is_online, p.cup, p.equipment_chest, " +
                             "pc.character_id, pc.level, pc.xp, pc.data, " +
                             "u.username " +
                             "FROM clan_members c " +
                             "INNER JOIN players p on c.player_id = p.player_id " +
                             "INNER JOIN player_characters pc on p.active_character_id = pc.player_character_id " +
                             "INNER JOIN users u on p.user_id = u.user_id " +
                             "WHERE p.clan_id = ? " +
                             "ORDER BY c.rights DESC " +
                             "LIMIT 10 OFFSET ?")) {
            statement.setShort(1, clanId);
            statement.setInt(2, page * 10);

            try (ResultSet resultSet = statement.executeQuery()) {
                byte index = 0;
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    ClanMemDTO entry = new ClanMemDTO();
                    entry.setPlayerId(resultSet.getInt("player_id"));

                    byte rights = resultSet.getByte("rights");
                    switch (rights) {
                        case 2 -> entry.setUsername(resultSet.getString("username") + " (Đội trưởng)");
                        case 1 -> entry.setUsername(resultSet.getString("username") + " (Đội phó %d)".formatted(index));
                        default -> entry.setUsername(resultSet.getString("username"));
                    }
                    entry.setPoint(resultSet.getInt("clan_point"));
                    entry.setActiveCharacter(resultSet.getByte("character_id"));
                    entry.setOnline(resultSet.getByte("is_online"));

                    int currentLevel = resultSet.getInt("level");
                    int currentXp = resultSet.getInt("xp");
                    int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
                    int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);
                    int currentXpInLevel = currentXp - requiredXpCurrentLevel;
                    int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    entry.setLevel((byte) currentLevel);
                    entry.setLevelPt(levelPercent);

                    entry.setIndex((byte) ((page * 10) + index));
                    entry.setCup(resultSet.getInt("cup"));

                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    EquipmentChestJson[] equipmentChests = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    entry.setDataEquip(EquipmentManager.getEquipmentData(equipmentChests, data, entry.getActiveCharacter()));

                    short contributeCount = resultSet.getShort("contribute_count");
                    if (contributeCount > 0) {
                        LocalDateTime currentTime = LocalDateTime.now();
                        LocalDateTime contributionTime = resultSet.getTimestamp("contribute_time").toLocalDateTime();
                        String contributionText = resultSet.getString("contribute_text");
                        int xuContribution = resultSet.getInt("xu");
                        int luongContribution = resultSet.getInt("luong");

                        String formattedTime = Utils.getStringTimeBySecond(Duration.between(currentTime, contributionTime).getSeconds());

                        entry.setContributeText(String.format("%s %s trước", contributionText, formattedTime));
                        entry.setContributeCount("%d lần: %s xu và %s lượng".formatted(contributeCount, Utils.getStringNumber(xuContribution), Utils.getStringNumber(luongContribution)));
                    } else {
                        entry.setContributeText("Chưa đóng góp");
                        entry.setContributeCount("");
                    }

                    entries.add(entry);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

}
