package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.response.GetFriendResponse;

import java.util.List;

public interface IUserDao extends Dao<User> {

    User findByUsernameAndPassword(String username, String password);

    void updateOnline(boolean flag, int playerId);

    List<GetFriendResponse> getFriendsList(int playerId, List<Integer> friends);

    boolean existsByUserIdAndPassword(int userId, String oldPass);

    void changePassword(int userId, String newPass);

    Integer findPlayerIdByUsername(String username);
}
