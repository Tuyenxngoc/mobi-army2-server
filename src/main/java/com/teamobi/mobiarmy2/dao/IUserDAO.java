package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author tuyen
 */
public interface IUserDAO {

    Optional<Integer> create(String accountId, int xu, int luong);

    void update(User user);

    UserDTO findByAccountId(String accountId);

    void setOnline(int userId, boolean online);

    void setDailyRewardTime(int userId, LocalDateTime now);

    List<FriendDTO> getFriendsList(int userId, List<Integer> friends);

    Optional<Integer> findUserIdByUsername(String username);

}
