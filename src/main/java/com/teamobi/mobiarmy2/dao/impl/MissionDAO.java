package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IMissionDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Mission;
import com.teamobi.mobiarmy2.server.MissionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class MissionDAO implements IMissionDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `missions` ORDER BY mission_type, level")) {
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

                    MissionManager.addMission(mission);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
