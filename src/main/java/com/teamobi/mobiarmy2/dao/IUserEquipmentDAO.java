package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;

import java.util.List;
import java.util.Map;

public interface IUserEquipmentDAO {
    List<UserEquipmentDTO> findAllByUserId(int userId);

    Map<Integer, UserEquipmentDTO> findAllByIdIn(int[] ids);
}
