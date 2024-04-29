package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.User;

import java.util.List;

public interface IUserDao {

    User findByUsernameAndPassword(String username, String password);

    void updateOnline(boolean flag, int id);

    User getUserDetails(int userId);

    List<User> getFriendsList(int userId, int[] friends);

    boolean existsByUserIdAndPassword(int id, String oldPass);

    void changePassword(int id, String newPass);

}
