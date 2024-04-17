package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.util.Until;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameDao implements IGameDao {

    @Override
    public void getAllMapData() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `map`")) {
                while (resultSet.next()) {
                    MapData.MapDataEntry mapDataEntry = new MapData.MapDataEntry();
                    mapDataEntry.id = (byte) (resultSet.getByte("id") - 1);
                    mapDataEntry.name = resultSet.getString("name");
                    mapDataEntry.file = resultSet.getString("file");
                    if (mapDataEntry.id == 27) {
                        mapDataEntry.data = new byte[0];
                    } else {
                        mapDataEntry.data = Until.getFile("res/map/" + mapDataEntry.file);
                    }
                    mapDataEntry.bg = resultSet.getShort("bg");
                    mapDataEntry.mapAddY = resultSet.getShort("mapAddY");
                    mapDataEntry.bullEffShower = resultSet.getShort("bullEffShower");
                    mapDataEntry.inWaterAddY = resultSet.getShort("inWaterAddY");
                    mapDataEntry.cl2AddY = resultSet.getShort("cl2AddY");
                    MapData.entries.add(mapDataEntry);
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
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `nhanvat`")) {
                while (resultSet.next()) {
                    NVData.NVEntry nvEntry = new NVData.NVEntry();
                    nvEntry.id = (byte) (resultSet.getByte("nhanvat_id") - 1);
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
                    equipEntry.idNV = resultSet.getByte("nv");
                    equipEntry.idEquipDat = resultSet.getByte("equipType");
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
                    equipEntry.bigImageCutX = new short[6];
                    equipEntry.bigImageCutY = new short[6];
                    equipEntry.bigImageSizeX = new byte[6];
                    equipEntry.bigImageSizeY = new byte[6];
                    equipEntry.bigImageAlignX = new byte[6];
                    equipEntry.bigImageAlignY = new byte[6];
                    equipEntry.arraySet = new short[5];
                    int l;
                    JSONArray jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigCutX"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageCutX[l] = ((Long) jArray3.get(l)).shortValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigCutY"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageCutY[l] = ((Long) jArray3.get(l)).shortValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigSizeX"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageSizeX[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigSizeY"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageSizeY[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigAlignX"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageAlignX[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("bigAlignY"));
                    for (l = 0; l < 6; l++) {
                        equipEntry.bigImageAlignY[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("arraySet"));
                    for (l = 0; l < 5; l++) {
                        equipEntry.arraySet[l] = ((Long) jArray3.get(l)).shortValue();
                    }
                    equipEntry.invAdd = new byte[5];
                    equipEntry.percenAdd = new byte[5];
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("addPN"));
                    for (l = 0; l < 5; l++) {
                        equipEntry.invAdd[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    jArray3 = (JSONArray) JSONValue.parse(resultSet.getString("addPN100"));
                    for (l = 0; l < 5; l++) {
                        equipEntry.percenAdd[l] = ((Long) jArray3.get(l)).byteValue();
                    }
                    NVData.addEquipEntryById(equipEntry.idNV, equipEntry.idEquipDat, equipEntry.id, equipEntry);
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
                    CaptionData.CaptionEntry capEntry = new CaptionData.CaptionEntry();
                    capEntry.level = resultSet.getInt("lvl");
                    capEntry.caption = resultSet.getString("caption");
                    CaptionData.entrys.add(capEntry);
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
                    ItemData.ItemEntry iEntry = new ItemData.ItemEntry();
                    iEntry.name = resultSet.getString("name");
                    iEntry.buyXu = resultSet.getInt("xu");
                    iEntry.buyLuong = resultSet.getInt("luong");
                    ItemData.entrys.add(iEntry);
                }
                System.out.println("Item readed size=" + ItemData.entrys.size());
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
                    ItemClanData.ItemClanEntry iEntry = new ItemClanData.ItemClanEntry();
                    iEntry.id = resultSet.getInt("id");
                    iEntry.level = resultSet.getInt("level");
                    iEntry.name = resultSet.getString("name");
                    iEntry.time = resultSet.getShort("time");
                    iEntry.onsole = resultSet.getByte("onsale");
                    iEntry.xu = resultSet.getInt("xu");
                    iEntry.luong = resultSet.getInt("luong");
                    ItemClanData.entrys.add(iEntry);
                    System.out.println("id " + iEntry.id + " level " + iEntry.level + " name " + iEntry.name);
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
                    JSONArray jarr = (JSONArray) JSONValue.parse(res.getString("ability"));
                    iEntry.ability = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        iEntry.ability[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    SpecialItemData.entrys.add(iEntry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void getAllFomular() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet res = statement.executeQuery("SELECT * FROM `fomular`;")) {
                while (res.next()) {
                    int materialId = res.getInt("idMaterial");
                    byte equipType = res.getByte("equipType");
                    JSONArray jarr = (JSONArray) JSONValue.parse(res.getString("equipId"));
                    short[] eqId = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        eqId[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("equipNeed"));
                    short[] eqNeedId = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        eqNeedId[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    FomularData.FomularEntry fE = new FomularData.FomularEntry();
                    fE.level = res.getByte("lv");
                    fE.levelRequire = res.getInt("lvRequire");
                    jarr = (JSONArray) JSONValue.parse(res.getString("addPNMin"));
                    fE.invAddMin = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        fE.invAddMin[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("addPNMax"));
                    fE.invAddMax = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        fE.invAddMax[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("addPP100Min"));
                    fE.percenAddMin = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        fE.percenAddMin[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("addPP100Max"));
                    fE.percenAddMax = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        fE.percenAddMax[i] = ((Long) jarr.get(i)).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("itemRequire"));
                    fE.itemNeed = new SpecialItemData.SpecialItemEntry[jarr.size()];
                    fE.itemNeedNum = new short[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        JSONObject jobj = (JSONObject) jarr.get(i);
                        fE.itemNeed[i] = SpecialItemData.getSpecialItemById(((Long) jobj.get("id")).intValue());
                        fE.itemNeedNum[i] = ((Long) jobj.get("num")).shortValue();
                    }
                    jarr = (JSONArray) JSONValue.parse(res.getString("detail"));
                    fE.detail = new String[jarr.size()];
                    for (int i = 0; i < jarr.size(); i++) {
                        fE.detail[i] = (String) jarr.get(i);
                    }
                    FomularData.addFomularEntry(materialId, equipType, eqId, eqNeedId, fE);
                }
                System.out.println("Fomular readed size=" + FomularData.entrys.size());

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
            try (ResultSet res = statement.executeQuery("SELECT * FROM `napthe`")) {
                while (res.next()) {
                    NapTienData.NapTienEntry nE = new NapTienData.NapTienEntry();
                    nE.id = res.getString("id");
                    nE.info = res.getString("info");
                    nE.url = res.getString("url");
                    nE.mssTo = res.getString("mssTo");
                    nE.mssContent = res.getString("mssContent");
                    NapTienData.entrys.add(nE);
                }
                System.out.println("Nap the readed size=" + NapTienData.entrys.size());

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
            try (ResultSet res = statement.executeQuery("SELECT * FROM `mission`")) {
                while (res.next()) {
                    MissionData.MissionEntry mE = new MissionData.MissionEntry();
                    int id = res.getInt("id");
                    byte idNeed = res.getByte("idneed");
                    mE.index = res.getInt("iddb");
                    mE.level = res.getByte("level");
                    mE.name = res.getString("name");
                    mE.require = res.getInt("require");
                    mE.reward = res.getString("reward");
                    mE.rewardXu = res.getInt("rewardXu");
                    mE.rewardLuong = res.getInt("rewardLuong");
                    mE.rewardXP = res.getInt("rewardXP");
                    mE.rewardCUP = res.getInt("rewardCUP");
                    MissionData.addMissionEntry(id, idNeed, mE);
                }
                System.out.println("Mission readed size=" + MissionData.entries.size());

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
