package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserEquipmentDAO implements IUserEquipmentDAO {

    @Override
    public List<UserEquipmentDTO> findAllByUserId(int userId) {
        List<UserEquipmentDTO> result = new ArrayList<>();

        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String query = "SELECT * FROM user_equipments WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    Gson gson = GsonUtil.getInstance();
                    while (resultSet.next()) {
                        UserEquipmentDTO userEquipmentDTO = new UserEquipmentDTO();
                        userEquipmentDTO.setUserEquipmentId(resultSet.getInt("user_equipment_id"));
                        userEquipmentDTO.setEquipmentId(resultSet.getShort("equipment_id"));
                        userEquipmentDTO.setVipLevel(resultSet.getByte("vip_level"));
                        userEquipmentDTO.setPurchaseDate(resultSet.getTimestamp("purchase_date").toLocalDateTime());
                        userEquipmentDTO.setInUse(resultSet.getBoolean("in_use"));
                        userEquipmentDTO.setSlots(gson.fromJson(resultSet.getString("slots"), byte[].class));
                        userEquipmentDTO.setAddPoints(gson.fromJson(resultSet.getString("add_points"), byte[].class));
                        userEquipmentDTO.setAddPercents(gson.fromJson(resultSet.getString("add_percents"), byte[].class));

                        result.add(userEquipmentDTO);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
