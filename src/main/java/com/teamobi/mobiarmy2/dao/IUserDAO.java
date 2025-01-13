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

    void create(User user);

    void update(User user);

    UserDTO findByUsernameAndPassword(String username, String password);

    void updateOnline(boolean flag, int playerId);

    List<FriendDTO> getFriendsList(int playerId, List<Integer> friends);

    boolean existsByUserIdAndPassword(String userId, String oldPass);

    void changePassword(String userId, String newPass);

    Integer findPlayerIdByUsername(String username);

    void updateLastOnline(LocalDateTime time, int playerId);

    boolean createPlayerCharacter(int playerId, byte characterId);

    PlayerCharacterDTO getPlayerCharacter(int playerId, byte characterId);

    void createTransaction(TransactionType type, int amount, int playerId);

}
