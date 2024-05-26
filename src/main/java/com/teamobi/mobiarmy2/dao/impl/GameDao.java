package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.*;
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
                    NVData.NVEntry nvEntry = new NVData.NVEntry();
                    nvEntry.id = (byte) (resultSet.getByte("character_id") - 1);
                    nvEntry.name = resultSet.getString("name");
                    nvEntry.buyXu = resultSet.getInt("xu");
                    nvEntry.buyLuong = resultSet.getInt("luong");
                    nvEntry.ma_sat_gio = resultSet.getByte("ma_sat_gio");
                    nvEntry.goc_min = resultSet.getByte("goc_min");
                    nvEntry.so_dan = resultSet.getByte("so_dan");
                    nvEntry.sat_thuong = resultSet.getShort("sat_thuong");
                    nvEntry.sat_thuong_dan = resultSet.getByte("sat_thuong_dan");
                    NVData.entrys.add(nvEntry);
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
                    NVData.EquipmentEntry equipEntry = new NVData.EquipmentEntry();
                    equipEntry.characterId = resultSet.getByte("character_id");
                    equipEntry.idEquipDat = resultSet.getByte("equip_type");
                    equipEntry.id = resultSet.getShort("equipId");
                    equipEntry.name = resultSet.getString("name");
                    equipEntry.giaXu = resultSet.getInt("giaXu");
                    equipEntry.giaLuong = resultSet.getInt("giaLuong");
                    equipEntry.hanSD = resultSet.getInt("hanSD");
                    equipEntry.lvRequire = resultSet.getByte("lvRequire");
                    equipEntry.frame = resultSet.getShort("frame");
                    equipEntry.bullId = resultSet.getByte("bullId");
                    equipEntry.onSale = resultSet.getBoolean("onSale");
                    equipEntry.isSet = resultSet.getBoolean("isSet");
                    equipEntry.cap = resultSet.getByte("cap");
                    equipEntry.bigImageCutX = GsonUtil.GSON.fromJson(resultSet.getString("bigCutX"), short[].class);
                    equipEntry.bigImageCutY = GsonUtil.GSON.fromJson(resultSet.getString("bigCutY"), short[].class);
                    equipEntry.bigImageSizeX = GsonUtil.GSON.fromJson(resultSet.getString("bigSizeX"), byte[].class);
                    equipEntry.bigImageSizeY = GsonUtil.GSON.fromJson(resultSet.getString("bigSizeY"), byte[].class);
                    equipEntry.bigImageAlignX = GsonUtil.GSON.fromJson(resultSet.getString("bigAlignX"), byte[].class);
                    equipEntry.bigImageAlignY = GsonUtil.GSON.fromJson(resultSet.getString("bigAlignY"), byte[].class);
                    equipEntry.arraySet = GsonUtil.GSON.fromJson(resultSet.getString("arraySet"), short[].class);
                    equipEntry.invAdd = GsonUtil.GSON.fromJson(resultSet.getString("addPN"), byte[].class);
                    equipEntry.percentAdd = GsonUtil.GSON.fromJson(resultSet.getString("addPN100"), byte[].class);
                    NVData.addEquipEntryById(equipEntry.characterId, equipEntry.idEquipDat, equipEntry.id, equipEntry);
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
                    capEntry.level = resultSet.getByte("lvl");
                    capEntry.caption = resultSet.getString("caption");
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `item`")) {
                while (resultSet.next()) {
                    ItemFightData.ItemFight iEntry = new ItemFightData.ItemFight();
                    iEntry.setName(resultSet.getString("name"));
                    iEntry.setBuyXu(resultSet.getInt("xu"));
                    iEntry.setBuyLuong(resultSet.getInt("luong"));
                    iEntry.setCarriedItemCount(resultSet.getByte("carried_item_count"));

                    ItemFightData.ITEM_FIGHTS.add(iEntry);
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
                    ItemClanData.ClanItemDetail item = new ItemClanData.ClanItemDetail();
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
