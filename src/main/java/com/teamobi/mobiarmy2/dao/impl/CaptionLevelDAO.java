package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.ICaptionLevelDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Caption;
import com.teamobi.mobiarmy2.server.CaptionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CaptionLevelDAO implements ICaptionLevelDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            CaptionManager.CAPTIONS.clear();

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

}
