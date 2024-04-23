package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.Dao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.DataCharacter;
import com.teamobi.mobiarmy2.json.DataItem;
import com.teamobi.mobiarmy2.json.Equipment;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.util.Until;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author tuyen
 */
public class UserDao implements Dao<User>, IUserDao {

    @Override
    public List<User> getAll() {
        return null;
    }

    @Override
    public Optional<User> get(int id) {
        return Optional.empty();
    }

    @Override
    public void save(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        User user = null;
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            // Truy vấn để lấy thông tin từ bảng user
            String userQuery = "SELECT * FROM user WHERE user = ? AND password = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        user = new User();
                        user.setId(userResultSet.getInt("user_id"));
                        user.setUsername(userResultSet.getString("user"));
                        user.setPassword(userResultSet.getString("password"));
                        user.setLock(userResultSet.getBoolean("lock"));
                        user.setActive(userResultSet.getBoolean("active"));
                    }
                }
            }

            if (user != null) {
                // Truy vấn để lấy thông tin từ bảng player
                String playerQuery = "SELECT * FROM armymem WHERE id = ?";
                try (PreparedStatement playerStatement = connection.prepareStatement(playerQuery)) {
                    playerStatement.setInt(1, user.getId());
                    try (ResultSet playerResultSet = playerStatement.executeQuery()) {
                        if (playerResultSet.next()) {

                            user.setXu(playerResultSet.getInt("xu"));
                            user.setLuong(playerResultSet.getInt("luong"));
                            user.setDanhVong(playerResultSet.getInt("dvong"));
                            user.setNvUsed(playerResultSet.getByte("NVused"));
                            user.setClanId(playerResultSet.getShort("clan"));
                            user.setPointEvent(playerResultSet.getInt("point_event"));

                            int len = NVData.entrys.size();
                            user.nvStt = new boolean[len];
                            user.lever = new int[len];
                            user.leverPercent = new byte[len];
                            user.xp = new int[len];
                            user.point = new int[len];
                            user.pointAdd = new int[len][5];
                            user.NvData = new int[len][6];
                            user.nvEquip = new ruongDoTBEntry[len][6];
                            user.setRuongDoItem(new ArrayList<>());
                            user.setRuongDoTB(new ArrayList<>());
                            Gson gson = new Gson();

                            Equipment[] equipments = gson.fromJson(playerResultSet.getString("ruongTrangBi"), Equipment[].class);
                            for (int i = 0; i < equipments.length; i++) {
                                Equipment equipment = equipments[i];
                                ruongDoTBEntry rdtbEntry = new ruongDoTBEntry();

                                int nvId = equipment.getNvId();
                                int equipType = equipment.getEquipType();
                                int equipId = equipment.getId();

                                rdtbEntry.index = i;
                                rdtbEntry.entry = NVData.getEquipEntryById(nvId, equipType, equipId);
                                rdtbEntry.dayBuy = Until.getDate(equipment.getDayBuy());
                                rdtbEntry.vipLevel = equipment.getVipLevel();
                                rdtbEntry.isUse = equipment.isUse();
                                rdtbEntry.invAdd = new short[5];
                                rdtbEntry.percentAdd = new short[5];
                                rdtbEntry.slot = new int[3];
                                rdtbEntry.anAdd = new short[5];
                                rdtbEntry.slotNull = 0;
                                rdtbEntry.cap = 0;
                                for (int l = 0; l < 5; l++) {
                                    rdtbEntry.invAdd[l] = equipment.getInvAdd().get(l);
                                }
                                for (int l = 0; l < 5; l++) {
                                    rdtbEntry.percentAdd[l] = equipment.getPercenAdd().get(l);
                                }
                                for (int l = 0; l < 3; l++) {
                                    rdtbEntry.slot[l] = equipment.getSlot().get(l);
                                    if (rdtbEntry.slot[l] == -1) {
                                        rdtbEntry.slotNull++;
                                    }
                                }
                                user.ruongDoTB.add(rdtbEntry);
                            }

                            DataItem[] dataItems = gson.fromJson(playerResultSet.getString("ruongItem"), DataItem[].class);
                            for (DataItem item : dataItems) {
                                ruongDoItemEntry rdiEntry = new ruongDoItemEntry();
                                rdiEntry.entry = SpecialItemData.getSpecialItemById(item.getId());
                                rdiEntry.numb = item.getNumb();
                                user.getRuongDoItem().add(rdiEntry);
                            }

                            for (int i = 0; i < 10; i++) {
                                DataCharacter dataCharacter = gson.fromJson(playerResultSet.getString("NV" + (i + 1)), DataCharacter.class);
                                user.lever[i] = dataCharacter.getLever();
                                user.xp[i] = dataCharacter.getXp();
                                user.leverPercent[i] = 0;
                                user.point[i] = dataCharacter.getPoint();
                                for (int j = 0; j < 5; j++) {
                                    user.pointAdd[i][j] = dataCharacter.getPointAdd().get(j);
                                }

                                List<Integer> data = dataCharacter.getData();
                                for (int j = 0; j < 5; j++) {
                                    user.NvData[i][j] = data.get(j);
                                    if (user.NvData[i][j] >= 0 && user.NvData[i][j] < user.ruongDoTB.size()) {
                                        ruongDoTBEntry rdE = user.ruongDoTB.get(user.NvData[i][j]);
                                        if (rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date()) > 0) {
                                            user.nvEquip[i][j] = rdE;
                                        } else {
                                            rdE.isUse = false;
                                        }
                                    }
                                }
                            }

                        } else {//Tạo mới một bản ghi
                            HikariCPManager.getInstance().update("INSERT INTO `armymem`(`user_id`) VALUES (?)", user.getId());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public void updateOnline(boolean flag, int id) {
        String sql = "UPDATE `armymem` SET `online` = ? WHERE id = ?";
        HikariCPManager.getInstance().update(sql, flag, id);
    }

    @Override
    public User getUserDetails(int userId) {
        User user = null;
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            // Truy vấn để lấy thông tin từ bảng user
            String userQuery = "SELECT `user`, `lock`, `active` FROM user WHERE user_id = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setInt(1, userId);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        user = new User();
                        user.setId(userId);
                        user.setUsername(userResultSet.getString("user"));
                        user.setLock(userResultSet.getBoolean("lock"));
                        user.setActive(userResultSet.getBoolean("active"));
                    }
                }
            }

            if (user != null) {
                // Truy vấn để lấy thông tin từ bảng player
                String playerQuery = "SELECT * FROM armymem WHERE id = ?";
                try (PreparedStatement playerStatement = connection.prepareStatement(playerQuery)) {
                    playerStatement.setInt(1, user.getId());
                    try (ResultSet playerResultSet = playerStatement.executeQuery()) {
                        if (playerResultSet.next()) {
                            user.setXu(playerResultSet.getInt("xu"));
                            user.setLuong(playerResultSet.getInt("luong"));
                            user.setDanhVong(playerResultSet.getInt("dvong"));
                            user.setNvUsed(playerResultSet.getByte("NVused"));
                        } else {
                            user = null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

}
