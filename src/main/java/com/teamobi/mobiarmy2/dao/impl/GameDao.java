package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.item.ClanItemEntry;
import com.teamobi.mobiarmy2.model.item.FightItemEntry;
import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.map.MapEntry;
import com.teamobi.mobiarmy2.model.mission.Mission;
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
                    map.id = resultSet.getByte("map_id");
                    map.name = resultSet.getString("name");
                    map.fileName = resultSet.getString("file");
                    if (map.id == 27) {
                        map.data = new byte[0];
                    } else {
                        byte[] dataMap = Until.getFile("res/map/" + map.fileName);
                        if (dataMap == null) {
                            System.exit(1);
                        }
                        map.data = dataMap;
                    }
                    map.bg = resultSet.getShort("background");
                    map.mapAddY = resultSet.getShort("map_add_y");
                    map.bullEffShower = resultSet.getShort("bullet_effect_shower");
                    map.inWaterAddY = resultSet.getShort("in_water_add_y");
                    map.cl2AddY = resultSet.getShort("cl2_add_y");

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
                    characterEntry.id = resultSet.getByte("character_id");
                    characterEntry.name = resultSet.getString("name");
                    characterEntry.buyXu = resultSet.getInt("xu");
                    characterEntry.buyLuong = resultSet.getInt("luong");
                    characterEntry.windResistance = resultSet.getByte("ma_sat_gio");
                    characterEntry.minAngle = resultSet.getByte("goc_min");
                    characterEntry.bulletCount = resultSet.getByte("so_dan");
                    characterEntry.damage = resultSet.getShort("sat_thuong");
                    characterEntry.bulletDamage = resultSet.getByte("sat_thuong_dan");
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
                    equipEntry.setIndex(resultSet.getShort("equip_index"));
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
                    equipEntry.setAdditionalPoints(GsonUtil.GSON.fromJson(resultSet.getString("additional_points"), byte[].class));
                    equipEntry.setAdditionalPercent(GsonUtil.GSON.fromJson(resultSet.getString("additional_percent"), byte[].class));

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
                    CaptionData.Caption capEntry = new CaptionData.Caption();
                    capEntry.setLevel(resultSet.getByte("lvl"));
                    capEntry.setCaption(resultSet.getString("caption"));
                    CaptionData.CAPTIONS.add(capEntry);
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
                    PaymentData.PaymentEntry paymentEntry = new PaymentData.PaymentEntry();
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

                    MissionData.addMission(mission);
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `xp_lv` ORDER BY lvl")) {
                int previousXp = 0;
                while (resultSet.next()) {
                    int currentXp = resultSet.getInt("exp");
                    if (currentXp < previousXp) {
                        throw new SQLException(String.format("XP of the next level (%d) is lower than the XP of the previous level (%d)!", currentXp, previousXp));
                    }
                    XpData.LevelXpRequired xpRequired = new XpData.LevelXpRequired();
                    xpRequired.level = resultSet.getInt("lvl");
                    xpRequired.xp = currentXp;
                    XpData.xpList.add(xpRequired);
                    previousXp = currentXp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
