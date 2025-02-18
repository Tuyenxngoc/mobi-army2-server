package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IFightItemDAO;
import com.teamobi.mobiarmy2.model.FightItem;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class FightItemDAO implements IFightItemDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT name, xu, luong, carried_item_count FROM `fight_items` ORDER BY fight_item_id")) {

                FightItemManager.FIGHT_ITEMS.clear();

                while (resultSet.next()) {
                    FightItem fightItem = new FightItem();
                    fightItem.setName(resultSet.getString("name"));
                    fightItem.setBuyXu(resultSet.getShort("xu"));
                    fightItem.setBuyLuong(resultSet.getShort("luong"));
                    fightItem.setCarriedItemCount(resultSet.getByte("carried_item_count"));

                    FightItemManager.FIGHT_ITEMS.add(fightItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
