package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.item.ClanItemDetail;
import com.teamobi.mobiarmy2.model.item.FightItem;
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
                    MapData.Map map = new MapData.Map();
                    map.id = (byte) (resultSet.getByte("id") - 1);
                    map.name = resultSet.getString("name");
                    map.fileName = resultSet.getString("file");
                    if (map.id == 27) {
                        map.data = new byte[0];
                    } else {
                        map.data = Until.getFile("res/map/" + map.fileName);
                    }
                    map.bg = resultSet.getShort("bg");
                    map.mapAddY = resultSet.getShort("mapAddY");
                    map.bullEffShower = resultSet.getShort("bullEffShower");
                    map.inWaterAddY = resultSet.getShort("inWaterAddY");
                    map.cl2AddY = resultSet.getShort("cl2AddY");
                    MapData.MAPS.add(map);
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
                    NVData.characterEntries.add(characterEntry);
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
                    CaptionData.captions.add(capEntry);
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
                    FightItem fightItem = new FightItem();
                    fightItem.setName(resultSet.getString("name"));
                    fightItem.setBuyXu(resultSet.getShort("xu"));
                    fightItem.setBuyLuong(resultSet.getShort("luong"));
                    fightItem.setCarriedItemCount(resultSet.getByte("carried_item_count"));

                    FightItemData.fightItems.add(fightItem);
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
                    ClanItemDetail item = new ClanItemDetail();
                    item.setId(resultSet.getByte("clanshop_id"));
                    item.setLevel(resultSet.getByte("level"));
                    item.setName(resultSet.getString("name"));
                    item.setTime(resultSet.getByte("time"));
                    item.setOnSale(resultSet.getByte("on_sale"));
                    item.setXu(resultSet.getInt("xu"));
                    item.setLuong(resultSet.getInt("luong"));

                    ItemClanData.clanItemsMap.put(item.getId(), item);
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
            try (ResultSet res = statement.executeQuery("SELECT * FROM `specialItem`")) {
                while (res.next()) {
                    SpecialItemData.SpecialItemEntry iEntry = new SpecialItemData.SpecialItemEntry();
                    // Id
                    iEntry.id = res.getInt("id");
                    // Ten
                    iEntry.name = res.getString("name");
                    // Detail
                    iEntry.detail = res.getString("detail");
                    // Gia xu
                    iEntry.buyXu = res.getInt("giaXu");
                    // Gia luong
                    iEntry.buyLuong = res.getInt("giaLuong");
                    // Han SD
                    iEntry.hanSD = res.getShort("hanSD");
                    // Show Chon
                    iEntry.showChon = res.getBoolean("showChon");
                    // OnSale
                    iEntry.onSale = res.getBoolean("onSale");
                    if (iEntry.onSale) {
                        iEntry.indexSale = SpecialItemData.nSaleItem;
                        SpecialItemData.nSaleItem++;
                    }
                    iEntry.ability = GsonUtil.GSON.fromJson(res.getString("ability"), short[].class);
                    SpecialItemData.entrys.add(iEntry);
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
                    PaymentData.Payment payment = new PaymentData.Payment();
                    payment.id = resultSet.getString("payment_id");
                    payment.info = resultSet.getString("info");
                    payment.url = resultSet.getString("url");
                    payment.mssTo = resultSet.getString("mss_to");
                    payment.mssContent = resultSet.getString("mss_content");

                    PaymentData.payments.put(payment.id, payment);
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
