package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserCharacterDTO;

import java.util.List;

public interface IUserCharacterDAO {
    List<UserCharacterDTO> findAllByUserId(int userId);
}
