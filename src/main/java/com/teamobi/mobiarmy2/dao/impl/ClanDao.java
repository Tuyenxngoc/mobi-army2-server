package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IClanDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.ClanItemData;
import com.teamobi.mobiarmy2.json.DataCharacter;
import com.teamobi.mobiarmy2.json.Equipment;
import com.teamobi.mobiarmy2.model.ItemClanData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class ClanDao implements IClanDao {

    private final Gson gson;

    public ClanDao() {
        this.gson = new Gson();
    }

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
        HikariCPManager.getInstance().update(sql, Until.toDateString(new Date()), txtContribute, xu, luong, playerId);
    }

    @Override
    public ClanManager.ClanInfo getClanInfo(short clanId) {
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
                    ClanManager.ClanInfo clanInfo = new ClanManager.ClanInfo();
                    clanInfo.setId(red.getShort("clan_id"));
                    clanInfo.setName(red.getString("name"));
                    clanInfo.setMemberCount((byte) red.getInt("mem"));
                    clanInfo.setMaxMemberCount((byte) red.getInt("mem_max"));
                    clanInfo.setMasterName(red.getString("username"));
                    clanInfo.setXu(red.getInt("xu"));
                    clanInfo.setLuong(red.getInt("luong"));
                    clanInfo.setCup(red.getInt("cup"));
                    clanInfo.setLevel((byte) 1);
                    clanInfo.setXpUpLevel(1000);
                    clanInfo.setDescription(red.getString("description"));
                    clanInfo.setDateCreated(red.getString("date_created"));

                    //Todo optimize this code
                    ClanItemData[] items = gson.fromJson(red.getString("item"), ClanItemData[].class);
                    Date currentDate = new Date();
                    List<ClanManager.ClanItem> a = new ArrayList<>();
                    for (ClanItemData item : items) {
                        if (item.getTime().before(currentDate)) {
                            continue;
                        }
                        ItemClanData.ItemClan itemClan = ItemClanData.getItemClanById(item.getId());
                        if (itemClan != null) {
                            ClanManager.ClanItem newItm = new ClanManager.ClanItem();
                            newItm.setName(itemClan.getName());
                            newItm.setTime((int) (item.getTime().getTime() / 1000) - (int) (currentDate.getTime() / 1000));
                            a.add(newItm);
                        }

                    }
                    clanInfo.setItems(a);

                    return clanInfo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClanManager.ClanMemEntry> getClanMember(short clanId, byte page) {
        List<ClanManager.ClanMemEntry> entries = new ArrayList<>();
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT c.*, p.*, u.username FROM clanmem c " +
                     "INNER JOIN player p on c.player_id = p.player_id " +
                     "INNER JOIN user u on p.user_id = u.user_id " +
                     "WHERE c.clan_id = ? " +
                     "ORDER BY c.rights DESC")) {
            statement.setShort(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                byte index = 0;
                while (resultSet.next()) {
                    ClanManager.ClanMemEntry entry = new ClanManager.ClanMemEntry();
                    entry.setPlayerId(resultSet.getInt("p.player_id"));

                    byte rights = resultSet.getByte("c.rights");
                    if (rights == 2) {
                        entry.setUsername(resultSet.getString("username") + " (Đội trưởng)");
                    } else if (rights == 1) {
                        entry.setUsername(resultSet.getString("username") + " (Đội phó %d)".formatted(index));
                    } else {
                        entry.setUsername(resultSet.getString("username"));
                    }
                    entry.setNvUsed(resultSet.getByte("NVused"));

                    DataCharacter dataCharacter = gson.fromJson(resultSet.getString("NV" + entry.getNvUsed()), DataCharacter.class);
                    Equipment[] trangBi = gson.fromJson(resultSet.getString("ruongTrangBi"), Equipment[].class);
                    entry.setDataEquip(NVData.getEquipData(trangBi, dataCharacter, entry.getNvUsed()));

                    entry.setContribute_text(resultSet.getString("c.contribute_text"));
                    entry.setN_contribute("Chưa đóng góp");

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
