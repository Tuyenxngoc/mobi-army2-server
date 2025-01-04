package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IEquipmentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.CharacterManager;
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

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `equips` ORDER BY equip_type, equip_index, character_id")) {

                //Khởi tại danh sách trang bị mặc định ban đầu
                User.equipDefault = new Equipment[CharacterManager.CHARACTERS.size()][5];
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    Equipment equipEntry = new Equipment();
                    equipEntry.setCharacterId(resultSet.getByte("character_id"));
                    equipEntry.setEquipType(resultSet.getByte("equip_type"));
                    equipEntry.setEquipIndex(resultSet.getShort("equip_index"));
                    equipEntry.setName(resultSet.getString("name"));
                    equipEntry.setPriceXu(resultSet.getInt("price_xu"));
                    equipEntry.setPriceLuong(resultSet.getInt("price_luong"));
                    equipEntry.setExpirationDays(resultSet.getByte("expiration_days"));
                    equipEntry.setLevelRequirement(resultSet.getByte("level_requirement"));
                    equipEntry.setFrameCount(resultSet.getShort("frame_count"));
                    equipEntry.setBulletId(resultSet.getByte("bullet_id"));
                    equipEntry.setOnSale(resultSet.getBoolean("on_sale"));
                    equipEntry.setDisguise(resultSet.getBoolean("is_disguise"));
                    equipEntry.setDisguiseEquippedIndexes(gson.fromJson(resultSet.getString("disguise_equipped_indexes"), short[].class));
                    equipEntry.setBigImageCutX(gson.fromJson(resultSet.getString("big_image_cut_x"), short[].class));
                    equipEntry.setBigImageCutY(gson.fromJson(resultSet.getString("big_image_cut_y"), short[].class));
                    equipEntry.setBigImageSizeX(gson.fromJson(resultSet.getString("big_image_size_x"), byte[].class));
                    equipEntry.setBigImageSizeY(gson.fromJson(resultSet.getString("big_image_size_y"), byte[].class));
                    equipEntry.setBigImageAlignX(gson.fromJson(resultSet.getString("big_image_align_x"), byte[].class));
                    equipEntry.setBigImageAlignY(gson.fromJson(resultSet.getString("big_image_align_y"), byte[].class));
                    equipEntry.setAddPoints(gson.fromJson(resultSet.getString("additional_points"), byte[].class));
                    equipEntry.setAddPercents(gson.fromJson(resultSet.getString("additional_percent"), byte[].class));

                    if (equipEntry.isDisguise() && equipEntry.getDisguiseEquippedIndexes().length != 5) {
                        throw new SQLException("Invalid disguise configuration for EquipmentEntry with ID: " +
                                equipEntry.getEquipIndex() +
                                ". Expected 5 disguise equipped indexes, but found " +
                                equipEntry.getDisguiseEquippedIndexes().length);
                    }

                    if (equipEntry.getBigImageCutX().length != 6 ||
                            equipEntry.getBigImageCutY().length != 6 ||
                            equipEntry.getBigImageSizeX().length != 6 ||
                            equipEntry.getBigImageSizeY().length != 6 ||
                            equipEntry.getBigImageAlignX().length != 6 ||
                            equipEntry.getBigImageAlignY().length != 6) {
                        StringBuilder errorMessage = new StringBuilder("Invalid image configuration for EquipmentEntry with ID: ");
                        errorMessage.append(equipEntry.getEquipIndex());
                        errorMessage.append(". Expected arrays of length 6 for all image properties but found:\n");

                        if (equipEntry.getBigImageCutX().length != 6) {
                            errorMessage.append("  - bigImageCutX length: ").append(equipEntry.getBigImageCutX().length).append("\n");
                        }
                        if (equipEntry.getBigImageCutY().length != 6) {
                            errorMessage.append("  - bigImageCutY length: ").append(equipEntry.getBigImageCutY().length).append("\n");
                        }
                        if (equipEntry.getBigImageSizeX().length != 6) {
                            errorMessage.append("  - bigImageSizeX length: ").append(equipEntry.getBigImageSizeX().length).append("\n");
                        }
                        if (equipEntry.getBigImageSizeY().length != 6) {
                            errorMessage.append("  - bigImageSizeY length: ").append(equipEntry.getBigImageSizeY().length).append("\n");
                        }
                        if (equipEntry.getBigImageAlignX().length != 6) {
                            errorMessage.append("  - bigImageAlignX length: ").append(equipEntry.getBigImageAlignX().length).append("\n");
                        }
                        if (equipEntry.getBigImageAlignY().length != 6) {
                            errorMessage.append("  - bigImageAlignY length: ").append(equipEntry.getBigImageAlignY().length).append("\n");
                        }

                        throw new SQLException(errorMessage.toString());
                    }

                    if (equipEntry.getAddPoints().length != 5 || equipEntry.getAddPercents().length != 5) {
                        throw new SQLException("Invalid additional points or percents configuration for EquipmentEntry with ID: " +
                                equipEntry.getEquipIndex() +
                                ". Expected arrays of length 5 but found:\n");
                    }

                    //Đặt trang bị mặc định cho nhân vật
                    if (resultSet.getBoolean("is_default")) {
                        User.equipDefault[equipEntry.getCharacterId()][equipEntry.getEquipType()] = equipEntry;
                    }

                    CharacterManager.addEquip(equipEntry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
