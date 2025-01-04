package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.constant.TransactionType;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.PlayerCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserDTO;
import com.teamobi.mobiarmy2.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author tuyen
 */
public interface IUserDAO {

    void save(User user);

    void update(User user);

    UserDTO findByAccountId(String accountId);

    void updateOnline(boolean flag, int userId);

    List<FriendDTO> getFriendsList(int userId, List<Integer> friends);

    boolean existsByUserIdAndPassword(String userId, String oldPass);

    void changePassword(String userId, String newPass);

    Integer findPlayerIdByUsername(String username);

    void updateLastOnline(LocalDateTime time, int userId);

    boolean createPlayerCharacter(int userId, byte characterId);

    PlayerCharacterDTO getPlayerCharacter(int userId, byte characterId);

    void createTransaction(TransactionType type, int amount, int userId);

}
