package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserCharacterDTO;

import java.util.List;
import java.util.Optional;

public interface IUserCharacterDAO {
    List<UserCharacterDTO> findAllByUserId(int userId);

    UserCharacterDTO findByUserIdAndCharacterId(int userId, byte characterId);

    Optional<Integer> create(int userId, byte characterId);

    void update(UserCharacterDTO userCharacterDTO);

    void updateAll(List<UserCharacterDTO> userCharacterDTOs);
}
