package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserCharacterDTO;

import java.util.List;
import java.util.Optional;

public interface IUserCharacterDAO {
    List<UserCharacterDTO> findAllByUserId(int userId);

    Optional<Integer> create(int userId, byte characterId, boolean isActive);
}
