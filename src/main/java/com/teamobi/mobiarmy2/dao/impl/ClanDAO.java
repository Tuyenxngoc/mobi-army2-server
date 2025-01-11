package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.ClanDTO;
import com.teamobi.mobiarmy2.dto.ClanInfoDTO;
import com.teamobi.mobiarmy2.server.ClanXpManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanDAO implements IClanDAO {

    @Override
    public Short getClanIcon(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT icon FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getShort("icon");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getXu(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT xu FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("xu");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getLuong(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT luong FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("luong");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getXp(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT xp FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("xp");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getLevel(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT level FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public int getCup(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT cup FROM clans WHERE clan_id = ?")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("cup");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void updateXu(short clanId, int xu) {
        // language=SQL
        String sql = "UPDATE clans SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu, clanId);
    }

    @Override
    public void updateLuong(short clanId, int luong) {
        // language=SQL
        String sql = "UPDATE clans SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong, clanId);
    }

    @Override
    public void gopClanContribute(String txtContribute, int playerId, int xu, int luong) {
        // language=SQL
        String sql = "UPDATE `clan_members` SET " +
                "`contribute_count` = `contribute_count` + 1, " +
                "`contribute_time` = ?, " +
                "`contribute_text` = ?, " +
                "`xu` = `xu` + ?, " +
                "`luong` = `luong` + ? " +
                "WHERE `player_id` = ?";
        HikariCPManager.getInstance().update(sql, LocalDateTime.now(), txtContribute, xu, luong, playerId);
    }

    @Override
    public ClanInfoDTO getClanInfo(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.clan_id, c.name, c.mem_max, c.xu, c.luong, c.cup, c.xp, c.level, c.description, c.created_date, " +
                             "a.username, " +
                             "(SELECT COUNT(*) FROM clan_members cm WHERE cm.clan_id = c.clan_id) AS member_count " +
                             "FROM clans c " +
                             "INNER JOIN users u ON c.master_id = u.user_id " +
                             "INNER JOIN accounts a ON u.account_id = a.account_id " +
                             "WHERE c.clan_id = ?")
        ) {

            statement.setShort(1, clanId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ClanInfoDTO clanInfoDTO = new ClanInfoDTO();
                    clanInfoDTO.setId(resultSet.getShort("clan_id"));
                    clanInfoDTO.setName(resultSet.getString("name"));
                    clanInfoDTO.setMemberCount(resultSet.getByte("member_count"));
                    clanInfoDTO.setMaxMemberCount(resultSet.getByte("mem_max"));
                    clanInfoDTO.setMasterName(resultSet.getString("username"));
                    clanInfoDTO.setXu(resultSet.getInt("xu"));
                    clanInfoDTO.setLuong(resultSet.getInt("luong"));
                    clanInfoDTO.setCup(resultSet.getInt("cup"));

                    int xp = resultSet.getInt("xp");
                    int level = resultSet.getInt("level");
                    int xpForCurrentLevel = ClanXpManager.getRequiredXpLevel(level - 1);
                    int xpForNextLevel = ClanXpManager.getRequiredXpLevel(level);
                    int currentXpInLevel = xp - xpForCurrentLevel;
                    int xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    clanInfoDTO.setExp(xp);
                    clanInfoDTO.setLevel((byte) level);
                    clanInfoDTO.setXpUpLevel(xpForNextLevel);
                    clanInfoDTO.setLevelPercentage(levelPercent);

                    clanInfoDTO.setDescription(resultSet.getString("description"));

                    Timestamp createdDate = resultSet.getTimestamp("created_date");
                    String formattedDate = Utils.formatLocalDateTime(createdDate.toLocalDateTime());
                    clanInfoDTO.setCreatedDate(formattedDate);

//                    ClanItem[] clanItemArray = GsonUtil.getInstance().fromJson(resultSet.getString("item"), ClanItem[].class);
//                    LocalDateTime currentDate = LocalDateTime.now();
//
//                    List<ClanItemDTO> filteredItems = Arrays.stream(clanItemArray)
//                            .filter(item -> !item.getTime().isBefore(currentDate))
//                            .map(item -> {
//                                ClanItemShop clanItemShop = ClanItemManager.getItemClanById(item.getId());
//                                if (clanItemShop != null) {
//                                    ClanItemDTO newClanItemDTO = new ClanItemDTO();
//                                    newClanItemDTO.setName(clanItemShop.getName());
//                                    newClanItemDTO.setTime((int) Duration.between(currentDate, item.getTime()).getSeconds());
//                                    return newClanItemDTO;
//                                }
//                                return null;
//                            })
//                            .filter(Objects::nonNull)
//                            .collect(Collectors.toList());
//
                    clanInfoDTO.setItems(new ArrayList<>());

                    return clanInfoDTO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public short getCountClan() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(clan_id) as `count` FROM clans")) {
                if (resultSet.next()) {
                    return resultSet.getShort("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<ClanDTO> getTopTeams(byte page) {
        List<ClanDTO> top = new ArrayList<>(10);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.clan_id, c.name, c.mem_max, c.xu, c.luong, c.cup, c.xp, c.level, c.description, " +
                             "u.username, " +
                             "(SELECT COUNT(*) FROM clan_members cm WHERE cm.clan_id = c.clan_id) AS member_count " +
                             "FROM clans c " +
                             "INNER JOIN players p ON c.master_id = p.player_id " +
                             "INNER JOIN users u ON p.user_id = u.user_id " +
                             "ORDER BY c.xp " +
                             "LIMIT 10 OFFSET ?")
        ) {
            statement.setByte(1, page);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ClanDTO clanInfo = new ClanDTO();
                    clanInfo.setId(resultSet.getShort("clan_id"));
                    clanInfo.setName(resultSet.getString("name"));
                    clanInfo.setMemberCount(resultSet.getByte("member_count"));
                    clanInfo.setMaxMemberCount(resultSet.getByte("mem_max"));
                    clanInfo.setMasterName(resultSet.getString("username"));
                    clanInfo.setXu(resultSet.getInt("xu"));
                    clanInfo.setLuong(resultSet.getInt("luong"));
                    clanInfo.setCup(resultSet.getInt("cup"));

                    int xp = resultSet.getInt("xp");
                    int level = resultSet.getInt("level");
                    int xpForCurrentLevel = ClanXpManager.getRequiredXpLevel(level - 1);
                    int xpForNextLevel = ClanXpManager.getRequiredXpLevel(level);
                    int currentXpInLevel = xp - xpForCurrentLevel;
                    int xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;
                    byte levelPercent = Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);

                    clanInfo.setLevel((byte) level);
                    clanInfo.setLevelPercentage(levelPercent);
                    clanInfo.setDescription(resultSet.getString("description"));

                    top.add(clanInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    @Override
    public void updateXp(short clanId, int playerId, int xp, int level) {
        // language=SQL
        String sql = "UPDATE clans c " +
                "JOIN clan_members cm ON c.clan_id = cm.clan_id " +
                "SET c.xp = ?, c.level = ? " +
                "WHERE c.clan_id = ? AND cm.player_id = ?";
        HikariCPManager.getInstance().update(sql, xp, level, clanId, playerId);
    }

    @Override
    public void updateCup(short clanId, int playerId, int cup) {
        // language=SQL
        String sql = "UPDATE clans c " +
                "JOIN clan_members cm ON c.clan_id = cm.clan_id " +
                "SET c.cup = ? " +
                "WHERE c.clan_id = ? AND cm.player_id = ?";
        HikariCPManager.getInstance().update(sql, cup, clanId, playerId);
    }

    @Override
    public void updateClanMemberPoints(int playerId, int point) {
        // language=SQL
        String sql = "UPDATE clan_members SET clan_point = clan_point + ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, point, playerId);
    }
}
