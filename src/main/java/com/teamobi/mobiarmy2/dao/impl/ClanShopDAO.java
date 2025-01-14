package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IClanShopDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.ClanItemShop;
import com.teamobi.mobiarmy2.server.ClanItemManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class ClanShopDAO implements IClanShopDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT clan_shop_id, level, name, time, on_sale, xu, luong FROM `clan_shops`")) {

                ClanItemManager.CLAN_ITEM_MAP.clear();

                while (resultSet.next()) {
                    ClanItemShop item = new ClanItemShop();
                    item.setId(resultSet.getByte("clan_shop_id"));
                    item.setLevel(resultSet.getByte("level"));
                    item.setName(resultSet.getString("name"));
                    item.setTime(resultSet.getByte("time"));
                    item.setOnSale(resultSet.getByte("on_sale"));
                    item.setXu(resultSet.getInt("xu"));
                    item.setLuong(resultSet.getInt("luong"));

                    ClanItemManager.addClanItemShop(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
