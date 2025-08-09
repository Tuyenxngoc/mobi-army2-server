package com.teamobi.mobiarmy2.dao;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.entity.SpecialItem;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SpecialItemDAO {
    private final HikariCPManager hikariCPManager;

    public SpecialItemDAO(HikariCPManager hikariCPManager) {
        this.hikariCPManager = hikariCPManager;
    }


    public void loadAll() {
        try (Connection connection = hikariCPManager.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `special_items`")) {
                Gson gson = GsonUtil.getInstance();
                SpecialItemManager.SPECIAL_ITEMS.clear();

                while (resultSet.next()) {
                    SpecialItem specialItem = new SpecialItem();
                    specialItem.setId(resultSet.getByte("special_item_id"));
                    specialItem.setName(resultSet.getString("name"));
                    specialItem.setDetail(resultSet.getString("detail"));
                    specialItem.setPriceXu(resultSet.getInt("price_xu"));
                    specialItem.setPriceLuong(resultSet.getInt("price_luong"));
                    specialItem.setPriceSellXu(resultSet.getInt("price_sell_xu"));
                    specialItem.setExpirationDays(resultSet.getShort("expiration_days"));
                    specialItem.setShowSelection(resultSet.getBoolean("show_selection"));
                    specialItem.setOnSale(resultSet.getBoolean("is_on_sale"));
                    specialItem.setAbility(gson.fromJson(resultSet.getString("ability"), short[].class));

                    //Phân loại item
                    byte specialItemType = resultSet.getByte("type");
                    switch (specialItemType) {
                        case 1 -> specialItem.setGem(true);
                        case 2 -> specialItem.setMaterial(true);
                        case 3 -> specialItem.setUsable(true);
                    }

                    SpecialItemManager.addSpecialItem(specialItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
