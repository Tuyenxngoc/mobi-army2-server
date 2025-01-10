package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.server.CharacterManager;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EquipmentDAO implements IEquipmentDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            EquipmentManager.clear();

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `equipments` ORDER BY equip_type, equip_index, character_id")) {

                //Khởi tại danh sách trang bị mặc định ban đầu
                EquipmentManager.equipDefault = new Equipment[CharacterManager.CHARACTERS.size()][5];
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    Equipment equipment = new Equipment();
                    equipment.setEquipmentId(resultSet.getShort("equipment_id"));
                    equipment.setCharacterId(resultSet.getByte("character_id"));
                    equipment.setEquipType(resultSet.getByte("equip_type"));
                    equipment.setEquipIndex(resultSet.getShort("equip_index"));
                    equipment.setName(resultSet.getString("name"));
                    equipment.setPriceXu(resultSet.getInt("price_xu"));
                    equipment.setPriceLuong(resultSet.getInt("price_luong"));
                    equipment.setExpirationDays(resultSet.getByte("expiration_days"));
                    equipment.setLevelRequirement(resultSet.getByte("level_requirement"));
                    equipment.setFrameCount(resultSet.getShort("frame_count"));
                    equipment.setBulletId(resultSet.getByte("bullet_id"));
                    equipment.setOnSale(resultSet.getBoolean("on_sale"));
                    equipment.setDisguise(resultSet.getBoolean("is_disguise"));
                    equipment.setDisguiseEquippedIndexes(gson.fromJson(resultSet.getString("disguise_equipped_indexes"), short[].class));
                    equipment.setBigImageCutX(gson.fromJson(resultSet.getString("big_image_cut_x"), short[].class));
                    equipment.setBigImageCutY(gson.fromJson(resultSet.getString("big_image_cut_y"), short[].class));
                    equipment.setBigImageSizeX(gson.fromJson(resultSet.getString("big_image_size_x"), byte[].class));
                    equipment.setBigImageSizeY(gson.fromJson(resultSet.getString("big_image_size_y"), byte[].class));
                    equipment.setBigImageAlignX(gson.fromJson(resultSet.getString("big_image_align_x"), byte[].class));
                    equipment.setBigImageAlignY(gson.fromJson(resultSet.getString("big_image_align_y"), byte[].class));
                    equipment.setAddPoints(gson.fromJson(resultSet.getString("additional_points"), byte[].class));
                    equipment.setAddPercents(gson.fromJson(resultSet.getString("additional_percent"), byte[].class));

                    if (equipment.isDisguise() && equipment.getDisguiseEquippedIndexes().length != 5) {
                        throw new SQLException("Invalid disguise configuration for EquipmentEntry with ID: " +
                                equipment.getEquipIndex() +
                                ". Expected 5 disguise equipped indexes, but found " +
                                equipment.getDisguiseEquippedIndexes().length);
                    }

                    if (equipment.getBigImageCutX().length != 6 ||
                            equipment.getBigImageCutY().length != 6 ||
                            equipment.getBigImageSizeX().length != 6 ||
                            equipment.getBigImageSizeY().length != 6 ||
                            equipment.getBigImageAlignX().length != 6 ||
                            equipment.getBigImageAlignY().length != 6) {
                        StringBuilder errorMessage = new StringBuilder("Invalid image configuration for EquipmentEntry with ID: ");
                        errorMessage.append(equipment.getEquipIndex());
                        errorMessage.append(". Expected arrays of length 6 for all image properties but found:\n");

                        if (equipment.getBigImageCutX().length != 6) {
                            errorMessage.append("  - bigImageCutX length: ").append(equipment.getBigImageCutX().length).append("\n");
                        }
                        if (equipment.getBigImageCutY().length != 6) {
                            errorMessage.append("  - bigImageCutY length: ").append(equipment.getBigImageCutY().length).append("\n");
                        }
                        if (equipment.getBigImageSizeX().length != 6) {
                            errorMessage.append("  - bigImageSizeX length: ").append(equipment.getBigImageSizeX().length).append("\n");
                        }
                        if (equipment.getBigImageSizeY().length != 6) {
                            errorMessage.append("  - bigImageSizeY length: ").append(equipment.getBigImageSizeY().length).append("\n");
                        }
                        if (equipment.getBigImageAlignX().length != 6) {
                            errorMessage.append("  - bigImageAlignX length: ").append(equipment.getBigImageAlignX().length).append("\n");
                        }
                        if (equipment.getBigImageAlignY().length != 6) {
                            errorMessage.append("  - bigImageAlignY length: ").append(equipment.getBigImageAlignY().length).append("\n");
                        }

                        throw new SQLException(errorMessage.toString());
                    }

                    if (equipment.getAddPoints().length != 5 || equipment.getAddPercents().length != 5) {
                        throw new SQLException("Invalid additional points or percents configuration for EquipmentEntry with ID: " +
                                equipment.getEquipIndex() +
                                ". Expected arrays of length 5 but found:\n");
                    }

                    //Đặt trang bị mặc định cho nhân vật
                    if (resultSet.getBoolean("is_default")) {
                        EquipmentManager.equipDefault[equipment.getCharacterId()][equipment.getEquipType()] = equipment;
                    }

                    EquipmentManager.addEquip(equipment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
