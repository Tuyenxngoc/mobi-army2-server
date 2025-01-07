package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;

import java.util.List;

public interface IUserEquipmentDAO {
    List<UserEquipmentDTO> findAllByUserId(int userId);
}
