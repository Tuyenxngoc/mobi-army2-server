package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.CharacterData;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.model.entry.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.entry.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.entry.clan.ClanItem;
import com.teamobi.mobiarmy2.model.entry.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.entry.item.ClanItemEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class ClanDao implements IClanDao {

    @Override
    public Short getClanIcon(int clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT icon FROM clans WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
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
    public Byte getMembersOfClan(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS member_count FROM clan_members cm WHERE cm.clan_id = ?")) {
            statement.setInt(1, clanId);
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
    public int getXu(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT xu FROM clans WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
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
            statement.setInt(1, clanId);
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
    public int getExp(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT xp FROM clans WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
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
    public void updateXu(int clanId, int xu) {
        String sql = "UPDATE clans SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu, clanId);
    }

    @Override
    public void updateLuong(int clanId, int luong) {
        String sql = "UPDATE clans SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong, clanId);
    }

    @Override
    public void gopClanContribute(String txtContribute, int playerId, int xu, int luong) {
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
    public ClanInfo getClanInfo(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.clan_id, c.name, c.mem_max, c.xu, c.luong, c.cup, c.xp, c.description, c.created_date, c.item, " +
                             "u.username, " +
                             "(SELECT COUNT(*) FROM clan_members cm WHERE cm.clan_id = c.clan_id) AS member_count " +
                             "FROM clans c " +
                             "INNER JOIN players p ON c.master_id = p.player_id " +
                             "INNER JOIN users u ON p.user_id = u.user_id " +
                             "WHERE c.clan_id = ?")
        ) {

            statement.setShort(1, clanId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ClanInfo clanInfo = new ClanInfo();
                    clanInfo.setId(resultSet.getShort("clan_id"));
                    clanInfo.setName(resultSet.getString("name"));
                    clanInfo.setMemberCount(resultSet.getByte("member_count"));
                    clanInfo.setMaxMemberCount(resultSet.getByte("mem_max"));
                    clanInfo.setMasterName(resultSet.getString("username"));
                    clanInfo.setXu(resultSet.getInt("xu"));
                    clanInfo.setLuong(resultSet.getInt("luong"));
                    clanInfo.setCup(resultSet.getInt("cup"));

                    int exp = resultSet.getInt("xp");
                    byte level = Utils.calculateLevelClan(exp);
                    int expUpLevel = Utils.calculateXPRequired(level + 1);
                    int expCurrentLevel = Utils.calculateXPRequired(level);

                    double expProgress = (exp - expCurrentLevel) * 100 / (double) expUpLevel;

                    clanInfo.setExp(exp);
                    clanInfo.setXpUpLevel(expUpLevel);
                    clanInfo.setLevel(level);
                    clanInfo.setLevelPercentage((byte) Math.min(100, expProgress));

                    clanInfo.setDescription(resultSet.getString("description"));

                    Timestamp createdDate = resultSet.getTimestamp("created_date");
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(CommonConstant.PATTERN_DATE_TIME);
                    String formattedDate = dateTimeFormat.format(createdDate);
                    clanInfo.setCreatedDate(formattedDate);

                    ClanItemJson[] clanItemJsonArray = GsonUtil.GSON.fromJson(resultSet.getString("item"), ClanItemJson[].class);
                    LocalDateTime currentDate = LocalDateTime.now();

                    List<ClanItem> filteredItems = Arrays.stream(clanItemJsonArray)
                            .filter(item -> !item.getTime().isBefore(currentDate))
                            .map(item -> {
                                ClanItemEntry clanItemEntry = ItemClanData.getItemClanById(item.getId());
                                if (clanItemEntry != null) {
                                    ClanItem newClanItem = new ClanItem();
                                    newClanItem.setName(clanItemEntry.getName());
                                    newClanItem.setTime((int) Duration.between(currentDate, item.getTime()).getSeconds());
                                    return newClanItem;
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    clanInfo.setItems(filteredItems);

                    return clanInfo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClanMemEntry> getClanMember(short clanId, byte page) {
        List<ClanMemEntry> entries = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.rights, c.clan_point, c.contribute_count, c.contribute_text, c.contribute_time, c.xu, c.luong, " +
                             "p.player_id, p.is_online, p.cup, p.equipment_chest, " +
                             "pc.character_id, pc.level, pc.data, " +
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
                Gson gson = GsonUtil.GSON;
                while (resultSet.next()) {
                    ClanMemEntry entry = new ClanMemEntry();
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
                    entry.setLever(resultSet.getByte("level"));
                    entry.setLevelPt((byte) 0);
                    entry.setIndex((byte) ((page * 10) + index));
                    entry.setCup(resultSet.getInt("cup"));

                    int[] data = gson.fromJson(resultSet.getString("data"), int[].class);
                    EquipmentChestJson[] equipmentChests = gson.fromJson(resultSet.getString("equipment_chest"), EquipmentChestJson[].class);
                    entry.setDataEquip(CharacterData.getEquipData(equipmentChests, data, entry.getActiveCharacter()));

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

    @Override
    public ClanItemJson[] getClanItems(short clanId) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT item FROM clans WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return GsonUtil.GSON.fromJson(resultSet.getString("item"), ClanItemJson[].class);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void updateClanItems(short clanId, ClanItemJson[] items) {
        String sql = "UPDATE clans SET item = ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, GsonUtil.GSON.toJson(items), clanId);
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
    public List<ClanEntry> getTopTeams(byte page) {
        List<ClanEntry> top = new ArrayList<>(10);
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " +
                             "c.clan_id, c.name, c.mem_max, c.xu, c.luong, c.cup, c.xp, c.description, " +
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
                    ClanEntry clanInfo = new ClanEntry();
                    clanInfo.setId(resultSet.getShort("clan_id"));
                    clanInfo.setName(resultSet.getString("name"));
                    clanInfo.setMemberCount(resultSet.getByte("member_count"));
                    clanInfo.setMaxMemberCount(resultSet.getByte("mem_max"));
                    clanInfo.setMasterName(resultSet.getString("username"));
                    clanInfo.setXu(resultSet.getInt("xu"));
                    clanInfo.setLuong(resultSet.getInt("luong"));
                    clanInfo.setCup(resultSet.getInt("cup"));

                    int exp = resultSet.getInt("xp");
                    byte level = Utils.calculateLevelClan(exp);
                    int expUpLevel = Utils.calculateXPRequired(level + 1);
                    int expCurrentLevel = Utils.calculateXPRequired(level);
                    double expProgress = (exp - expCurrentLevel) * 100 / (double) expUpLevel;

                    clanInfo.setLevel(level);
                    clanInfo.setLevelPercentage((byte) Math.min(100, expProgress));
                    clanInfo.setDescription(resultSet.getString("description"));

                    top.add(clanInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

}
