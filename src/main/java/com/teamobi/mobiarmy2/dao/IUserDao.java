package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.response.GetFriendResponse;

import java.util.List;

public interface IUserDao extends Dao<User> {

    User findByUsernameAndPassword(String username, String password);

    void updateOnline(boolean flag, int id);

    List<GetFriendResponse> getFriendsList(int userId, int[] friends);

    boolean existsByUserIdAndPassword(int id, String oldPass);

    void changePassword(int id, String newPass);

}
