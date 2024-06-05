package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.CharacterJson;
import com.teamobi.mobiarmy2.json.ClanItemJson;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.entry.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.entry.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.entry.clan.ClanItem;
import com.teamobi.mobiarmy2.model.entry.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.entry.item.ClanItemEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.*;
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
             PreparedStatement statement = connection.prepareStatement("SELECT icon FROM clan WHERE clan_id = ?")) {
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
             PreparedStatement statement = connection.prepareStatement("SELECT mem FROM clan WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getByte("mem");
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
             PreparedStatement statement = connection.prepareStatement("SELECT xu FROM clan WHERE clan_id = ?")) {
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
             PreparedStatement statement = connection.prepareStatement("SELECT luong FROM clan WHERE clan_id = ?")) {
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
             PreparedStatement statement = connection.prepareStatement("SELECT xp FROM clan WHERE clan_id = ?")) {
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
        String sql = "UPDATE clan SET xu = xu + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, xu, clanId);
    }

    @Override
    public void updateLuong(int clanId, int luong) {
        String sql = "UPDATE clan SET luong = luong + ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, luong, clanId);
    }

    @Override
    public void gopClanContribute(String txtContribute, int playerId, int xu, int luong) {
        String sql = "UPDATE `clanmem` SET " +
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
                     "SELECT c.*, u.username " +
                             "FROM clan c " +
                             "INNER JOIN player p ON c.master_id = p.player_id " +
                             "INNER JOIN user u ON p.user_id = u.user_id " +
                             "WHERE c.clan_id = ?")
        ) {

            statement.setShort(1, clanId);

            try (ResultSet red = statement.executeQuery()) {
                if (red.next()) {
                    ClanInfo clanInfo = new ClanInfo();
                    clanInfo.setId(red.getShort("clan_id"));
                    clanInfo.setName(red.getString("name"));
                    clanInfo.setMemberCount(red.getByte("mem"));
                    clanInfo.setMaxMemberCount(red.getByte("mem_max"));
                    clanInfo.setMasterName(red.getString("username"));
                    clanInfo.setXu(red.getInt("xu"));
                    clanInfo.setLuong(red.getInt("luong"));
                    clanInfo.setCup(red.getInt("cup"));

                    int exp = red.getInt("xp");
                    byte level = Utils.calculateLevelClan(exp);
                    int expUpLevel = Utils.calculateXPRequired(level + 1);
                    int expCurrentLevel = Utils.calculateXPRequired(level);

                    double expProgress = (exp - expCurrentLevel) * 100 / (double) expUpLevel;

                    clanInfo.setExp(exp);
                    clanInfo.setXpUpLevel(expUpLevel);
                    clanInfo.setLevel(level);
                    clanInfo.setLevelPercentage((byte) Math.min(100, expProgress));

                    clanInfo.setDescription(red.getString("description"));
                    clanInfo.setDateCreated(red.getString("date_created"));

                    ClanItemJson[] clanItemJsonArray = GsonUtil.GSON.fromJson(red.getString("item"), ClanItemJson[].class);
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
             PreparedStatement statement = connection.prepareStatement("SELECT c.*, p.*, u.username FROM clanmem c " +
                     "INNER JOIN player p on c.player_id = p.player_id " +
                     "INNER JOIN user u on p.user_id = u.user_id " +
                     "WHERE p.clan_id = ? " +
                     "ORDER BY c.rights DESC " +
                     "LIMIT 10 OFFSET ?")) {
            statement.setShort(1, clanId);
            statement.setInt(2, page * 10);

            try (ResultSet resultSet = statement.executeQuery()) {
                byte index = 0;
                while (resultSet.next()) {
                    ClanMemEntry entry = new ClanMemEntry();
                    entry.setPlayerId(resultSet.getInt("p.player_id"));
                    byte rights = resultSet.getByte("c.rights");
                    switch (rights) {
                        case 2 -> entry.setUsername(resultSet.getString("username") + " (Đội trưởng)");
                        case 1 -> entry.setUsername(resultSet.getString("username") + " (Đội phó %d)".formatted(index));
                        default -> entry.setUsername(resultSet.getString("username"));
                    }
                    entry.setPoint(resultSet.getInt("c.clan_point"));
                    entry.setNvUsed(resultSet.getByte("nv_used"));
                    entry.setOnline(resultSet.getByte("p.online"));

                    CharacterJson characterJson = GsonUtil.GSON.fromJson(resultSet.getString("NV" + entry.getNvUsed()), CharacterJson.class);
                    EquipmentChestJson[] trangBi = GsonUtil.GSON.fromJson(resultSet.getString("ruongTrangBi"), EquipmentChestJson[].class);

                    entry.setLever((byte) characterJson.getLevel());
                    entry.setLevelPt((byte) 0);
                    entry.setIndex((byte) ((page * 10) + index));
                    entry.setCup(resultSet.getInt("p.dvong"));
                    entry.setDataEquip(NVData.getEquipData(trangBi, characterJson, entry.getNvUsed()));

                    short contributeCount = resultSet.getShort("c.contribute_count");
                    if (contributeCount > 0) {
                        LocalDateTime currentTime = LocalDateTime.now();
                        LocalDateTime contributionTime = resultSet.getTimestamp("c.contribute_time").toLocalDateTime();
                        String contributionText = resultSet.getString("c.contribute_text");
                        int xuContribution = resultSet.getInt("c.xu");
                        int luongContribution = resultSet.getInt("c.luong");

                        String formattedTime = Utils.getStringTimeBySecond(Duration.between(currentTime, contributionTime).getSeconds());

                        entry.setContribute_text(String.format("%s %s trước", contributionText, formattedTime));
                        entry.setContribute_count("%d lần: %s xu và %s lượng".formatted(contributeCount, Utils.getStringNumber(xuContribution), Utils.getStringNumber(luongContribution)));
                    } else {
                        entry.setContribute_text("Chưa đóng góp");
                        entry.setContribute_count("");
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
             PreparedStatement statement = connection.prepareStatement("SELECT item FROM clan WHERE clan_id = ?")) {
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
        String sql = "UPDATE clan SET item = ? WHERE clan_id = ?";
        HikariCPManager.getInstance().update(sql, GsonUtil.GSON.toJson(items), clanId);
    }

    @Override
    public short getCountClan() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(clan_id) as `count` FROM clan")) {
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
                     "SELECT c.*, u.username " +
                             "FROM clan c " +
                             "INNER JOIN player p ON c.master_id = p.player_id " +
                             "INNER JOIN user u ON p.user_id = u.user_id " +
                             "ORDER BY c.xp " +
                             "LIMIT 10 OFFSET ?")
        ) {
            statement.setByte(1, page);

            try (ResultSet red = statement.executeQuery()) {
                while (red.next()) {
                    ClanEntry clanInfo = new ClanEntry();
                    clanInfo.setId(red.getShort("clan_id"));
                    clanInfo.setName(red.getString("name"));
                    clanInfo.setMemberCount(red.getByte("mem"));
                    clanInfo.setMaxMemberCount(red.getByte("mem_max"));
                    clanInfo.setMasterName(red.getString("username"));
                    clanInfo.setXu(red.getInt("xu"));
                    clanInfo.setLuong(red.getInt("luong"));
                    clanInfo.setCup(red.getInt("cup"));

                    int exp = red.getInt("xp");
                    byte level = Utils.calculateLevelClan(exp);
                    int expUpLevel = Utils.calculateXPRequired(level + 1);
                    int expCurrentLevel = Utils.calculateXPRequired(level);
                    double expProgress = (exp - expCurrentLevel) * 100 / (double) expUpLevel;

                    clanInfo.setLevel(level);
                    clanInfo.setLevelPercentage((byte) Math.min(100, expProgress));
                    clanInfo.setDescription(red.getString("description"));

                    top.add(clanInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

}
