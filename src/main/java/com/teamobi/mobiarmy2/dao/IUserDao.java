package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.entry.user.FriendEntry;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserDao extends Dao<User> {

    User findByUsernameAndPassword(String username, String password);

    void updateOnline(boolean flag, int playerId);

    List<FriendEntry> getFriendsList(int playerId, List<Integer> friends);

    boolean existsByUserIdAndPassword(String userId, String oldPass);

    void changePassword(String userId, String newPass);

    Integer findPlayerIdByUsername(String username);

    void updateLastOnline(LocalDateTime now, int playerId);
}
