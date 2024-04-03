package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IGameDao;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameDao implements IGameDao {

    @Override
    public void getAllMapData() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM map")) {
                while (resultSet.next()) {
                    MapData.MapDataEntry mapDataEntry = new MapData.MapDataEntry();
                    mapDataEntry.id = (byte) (resultSet.getByte("id") - 1);
                    mapDataEntry.name = resultSet.getString("name");
                    mapDataEntry.file = resultSet.getString("file");
                    if (mapDataEntry.id == 27) {
                        mapDataEntry.data = new byte[0];
                    } else {
                        mapDataEntry.data = Utils.getFile("res/map/" + mapDataEntry.file);
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

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `nhanvat`;")) {
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
}
