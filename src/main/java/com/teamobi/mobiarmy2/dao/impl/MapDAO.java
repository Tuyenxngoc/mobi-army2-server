package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.dao.IMapDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.ArmyMap;
import com.teamobi.mobiarmy2.server.MapManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class MapDAO implements IMapDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            MapManager.ARMY_MAPS.clear();

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

                    MapManager.addMap(armyMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
