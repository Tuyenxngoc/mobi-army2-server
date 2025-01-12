package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;
import com.teamobi.mobiarmy2.model.EquipmentChest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserEquipmentDAO {
    List<UserEquipmentDTO> findAllByUserId(int userId);

    Map<Integer, UserEquipmentDTO> findAllByIdIn(int[] ids);

    Optional<Integer> create(int userId, EquipmentChest equipmentChest);
}
