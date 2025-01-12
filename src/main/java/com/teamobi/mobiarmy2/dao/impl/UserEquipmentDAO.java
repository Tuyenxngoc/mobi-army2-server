package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IUserEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.UserEquipmentDTO;
import com.teamobi.mobiarmy2.model.EquipmentChest;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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

    @Override
    public Map<Integer, UserEquipmentDTO> findAllByIdIn(int[] ids) {
        Map<Integer, UserEquipmentDTO> result = new HashMap<>();

        List<Integer> validIds = Arrays.stream(ids)
                .filter(id -> id != -1)
                .boxed()
                .toList();

        if (validIds.isEmpty()) {
            return result;
        }

        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM user_equipments WHERE user_equipment_id IN (");
            queryBuilder.append("?,".repeat(validIds.size()));
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            queryBuilder.append(")");

            String query = queryBuilder.toString();

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < validIds.size(); i++) {
                    statement.setInt(i + 1, validIds.get(i));
                }

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

                        result.put(userEquipmentDTO.getUserEquipmentId(), userEquipmentDTO);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Optional<Integer> create(int userId, EquipmentChest equipmentChest) {
        Gson gson = GsonUtil.getInstance();

        // language=SQL
        String sql =
                "INSERT INTO `user_equipments` " +
                        "(user_id, equipment_id, vip_level, purchase_date, slots, add_points, add_percents) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        return HikariCPManager.getInstance().update(
                sql,
                userId,
                equipmentChest.getEquipment().getEquipmentId(),
                equipmentChest.getVipLevel(),
                equipmentChest.getPurchaseDate(),
                gson.toJson(equipmentChest.getSlots()),
                gson.toJson(equipmentChest.getAddPoints()),
                gson.toJson(equipmentChest.getAddPercents())
        );
    }

    @Override
    public Optional<Integer> delete(int userId, int userEquipmentId) {
        // language=SQL
        String sql = "DELETE FROM `user_equipments` WHERE user_id = ? AND user_equipment_id = ?";
        return HikariCPManager.getInstance().update(sql, userId, userEquipmentId);
    }
}
