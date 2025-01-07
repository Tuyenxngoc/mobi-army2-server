package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserSpecialItemDTO;

import java.util.List;

public interface IUserSpecialItemDAO {
    List<UserSpecialItemDTO> findAllByUserId(int userId);
}
