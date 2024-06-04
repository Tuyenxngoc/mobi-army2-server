package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.entry.CaptionEntry;
import com.teamobi.mobiarmy2.model.entry.LevelXpRequiredEntry;
import com.teamobi.mobiarmy2.model.entry.MissionEntry;
import com.teamobi.mobiarmy2.model.entry.PaymentEntry;
import com.teamobi.mobiarmy2.model.entry.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.item.ClanItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.FightItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Until;

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

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `map`")) {
                while (resultSet.next()) {
                    MapEntry map = new MapEntry();
                    map.setId(resultSet.getByte("map_id"));
                    map.setName(resultSet.getString("name"));
                    map.setFileName(resultSet.getString("file"));
                    if (map.getId() == 27) {
                        map.setData(new byte[0]);
                    } else {
                        byte[] dataMap = Until.getFile("res/map/" + map.getFileName());
                        if (dataMap == null) {
                            System.exit(1);
                        }
                        map.setData(dataMap);
                    }
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `character`")) {
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

                    NVData.CHARACTER_ENTRIES.add(characterEntry);
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `equip`")) {
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
                    equipEntry.setDisguiseEquippedIndexes(GsonUtil.GSON.fromJson(resultSet.getString("disguise_equipped_indexes"), short[].class));
                    equipEntry.setBigImageCutX(GsonUtil.GSON.fromJson(resultSet.getString("big_image_cut_x"), short[].class));
                    equipEntry.setBigImageCutY(GsonUtil.GSON.fromJson(resultSet.getString("big_image_cut_y"), short[].class));
                    equipEntry.setBigImageSizeX(GsonUtil.GSON.fromJson(resultSet.getString("big_image_size_x"), byte[].class));
                    equipEntry.setBigImageSizeY(GsonUtil.GSON.fromJson(resultSet.getString("big_image_size_y"), byte[].class));
                    equipEntry.setBigImageAlignX(GsonUtil.GSON.fromJson(resultSet.getString("big_image_align_x"), byte[].class));
                    equipEntry.setBigImageAlignY(GsonUtil.GSON.fromJson(resultSet.getString("big_image_align_y"), byte[].class));
                    equipEntry.setAddPoints(GsonUtil.GSON.fromJson(resultSet.getString("additional_points"), byte[].class));
                    equipEntry.setAddPercents(GsonUtil.GSON.fromJson(resultSet.getString("additional_percent"), byte[].class));

                    NVData.addEquip(equipEntry);
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `captionlv`")) {
                while (resultSet.next()) {
                    CaptionEntry capEntry = new CaptionEntry();
                    capEntry.setLevel(resultSet.getByte("lvl"));
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
            try (ResultSet resultSet = statement.executeQuery("SELECT name, xu, luong, carried_item_count FROM `fight_item`")) {
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `clanshop`")) {
                while (resultSet.next()) {
                    ClanItemEntry item = new ClanItemEntry();
                    item.setId(resultSet.getByte("clanshop_id"));
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `special_item`")) {
                while (resultSet.next()) {
                    SpecialItemEntry specialItemEntry = new SpecialItemEntry();
                    specialItemEntry.setId(resultSet.getByte("special_item_id"));
                    specialItemEntry.setName(resultSet.getString("name"));
                    specialItemEntry.setDetail(resultSet.getString("detail"));
                    specialItemEntry.setPriceXu(resultSet.getInt("priceXu"));
                    specialItemEntry.setPriceLuong(resultSet.getInt("priceLuong"));
                    specialItemEntry.setPriceSellXu(resultSet.getInt("priceSellXu"));
                    specialItemEntry.setExpiration_days(resultSet.getShort("expiration_days"));
                    specialItemEntry.setShowSelection(resultSet.getBoolean("showSelection"));
                    specialItemEntry.setOnSale(resultSet.getBoolean("isOnSale"));
                    specialItemEntry.setAbility(GsonUtil.GSON.fromJson(resultSet.getString("ability"), short[].class));

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

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `fomular`;")) {
                while (resultSet.next()) {
                    //Todo create fomular
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `payment`")) {
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `mission` ORDER BY mission_type, level")) {
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
            try (ResultSet resultSet = statement.executeQuery("SELECT experience_points, level FROM `experience_level` ORDER BY level")) {
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

}
