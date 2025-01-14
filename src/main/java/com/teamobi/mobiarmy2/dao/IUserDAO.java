package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.constant.TransactionType;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
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

    void updateOnline(boolean flag, int playerId);

    List<FriendDTO> getFriendsList(int playerId, List<Integer> friends);

    boolean existsByUserIdAndPassword(String userId, String oldPass);

    void changePassword(String userId, String newPass);

    Integer findPlayerIdByUsername(String username);

    void updateLastOnline(LocalDateTime time, int playerId);

    boolean createPlayerCharacter(int playerId, byte characterId);

    UserCharacterDTO getPlayerCharacter(int playerId, byte characterId);

    void createTransaction(TransactionType type, int amount, int playerId);

}
