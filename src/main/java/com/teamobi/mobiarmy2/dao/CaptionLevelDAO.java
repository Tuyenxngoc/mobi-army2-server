package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.Caption;
import com.teamobi.mobiarmy2.server.CaptionManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CaptionLevelDAO {

    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT level, caption FROM `caption_levels`")) {

                CaptionManager.CAPTIONS.clear();

                while (resultSet.next()) {
                    Caption caption = new Caption();
                    caption.setLevel(resultSet.getByte("level"));
                    caption.setCaption(resultSet.getString("caption"));

                    CaptionManager.CAPTIONS.add(caption);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
