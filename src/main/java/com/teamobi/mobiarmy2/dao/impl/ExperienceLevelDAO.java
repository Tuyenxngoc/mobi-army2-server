package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IExperienceLevelDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.LevelXpRequired;
import com.teamobi.mobiarmy2.server.ClanXpManager;
import com.teamobi.mobiarmy2.server.UserXpManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class ExperienceLevelDAO implements IExperienceLevelDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT exp_user, exp_clan, level FROM `experience_levels` ORDER BY level")) {

                ClanXpManager.LEVEL_XP_REQUIRED_LIST.clear();
                UserXpManager.LEVEL_XP_REQUIRED_LIST.clear();

                int previousUserXp = 0;
                int previousClanXp = 0;
                boolean reachedMaxUserLevel = false;
                boolean reachedMaxClanLevel = false;

                while (resultSet.next()) {
                    Integer expUser = resultSet.getObject("exp_user", Integer.class);
                    Integer expClan = resultSet.getObject("exp_clan", Integer.class);
                    short level = resultSet.getShort("level");

                    if (!reachedMaxUserLevel) {
                        if (expUser == null) {
                            reachedMaxUserLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho player
                            if (expUser < previousUserXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho player (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", expUser, previousUserXp));
                            }

                            // Tạo bản ghi cho player
                            LevelXpRequired playerXpRequired = new LevelXpRequired(level, expUser);
                            UserXpManager.LEVEL_XP_REQUIRED_LIST.add(playerXpRequired);

                            previousUserXp = expUser;
                        }
                    }

                    if (!reachedMaxClanLevel) {
                        if (expClan == null) {
                            reachedMaxClanLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho clan
                            if (expClan < previousClanXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho clan (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", expClan, previousClanXp));
                            }

                            // Tạo bản ghi cho clan
                            LevelXpRequired clanXpRequired = new LevelXpRequired(level, expClan);
                            ClanXpManager.LEVEL_XP_REQUIRED_LIST.add(clanXpRequired);

                            previousClanXp = expClan;
                        }
                    }

                    if (reachedMaxUserLevel && reachedMaxClanLevel) {
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
