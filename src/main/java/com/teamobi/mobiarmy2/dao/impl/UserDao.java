package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.reflect.TypeToken;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.CharacterJson;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.entry.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.entry.user.FriendEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.JsonConverter;
import com.teamobi.mobiarmy2.util.Until;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tuyen
 */
public class UserDao implements IUserDao {

    @Override
    public void save(User user) {
        HikariCPManager.getInstance().update("INSERT INTO `player`(`user_id`, `xu`, `luong`) VALUES (?,?,?)", user.getUserId(), user.getXu(), user.getLuong());
    }

    @Override
    public void update(User user) {
        int nvstt = 1, pow = 1;
        for (int i = 0; i < user.getOwnedCharacters().length; i++) {
            nvstt |= user.getOwnedCharacters()[i] ? pow : 0;
            pow <<= 1;
        }

        String ruongdoItem = JsonConverter.convertRuongDoItemToJson(user.getRuongDoItem());
        String ruongdoTb = JsonConverter.convertRuongDoTBToJson(user.getRuongDoTB());
        CharacterJson nv1 = new CharacterJson();
        nv1.setPoint(user.getPoints()[0]);
        nv1.setLevel(user.getLevels()[0]);
        nv1.setXp(user.getXps()[0]);
        nv1.setPointAdd(user.getPointAdd()[0]);
        nv1.setData(user.getEquipData()[0]);

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
                "`online` = ?, " +
                "`mission` = ?, " +
                "`missionLevel` = ?, " +
                "`top_earnings_xu` = ?, " +
                "`NV1` = ?, " +

                //...//
                "`nv_used` = ? " +
                " WHERE player_id = ?";
        HikariCPManager.getInstance().update(sql,
                user.getFriends().toString(),
                user.getXu(),
                user.getLuong(),
                user.getDanhVong(),
                user.getClanId() == 0 ? null : user.getClanId(),
                Arrays.toString(user.getItems()),
                ruongdoTb,
                ruongdoItem,
                nvstt,
                false,
                Arrays.toString(user.getMission()),
                Arrays.toString(user.getMissionLevel()),
                user.getTopEarningsXu(),
                GsonUtil.GSON.toJson(nv1),
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
                        //init
                        int len = NVData.CHARACTER_ENTRIES.size();
                        user.setOwnedCharacters(new boolean[len]);
                        user.setLevels(new int[len]);
                        user.setLevelPercents(new byte[len]);
                        user.setXps(new int[len]);
                        user.setPoints(new int[len]);
                        user.setPointAdd(new short[len][5]);
                        user.setEquipData(new int[len][6]);
                        user.setNvEquip(new EquipmentChestEntry[len][6]);
                        user.setRuongDoItem(new ArrayList<>());
                        user.setRuongDoTB(new ArrayList<>());

                        if (playerResultSet.next()) {
                            user.setPlayerId(playerResultSet.getInt("player_id"));
                            user.setXu(playerResultSet.getInt("xu"));
                            user.setLuong(playerResultSet.getInt("luong"));
                            user.setDanhVong(playerResultSet.getInt("dvong"));
                            user.setNvUsed(playerResultSet.getByte("nv_used"));
                            user.setClanId(playerResultSet.getShort("clan_id"));
                            user.setPointEvent(playerResultSet.getInt("point_event"));

                            int nvstt = playerResultSet.getInt("sttnhanvat");
                            for (byte i = 0; i < 10; i++) {
                                user.getOwnedCharacters()[i] = (nvstt & 1) > 0;
                                nvstt = nvstt / 2;
                            }

                            byte[] items = GsonUtil.GSON.fromJson(playerResultSet.getString("item"), byte[].class);
                            if (items.length != FightItemData.FIGHT_ITEM_ENTRIES.size()) {
                                byte[] adjustedItems = new byte[FightItemData.FIGHT_ITEM_ENTRIES.size()];
                                System.arraycopy(items, 0, adjustedItems, 0, Math.min(items.length, adjustedItems.length));
                                user.setItems(adjustedItems);
                            } else {
                                user.setItems(items);
                            }

                            //Đọc dữ liệu trang bị
                            EquipmentChestJson[] equipmentChestJsons = GsonUtil.GSON.fromJson(playerResultSet.getString("ruongTrangBi"), EquipmentChestJson[].class);
                            for (EquipmentChestJson json : equipmentChestJsons) {
                                EquipmentChestEntry equip = new EquipmentChestEntry();
                                equip.setEquipmentEntry(NVData.getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex()));
                                if (equip.getEquipmentEntry() == null) {
                                    continue;
                                }
                                equip.setKey(json.getKey());
                                equip.setPurchaseDate(json.getPurchaseDate());
                                equip.setVipLevel(json.getVipLevel());
                                equip.setInUse(json.getInUse() == 1);
                                equip.setAddPoints(json.getAdditionalPoints());
                                equip.setAddPercents(json.getAdditionalPercent());
                                equip.setSlots(json.getSlots());
                                byte emptySlot = 0;
                                for (int l = 0; l < equip.getSlots().length; l++) {
                                    if (equip.getSlots()[l] < 0) {
                                        emptySlot++;
                                    }
                                }
                                equip.setEmptySlot(emptySlot);
                                user.getRuongDoTB().add(equip);
                            }

                            //Đọc dữ liệu item
                            SpecialItemChestJson[] specialItemChestJsons = GsonUtil.GSON.fromJson(playerResultSet.getString("ruongItem"), SpecialItemChestJson[].class);
                            for (SpecialItemChestJson item : specialItemChestJsons) {
                                SpecialItemChestEntry specialItemChestEntry = new SpecialItemChestEntry();
                                specialItemChestEntry.setItem(SpecialItemData.getSpecialItemById(item.getId()));
                                if (specialItemChestEntry.getItem() == null) {
                                    continue;
                                }
                                specialItemChestEntry.setQuantity(item.getQuantity());
                                user.getRuongDoItem().add(specialItemChestEntry);
                            }

                            //Đọc dữ liệu nhân vật
                            for (int i = 0; i < 10; i++) {
                                CharacterJson characterJson = GsonUtil.GSON.fromJson(playerResultSet.getString("NV" + (i + 1)), CharacterJson.class);
                                user.getLevels()[i] = characterJson.getLevel();
                                user.getXps()[i] = characterJson.getXp();
                                user.getLevelPercents()[i] = Until.calculateLevelPercent(characterJson.getXp(), XpData.getXpRequestLevel(characterJson.getLevel() + 1));
                                user.getPoints()[i] = characterJson.getPoint();
                                user.getPointAdd()[i] = characterJson.getPointAdd();
                                user.getEquipData()[i] = new int[]{-1, -1, -1, -1, -1, -1};
                                for (int j = 0; j < characterJson.getData().length; j++) {
                                    int key = characterJson.getData()[j];
                                    EquipmentChestEntry equip = user.getEquipmentByKey(key);
                                    if (equip != null) {
                                        if (equip.isExpired()) {
                                            equip.setInUse(false);
                                        } else {
                                            user.getNvEquip()[i][j] = equip;
                                            user.getEquipData()[i][j] = equip.getKey();
                                        }
                                    }
                                }
                            }

                            Type listType = new TypeToken<List<Integer>>() {
                            }.getType();
                            user.setFriends(GsonUtil.GSON.fromJson(playerResultSet.getString("friends"), listType));

                            int[] missions = GsonUtil.GSON.fromJson(playerResultSet.getString("mission"), int[].class);
                            byte[] missionLevels = GsonUtil.GSON.fromJson(playerResultSet.getString("missionLevel"), byte[].class);

                            if (missions.length != MissionData.MISSION_LIST.size()) {
                                int[] adjustedMissions = new int[MissionData.MISSION_LIST.size()];
                                System.arraycopy(missions, 0, adjustedMissions, 0, Math.min(missions.length, adjustedMissions.length));
                                user.setMission(adjustedMissions);
                            } else {
                                user.setMission(missions);
                            }
                            if (missionLevels.length != MissionData.MISSION_LIST.size()) {
                                byte[] adjustedMissionLevels = new byte[MissionData.MISSION_LIST.size()];
                                Arrays.fill(adjustedMissionLevels, (byte) 1);
                                System.arraycopy(missionLevels, 0, adjustedMissionLevels, 0, Math.min(missionLevels.length, adjustedMissionLevels.length));
                                user.setMissionLevel(adjustedMissionLevels);
                            } else {
                                user.setMissionLevel(missionLevels);
                            }

                            user.setXpX2Time(playerResultSet.getTimestamp("x2_xp_time").toLocalDateTime());
                            user.setLastOnline(playerResultSet.getTimestamp("last_online").toLocalDateTime());

                            user.setTopEarningsXu(playerResultSet.getInt("top_earnings_xu"));
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
    public List<FriendEntry> getFriendsList(int playerId, List<Integer> friendIds) {
        List<FriendEntry> friendsList = new ArrayList<>();

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
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getBoolean("lock") || !resultSet.getBoolean("active")) {
                        continue;
                    }
                    FriendEntry friend = new FriendEntry();
                    friend.setId(resultSet.getInt("player_id"));
                    friend.setName(resultSet.getString("username"));
                    friend.setXu(resultSet.getInt("xu"));
                    friend.setNvUsed(resultSet.getByte("nv_used"));
                    friend.setClanId(resultSet.getShort("clan_id"));
                    friend.setOnline(resultSet.getByte("online"));
                    CharacterJson characterJson = GsonUtil.GSON.fromJson(resultSet.getString("NV" + friend.getNvUsed()), CharacterJson.class);

                    int level = characterJson.getLevel();
                    int xp = characterJson.getXp();
                    int xpRequired = XpData.getXpRequestLevel(level);

                    friend.setLevel((byte) level);
                    friend.setLevelPt(Until.calculateLevelPercent(xp, xpRequired));
                    EquipmentChestJson[] trangBi = GsonUtil.GSON.fromJson(resultSet.getString("ruongTrangBi"), EquipmentChestJson[].class);
                    friend.setData(NVData.getEquipData(trangBi, characterJson, friend.getNvUsed()));

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
             PreparedStatement statement = connection.prepareStatement("SELECT p.player_id FROM user u INNER JOIN player p ON u.user_id = p.user_id WHERE u.username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("player_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateLastOnline(LocalDateTime now, int playerId) {
        String sql = "UPDATE `player` SET `last_online` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, now.toString(), playerId);
    }

}
