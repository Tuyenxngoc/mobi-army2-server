package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.DataCharacter;
import com.teamobi.mobiarmy2.json.DataItem;
import com.teamobi.mobiarmy2.json.Equipment;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.response.GetFriendResponse;
import com.teamobi.mobiarmy2.util.Until;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class UserDao implements IUserDao {

    @Override
    public void save(User user) {
        HikariCPManager.getInstance().update("INSERT INTO `player`(`userId`) VALUES (?)", user.getUserId());
    }

    @Override
    public void update(User user) {
        int nvstt = 1, pow = 1;
        for (int i = 0; i < user.nvStt.length; i++) {
            nvstt |= user.nvStt[i] ? pow : 0;
            pow <<= 1;
        }

        String sql = "UPDATE `player` SET " +
                "`friends` = ?, " +
                "`xu` = ?, " +
                "`luong` = ?, " +
                "`dvong` = ?, " +
                "`clan_id` = ?, " +
                "`item` = ?, " +
                "`ruongTrangBi` = ?, " +
                "`ruongItem` = ? ," +
                "`sttnhanvat` = ?, " +
                //...//
                "`NVused` = ? " +
                " WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql,
                user.getFriends().toString(),
                user.getXu(),
                user.getLuong(),
                user.getDanhVong(),
                user.getClanId(),
                Arrays.toString(user.getItems()),
                user.getRuongDoTB().toString(),
                user.getRuongDoItem().toString(),
                nvstt,
                //...//
                user.getNvUsed(),
                user.getPlayerId());
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        User user = null;
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            // Truy vấn để lấy thông tin từ bảng user
            String userQuery = "SELECT `user_id`, `password`, `lock`, `active` FROM user WHERE username = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, username);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        // Kiểm tra mật khẩu đã được mã hóa bằng jBCrypt
                        String hashedPassword = userResultSet.getString("password");
                        if (!BCrypt.checkpw(password, hashedPassword)) {
                            return null;
                        }
                        user = new User();
                        user.setUsername(username);
                        user.setUserId(userResultSet.getInt("user_id"));
                        user.setLock(userResultSet.getBoolean("lock"));
                        user.setActive(userResultSet.getBoolean("active"));
                    }
                }
            }

            if (user != null) {
                //Nếu bị khóa hoặc chưa kích hoạt thì ngừng đọc thông tin
                if (user.isLock() || !user.isActive()) {
                    return user;
                }
                // Truy vấn để lấy thông tin từ bảng player
                String playerQuery = "SELECT * FROM player WHERE user_id = ?";
                try (PreparedStatement playerStatement = connection.prepareStatement(playerQuery)) {
                    playerStatement.setInt(1, user.getUserId());
                    try (ResultSet playerResultSet = playerStatement.executeQuery()) {
                        Gson gson = new Gson();
                        //init
                        int len = NVData.entrys.size();
                        user.nvStt = new boolean[len];
                        user.levels = new int[len];
                        user.levelPercents = new byte[len];
                        user.xps = new int[len];
                        user.points = new int[len];
                        user.pointAdd = new int[len][5];
                        user.NvData = new int[len][6];
                        user.nvEquip = new ruongDoTBEntry[len][6];
                        user.setRuongDoItem(new ArrayList<>());
                        user.setRuongDoTB(new ArrayList<>());

                        if (playerResultSet.next()) {
                            user.setPlayerId(playerResultSet.getInt("player_id"));
                            user.setXu(playerResultSet.getInt("xu"));
                            user.setLuong(playerResultSet.getInt("luong"));
                            user.setDanhVong(playerResultSet.getInt("dvong"));
                            user.setNvUsed(playerResultSet.getByte("NVused"));
                            user.setClanId(playerResultSet.getShort("clan_id"));
                            user.setPointEvent(playerResultSet.getInt("point_event"));

                            int nvstt = playerResultSet.getInt("sttnhanvat");
                            for (byte i = 0; i < 10; i++) {
                                user.nvStt[i] = (nvstt & 1) > 0;
                                nvstt = nvstt / 2;
                            }

                            user.items = gson.fromJson(playerResultSet.getString("item"), byte[].class);

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
                                user.levels[i] = dataCharacter.getLevel();
                                user.xps[i] = dataCharacter.getXp();
                                user.levelPercents[i] = 0;
                                user.points[i] = dataCharacter.getPoint();
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

                            Type listType = new TypeToken<List<Integer>>() {
                            }.getType();
                            user.setFriends(gson.fromJson(playerResultSet.getString("friends"), listType));

                            //Mission
                            user.setMission(gson.fromJson(playerResultSet.getString("mission"), int[].class));
                            user.setMissionLevel(gson.fromJson(playerResultSet.getString("missionLevel"), byte[].class));

                        } else {//Tạo mới một bản ghi
                            User.setDefaultValue(user);
                            save(user);
                        }
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    @Override
    public void updateOnline(boolean flag, int playerId) {
        String sql = "UPDATE `player` SET `online` = ? WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql, flag, playerId);
    }

    @Override
    public List<GetFriendResponse> getFriendsList(int playerId, List<Integer> friendIds) {
        List<GetFriendResponse> friendsList = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder("SELECT user.*, player.* FROM player INNER JOIN user ON user.user_id = player.user_id WHERE player.player_id IN (");
        for (int i = 0; i < friendIds.size(); i++) {
            queryBuilder.append("?");
            if (i < friendIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < friendIds.size(); i++) {
                statement.setInt(i + 1, friendIds.get(i));
            }
            Gson gson = new Gson();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getBoolean("lock") || !resultSet.getBoolean("active")) {
                        continue;
                    }
                    GetFriendResponse friend = new GetFriendResponse();
                    friend.setId(resultSet.getInt("player_id"));
                    friend.setName(resultSet.getString("username"));
                    friend.setXu(resultSet.getInt("xu"));
                    friend.setNvUsed(resultSet.getByte("NVused"));
                    friend.setClanId(resultSet.getShort("clan_id"));
                    friend.setOnline(resultSet.getByte("online"));
                    DataCharacter dataCharacter = gson.fromJson(resultSet.getString("NV" + friend.getNvUsed()), DataCharacter.class);

                    int level = dataCharacter.getLevel();
                    int xp = dataCharacter.getXp();
                    int xpRequired = XpData.getXpRequestLevel(level);

                    friend.setLevel((byte) level);
                    friend.setLevelPt((byte) Until.calculateLevelPercent(xp, xpRequired));
                    Equipment[] trangBi = gson.fromJson(resultSet.getString("ruongTrangBi"), Equipment[].class);
                    friend.setData(NVData.getEquipData(trangBi, dataCharacter, friend.getNvUsed()));

                    friendsList.add(friend);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsList;
    }

    @Override
    public boolean existsByUserIdAndPassword(int userId, String oldPass) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM user WHERE user_id = ?")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    // Check if the provided password matches the hashed password from the database
                    return BCrypt.checkpw(oldPass, hashedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void changePassword(int userId, String newPass) {
        String hashedPassword = BCrypt.hashpw(newPass, BCrypt.gensalt());
        String sql = "UPDATE `user` SET `password` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, hashedPassword, userId);
    }

    @Override
    public Integer findPlayerIdByUsername(String username) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.user_id FROM user u INNER JOIN player p ON u.user_id = p.user_id WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
