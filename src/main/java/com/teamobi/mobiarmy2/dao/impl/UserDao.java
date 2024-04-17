package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.Dao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
            String userQuery = "SELECT * FROM user WHERE username = ? AND password = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        user = new User();
                        user.setId(userResultSet.getInt("user_id"));
                        user.setUsername(userResultSet.getString("username"));
                        user.setPassword(userResultSet.getString("password"));
                        user.setLock(userResultSet.getBoolean("lock"));
                        user.setActive(userResultSet.getBoolean("active"));
                    }
                }
            }

            if (user != null) {
                // Truy vấn để lấy thông tin từ bảng player
                String playerQuery = "SELECT * FROM player WHERE user_id = ?";
                try (PreparedStatement playerStatement = connection.prepareStatement(playerQuery)) {
                    playerStatement.setInt(1, user.getId());
                    try (ResultSet playerResultSet = playerStatement.executeQuery()) {
                        if (playerResultSet.next()) {
                            user.setXu(playerResultSet.getInt("xu"));
                            user.setLuong(playerResultSet.getInt("luong"));
                            user.setDanhVong(playerResultSet.getInt("danh_vong"));
                            user.setClanId(playerResultSet.getShort("clan_id"));
                            user.setPointEvent(playerResultSet.getInt("point_event"));

                            Gson gson = new Gson();
                            int[] friends = gson.fromJson(playerResultSet.getString("friends"), int[].class);
                            if (friends != null && friends.length > 0) {
                                user.setFriends(friends);
                            } else {
                                user.setFriends(new int[]{2});
                            }

                            String ruongTrangBi = playerResultSet.getString(("ruong_trang_bi"));
                            String ruongItem = playerResultSet.getString(("ruong_item"));


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
        String sql = "UPDATE `armymem` SET `online` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, flag, id);
    }

}
