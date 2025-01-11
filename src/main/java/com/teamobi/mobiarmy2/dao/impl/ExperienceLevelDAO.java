package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IExperienceLevelDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.LevelXpRequired;
import com.teamobi.mobiarmy2.server.ClanXpManager;
import com.teamobi.mobiarmy2.server.PlayerXpManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExperienceLevelDAO implements IExperienceLevelDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            ClanXpManager.LEVEL_XP_REQUIRED_LIST.clear();
            PlayerXpManager.LEVEL_XP_REQUIRED_LIST.clear();

            try (ResultSet resultSet = statement.executeQuery("SELECT exp_user, exp_clan, level FROM `experience_levels` ORDER BY level")) {
                int previousPlayerXp = 0;
                int previousClanXp = 0;
                boolean reachedMaxPlayerLevel = false;
                boolean reachedMaxClanLevel = false;

                while (resultSet.next()) {
                    Integer playerXp = resultSet.getObject("exp_user", Integer.class);
                    Integer clanXp = resultSet.getObject("exp_clan", Integer.class);
                    short level = resultSet.getShort("level");

                    if (!reachedMaxPlayerLevel) {
                        if (playerXp == null) {
                            reachedMaxPlayerLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho player
                            if (playerXp < previousPlayerXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho player (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", playerXp, previousPlayerXp));
                            }

                            // Tạo bản ghi cho player
                            LevelXpRequired playerXpRequired = new LevelXpRequired(level, playerXp);
                            PlayerXpManager.LEVEL_XP_REQUIRED_LIST.add(playerXpRequired);

                            previousPlayerXp = playerXp;
                        }
                    }

                    if (!reachedMaxClanLevel) {
                        if (clanXp == null) {
                            reachedMaxClanLevel = true;
                        } else {
                            // Kiểm tra tính hợp lệ của XP cho clan
                            if (clanXp < previousClanXp) {
                                throw new SQLException(String.format("XP của cấp độ tiếp theo cho clan (%d) nhỏ hơn XP của cấp độ trước đó (%d)!", clanXp, previousClanXp));
                            }

                            // Tạo bản ghi cho clan
                            LevelXpRequired clanXpRequired = new LevelXpRequired(level, clanXp);
                            ClanXpManager.LEVEL_XP_REQUIRED_LIST.add(clanXpRequired);

                            previousClanXp = clanXp;
                        }
                    }

                    if (reachedMaxPlayerLevel && reachedMaxClanLevel) {
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
