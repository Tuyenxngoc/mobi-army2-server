package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.Character;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class GameDao implements IGameDao {

    @Override
    public void getAllMapData() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `maps`")) {
                while (resultSet.next()) {
                    ArmyMap armyMap = new ArmyMap();
                    armyMap.setId(resultSet.getByte("map_id"));
                    armyMap.setName(resultSet.getString("name"));
                    armyMap.setFileName(resultSet.getString("file"));
                    byte[] dataMap = Utils.getFile(GameConstants.MAP_PATH + "/" + armyMap.getFileName());
                    if (dataMap == null) {
                        System.exit(1);
                    }
                    armyMap.setData(dataMap);
                    armyMap.setBg(resultSet.getShort("background"));
                    armyMap.setMapAddY(resultSet.getShort("map_add_y"));
                    armyMap.setBullEffShower(resultSet.getShort("bullet_effect_shower"));
                    armyMap.setInWaterAddY(resultSet.getShort("in_water_add_y"));
                    armyMap.setCl2AddY(resultSet.getShort("cl2_add_y"));

                    MapManager.ARMY_MAPS.add(armyMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllCharacterData() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `characters`")) {
                while (resultSet.next()) {
                    Character character = new Character();
                    character.setId(resultSet.getByte("character_id"));
                    character.setName(resultSet.getString("name"));
                    character.setPriceXu(resultSet.getInt("xu"));
                    character.setPriceLuong(resultSet.getInt("luong"));
                    character.setWindResistance(resultSet.getByte("wind_resistance"));
                    character.setMinAngle(resultSet.getByte("min_angle"));
                    character.setDamage(resultSet.getShort("damage"));
                    character.setBulletDamage(resultSet.getByte("bullet_damage"));
                    character.setBulletCount(resultSet.getByte("bullet_count"));

                    CharacterManager.CHARACTERS.add(character);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllEquip() {
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

    @Override
    public void getAllCaptionLevel() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `caption_levels`")) {
                while (resultSet.next()) {
                    Caption capEntry = new Caption();
                    capEntry.setLevel(resultSet.getByte("level"));
                    capEntry.setCaption(resultSet.getString("caption"));
                    CaptionManager.CAPTIONS.add(capEntry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllItem() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT name, xu, luong, carried_item_count FROM `fight_items`")) {
                while (resultSet.next()) {
                    FightItem fightItem = new FightItem();
                    fightItem.setName(resultSet.getString("name"));
                    fightItem.setBuyXu(resultSet.getShort("xu"));
                    fightItem.setBuyLuong(resultSet.getShort("luong"));
                    fightItem.setCarriedItemCount(resultSet.getByte("carried_item_count"));

                    FightItemManager.FIGHT_ITEMS.add(fightItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllItemClan() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `clan_shops`")) {
                while (resultSet.next()) {
                    ClanItem item = new ClanItem();
                    item.setId(resultSet.getByte("clan_shop_id"));
                    item.setLevel(resultSet.getByte("level"));
                    item.setName(resultSet.getString("name"));
                    item.setTime(resultSet.getByte("time"));
                    item.setOnSale(resultSet.getByte("on_sale"));
                    item.setXu(resultSet.getInt("xu"));
                    item.setLuong(resultSet.getInt("luong"));

                    ClanItemManager.CLAN_ITEM_MAP.put(item.getId(), item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllSpecialItem() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `special_items`")) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    SpecialItem specialItem = new SpecialItem();
                    specialItem.setId(resultSet.getByte("special_item_id"));
                    specialItem.setName(resultSet.getString("name"));
                    specialItem.setDetail(resultSet.getString("detail"));
                    specialItem.setPriceXu(resultSet.getInt("price_xu"));
                    specialItem.setPriceLuong(resultSet.getInt("price_luong"));
                    specialItem.setPriceSellXu(resultSet.getInt("price_sell_xu"));
                    specialItem.setExpirationDays(resultSet.getShort("expiration_days"));
                    specialItem.setShowSelection(resultSet.getBoolean("show_selection"));
                    specialItem.setOnSale(resultSet.getBoolean("is_on_sale"));
                    specialItem.setAbility(gson.fromJson(resultSet.getString("ability"), short[].class));

                    //Phân loại item
                    byte specialItemType = resultSet.getByte("type");
                    switch (specialItemType) {
                        case 1 -> specialItem.setGem(true);
                        case 2 -> specialItem.setMaterial(true);
                        case 3 -> specialItem.setUsable(true);
                    }

                    SpecialItemManager.addSpecialItem(specialItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllFormula() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM formula_details fd INNER JOIN formulas f on fd.formula_id = f.formula_id ORDER BY f.material_id, fd.character_id, f.level")) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    Formula entry = new Formula();
                    entry.setMaterial(SpecialItemManager.getSpecialItemById(resultSet.getByte("f.material_id")));
                    entry.setLevel(resultSet.getByte("f.level"));
                    entry.setLevelRequired(resultSet.getByte("f.level_required"));
                    entry.setEquipType(resultSet.getByte("f.equip_type"));
                    entry.setCharacterId(resultSet.getByte("fd.character_id"));
                    entry.setDetails(gson.fromJson(resultSet.getString("f.details"), String[].class));
                    entry.setAddPointsMax(gson.fromJson(resultSet.getString("f.add_points_max"), byte[].class));
                    entry.setAddPointsMin(gson.fromJson(resultSet.getString("f.add_points_min"), byte[].class));
                    entry.setAddPercentsMax(gson.fromJson(resultSet.getString("f.add_percents_max"), byte[].class));
                    entry.setAddPercentsMin(gson.fromJson(resultSet.getString("f.add_percents_min"), byte[].class));
                    entry.setRequiredEquip(CharacterManager.getEquipEntry(entry.getCharacterId(), entry.getEquipType(), resultSet.getShort("fd.required_equip")));
                    entry.setResultEquip(CharacterManager.getEquipEntry(entry.getCharacterId(), entry.getEquipType(), resultSet.getShort("fd.result_equip")));
                    SpecialItemChestJson[] json = gson.fromJson(resultSet.getString("fd.required_items"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson itemChestJson : json) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(itemChestJson.getId());
                        if (specialItem != null) {
                            entry.getRequiredItems().add(new SpecialItemChest(itemChestJson.getQuantity(), specialItem));
                        }
                    }

                    FormulaManager.addFormulaEntry(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllPayment() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `payments`")) {
                while (resultSet.next()) {
                    Payment payment = new Payment();
                    payment.setId(resultSet.getString("payment_id"));
                    payment.setInfo(resultSet.getString("info"));
                    payment.setUrl(resultSet.getString("url"));
                    payment.setMssTo(resultSet.getString("mss_to"));
                    payment.setMssContent(resultSet.getString("mss_content"));

                    PaymentManager.PAYMENT_MAP.put(payment.getId(), payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllMissions() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `missions` ORDER BY mission_type, level")) {
                while (resultSet.next()) {
                    Mission mission = new Mission();
                    mission.setId(resultSet.getByte("mission_id"));
                    mission.setType(resultSet.getByte("mission_type"));
                    mission.setLevel(resultSet.getByte("level"));
                    mission.setName(resultSet.getString("mission_name"));
                    mission.setRequirement(resultSet.getInt("requirement"));
                    mission.setReward(resultSet.getString("reward_items"));
                    mission.setRewardXu(resultSet.getInt("reward_xu"));
                    mission.setRewardLuong(resultSet.getInt("reward_luong"));
                    mission.setRewardXp(resultSet.getInt("reward_xp"));
                    mission.setRewardCup(resultSet.getInt("reward_cup"));

                    MissionManager.addMission(mission);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllXpData() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT exp_player, exp_clan, level FROM `experience_levels` ORDER BY level")) {
                int previousPlayerXp = 0;
                int previousClanXp = 0;
                boolean reachedMaxPlayerLevel = false;
                boolean reachedMaxClanLevel = false;

                while (resultSet.next()) {
                    Integer playerXp = resultSet.getObject("exp_player", Integer.class);
                    Integer clanXp = resultSet.getObject("exp_clan", Integer.class);
                    short level = resultSet.getShort("level");

                    if (!reachedMaxPlayerLevel) {
                        if (playerXp == null) {
                            reachedMaxPlayerLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho player
                            if (playerXp < previousPlayerXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho player (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", playerXp, previousPlayerXp));
                            }

                            // Tạo bản ghi cho player
                            LevelXpRequired playerXpRequired = new LevelXpRequired(level, playerXp);
                            PlayerXpManager.LEVEL_XP_REQUIRED_LIST.add(playerXpRequired);

                            previousPlayerXp = playerXp;
                        }
                    }

                    if (!reachedMaxClanLevel) {
                        if (clanXp == null) {
                            reachedMaxClanLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho clan
                            if (clanXp < previousClanXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho clan (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", clanXp, previousClanXp));
                            }

                            // Tạo bản ghi cho clan
                            LevelXpRequired clanXpRequired = new LevelXpRequired(level, clanXp);
                            ClanXpManager.LEVEL_XP_REQUIRED_LIST.add(clanXpRequired);

                            previousClanXp = clanXp;
                        }
                    }

                    if (reachedMaxPlayerLevel && reachedMaxClanLevel) {
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllFabricateItems() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `fabricate_items`")) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    FabricateItem entry = new FabricateItem();
                    entry.setId(resultSet.getInt("fabricate_item_id"));
                    entry.setXuRequire(resultSet.getInt("xu_require"));
                    entry.setLuongRequire(resultSet.getInt("luong_require"));
                    entry.setRewardXu(resultSet.getInt("reward_xu"));
                    entry.setRewardLuong(resultSet.getInt("reward_luong"));
                    entry.setRewardCup(resultSet.getInt("reward_cup"));
                    entry.setRewardExp(resultSet.getInt("reward_exp"));
                    entry.setConfirmationMessage(resultSet.getString("confirmation_message"));
                    entry.setCompletionMessage(resultSet.getString("completion_message"));

                    SpecialItemChestJson[] jsonArray = gson.fromJson(resultSet.getString("item_require"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : jsonArray) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                        if (specialItem == null) {
                            continue;
                        }
                        entry.getItemRequire().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                    }

                    jsonArray = gson.fromJson(resultSet.getString("reward_item"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : jsonArray) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                        if (specialItem == null) {
                            continue;
                        }
                        entry.getRewardItem().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                    }

                    if (!entry.getItemRequire().isEmpty() && !entry.getRewardItem().isEmpty()) {
                        FabricateItemManager.FABRICATE_ITEMS.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
