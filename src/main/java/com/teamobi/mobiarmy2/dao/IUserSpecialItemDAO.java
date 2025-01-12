package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserSpecialItemDTO;
import com.teamobi.mobiarmy2.model.SpecialItemChest;

import java.util.List;
import java.util.Optional;

public interface IUserSpecialItemDAO {
    List<UserSpecialItemDTO> findAllByUserId(int userId);

    Optional<Integer> create(int userId, SpecialItemChest specialItemChest);

    Optional<Integer> delete(int userId, byte specialItemId);
}
