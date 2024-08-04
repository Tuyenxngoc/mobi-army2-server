package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.CharacterData;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.entry.*;
import com.teamobi.mobiarmy2.model.entry.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.item.ClanItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.FightItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
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
                    MapEntry map = new MapEntry();
                    map.setId(resultSet.getByte("map_id"));
                    map.setName(resultSet.getString("name"));
                    map.setFileName(resultSet.getString("file"));
                    byte[] dataMap = Utils.getFile("res/map/" + map.getFileName());
                    if (dataMap == null) {
                        System.exit(1);
                    }
                    map.setData(dataMap);
                    map.setBg(resultSet.getShort("background"));
                    map.setMapAddY(resultSet.getShort("map_add_y"));
                    map.setBullEffShower(resultSet.getShort("bullet_effect_shower"));
                    map.setInWaterAddY(resultSet.getShort("in_water_add_y"));
                    map.setCl2AddY(resultSet.getShort("cl2_add_y"));

                    MapData.MAP_ENTRIES.add(map);
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
                    CharacterEntry characterEntry = new CharacterEntry();
                    characterEntry.setId(resultSet.getByte("character_id"));
                    characterEntry.setName(resultSet.getString("name"));
                    characterEntry.setPriceXu(resultSet.getInt("xu"));
                    characterEntry.setPriceLuong(resultSet.getInt("luong"));
                    characterEntry.setWindResistance(resultSet.getByte("wind_resistance"));
                    characterEntry.setMinAngle(resultSet.getByte("min_angle"));
                    characterEntry.setDamage(resultSet.getShort("damage"));
                    characterEntry.setBulletDamage(resultSet.getByte("bullet_damage"));
                    characterEntry.setBulletCount(resultSet.getByte("bullet_count"));

                    CharacterData.CHARACTER_ENTRIES.add(characterEntry);
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
                User.equipDefault = new EquipmentEntry[CharacterData.CHARACTER_ENTRIES.size()][5];
                Gson gson = GsonUtil.GSON;
                while (resultSet.next()) {
                    EquipmentEntry equipEntry = new EquipmentEntry();
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

                    //Đặt trang bị mặc định cho nhân vật
                    if (resultSet.getBoolean("is_default")) {
                        User.equipDefault[equipEntry.getCharacterId()][equipEntry.getEquipType()] = equipEntry;
                    }

                    CharacterData.addEquip(equipEntry);
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
                    CaptionEntry capEntry = new CaptionEntry();
                    capEntry.setLevel(resultSet.getByte("level"));
                    capEntry.setCaption(resultSet.getString("caption"));
                    CaptionData.CAPTION_ENTRIES.add(capEntry);
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
                    FightItemEntry fightItemEntry = new FightItemEntry();
                    fightItemEntry.setName(resultSet.getString("name"));
                    fightItemEntry.setBuyXu(resultSet.getShort("xu"));
                    fightItemEntry.setBuyLuong(resultSet.getShort("luong"));
                    fightItemEntry.setCarriedItemCount(resultSet.getByte("carried_item_count"));

                    FightItemData.FIGHT_ITEM_ENTRIES.add(fightItemEntry);
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
                    ClanItemEntry item = new ClanItemEntry();
                    item.setId(resultSet.getByte("clan_shop_id"));
                    item.setLevel(resultSet.getByte("level"));
                    item.setName(resultSet.getString("name"));
                    item.setTime(resultSet.getByte("time"));
                    item.setOnSale(resultSet.getByte("on_sale"));
                    item.setXu(resultSet.getInt("xu"));
                    item.setLuong(resultSet.getInt("luong"));

                    ItemClanData.CLAN_ITEM_ENTRY_MAP.put(item.getId(), item);
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
                Gson gson = GsonUtil.GSON;
                while (resultSet.next()) {
                    SpecialItemEntry specialItemEntry = new SpecialItemEntry();
                    specialItemEntry.setId(resultSet.getByte("special_item_id"));
                    specialItemEntry.setName(resultSet.getString("name"));
                    specialItemEntry.setDetail(resultSet.getString("detail"));
                    specialItemEntry.setPriceXu(resultSet.getInt("price_xu"));
                    specialItemEntry.setPriceLuong(resultSet.getInt("price_luong"));
                    specialItemEntry.setPriceSellXu(resultSet.getInt("price_sell_xu"));
                    specialItemEntry.setExpirationDays(resultSet.getShort("expiration_days"));
                    specialItemEntry.setShowSelection(resultSet.getBoolean("show_selection"));
                    specialItemEntry.setOnSale(resultSet.getBoolean("is_on_sale"));
                    specialItemEntry.setAbility(gson.fromJson(resultSet.getString("ability"), short[].class));

                    //Phân loại item
                    byte specialItemType = resultSet.getByte("type");
                    switch (specialItemType) {
                        case 1 -> specialItemEntry.setGem(true);
                        case 2 -> specialItemEntry.setMaterial(true);
                        case 3 -> specialItemEntry.setUsable(true);
                    }

                    SpecialItemData.SPECIAL_ITEM_ENTRIES.add(specialItemEntry);
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
                Gson gson = GsonUtil.GSON;
                while (resultSet.next()) {
                    FormulaEntry entry = new FormulaEntry();
                    entry.setMaterial(SpecialItemData.getSpecialItemById(resultSet.getByte("f.material_id")));
                    entry.setLevel(resultSet.getByte("f.level"));
                    entry.setLevelRequired(resultSet.getByte("f.level_required"));
                    entry.setEquipType(resultSet.getByte("f.equip_type"));
                    entry.setCharacterId(resultSet.getByte("fd.character_id"));
                    entry.setDetails(gson.fromJson(resultSet.getString("f.details"), String[].class));
                    entry.setAddPointsMax(gson.fromJson(resultSet.getString("f.add_points_max"), byte[].class));
                    entry.setAddPointsMin(gson.fromJson(resultSet.getString("f.add_points_min"), byte[].class));
                    entry.setAddPercentsMax(gson.fromJson(resultSet.getString("f.add_percents_max"), byte[].class));
                    entry.setAddPercentsMin(gson.fromJson(resultSet.getString("f.add_percents_min"), byte[].class));
                    entry.setRequiredEquip(CharacterData.getEquipEntry(entry.getCharacterId(), entry.getEquipType(), resultSet.getShort("fd.required_equip")));
                    entry.setResultEquip(CharacterData.getEquipEntry(entry.getCharacterId(), entry.getEquipType(), resultSet.getShort("fd.result_equip")));
                    SpecialItemChestJson[] json = gson.fromJson(resultSet.getString("fd.required_items"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson itemChestJson : json) {
                        SpecialItemEntry specialItemEntry = SpecialItemData.getSpecialItemById(itemChestJson.getId());
                        if (specialItemEntry != null) {
                            entry.getRequiredItems().add(new SpecialItemChestEntry(itemChestJson.getQuantity(), specialItemEntry));
                        }
                    }

                    FormulaData.addFormulaEntry(entry);
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
                    PaymentEntry paymentEntry = new PaymentEntry();
                    paymentEntry.setId(resultSet.getString("payment_id"));
                    paymentEntry.setInfo(resultSet.getString("info"));
                    paymentEntry.setUrl(resultSet.getString("url"));
                    paymentEntry.setMssTo(resultSet.getString("mss_to"));
                    paymentEntry.setMssContent(resultSet.getString("mss_content"));

                    PaymentData.PAYMENT_ENTRY_MAP.put(paymentEntry.getId(), paymentEntry);
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
                    MissionEntry missionEntry = new MissionEntry();
                    missionEntry.setId(resultSet.getByte("mission_id"));
                    missionEntry.setType(resultSet.getByte("mission_type"));
                    missionEntry.setLevel(resultSet.getByte("level"));
                    missionEntry.setName(resultSet.getString("mission_name"));
                    missionEntry.setRequirement(resultSet.getInt("requirement"));
                    missionEntry.setReward(resultSet.getString("reward_items"));
                    missionEntry.setRewardXu(resultSet.getInt("reward_xu"));
                    missionEntry.setRewardLuong(resultSet.getInt("reward_luong"));
                    missionEntry.setRewardXp(resultSet.getInt("reward_xp"));
                    missionEntry.setRewardCup(resultSet.getInt("reward_cup"));

                    MissionData.addMission(missionEntry);
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
            try (ResultSet resultSet = statement.executeQuery("SELECT experience_points, level FROM `experience_levels` ORDER BY level")) {
                int previousXp = 0;
                while (resultSet.next()) {
                    int currentXp = resultSet.getInt("experience_points");
                    if (currentXp < previousXp) {
                        throw new SQLException(String.format("XP of the next level (%d) is lower than the XP of the previous level (%d)!", currentXp, previousXp));
                    }
                    LevelXpRequiredEntry xpRequired = new LevelXpRequiredEntry();
                    xpRequired.setLevel(resultSet.getShort("level"));
                    xpRequired.setXp(currentXp);
                    XpData.LEVEL_XP_REQUIRED_ENTRIES.add(xpRequired);
                    previousXp = currentXp;
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
                Gson gson = GsonUtil.GSON;
                while (resultSet.next()) {
                    FabricateItemEntry entry = new FabricateItemEntry();
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
                        SpecialItemEntry specialItemEntry = SpecialItemData.SPECIAL_ITEM_ENTRIES.get(specialItemChestJson.getId());
                        if (specialItemEntry == null) {
                            continue;
                        }
                        entry.getItemRequire().add(new SpecialItemChestEntry(specialItemChestJson.getQuantity(), specialItemEntry));
                    }

                    jsonArray = gson.fromJson(resultSet.getString("reward_item"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : jsonArray) {
                        SpecialItemEntry specialItemEntry = SpecialItemData.SPECIAL_ITEM_ENTRIES.get(specialItemChestJson.getId());
                        if (specialItemEntry == null) {
                            continue;
                        }
                        entry.getRewardItem().add(new SpecialItemChestEntry(specialItemChestJson.getQuantity(), specialItemEntry));
                    }

                    if (!entry.getItemRequire().isEmpty() && !entry.getRewardItem().isEmpty()) {
                        FabricateItemData.FABRICATE_ITEM_ENTRIES.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
