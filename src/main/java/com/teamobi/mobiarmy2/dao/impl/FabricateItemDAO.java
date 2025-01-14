package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IFabricateItemDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.FabricateItem;
import com.teamobi.mobiarmy2.model.SpecialItem;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import com.teamobi.mobiarmy2.server.FabricateItemManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class FabricateItemDAO implements IFabricateItemDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `fabricate_items`")) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    FabricateItem entry = new FabricateItem();
                    entry.setId(resultSet.getInt("fabricate_item_id"));
                    entry.setXuRequire(resultSet.getInt("xu_require"));
                    entry.setLuongRequire(resultSet.getInt("luong_require"));
                    entry.setRewardXu(resultSet.getInt("reward_xu"));
                    entry.setRewardLuong(resultSet.getInt("reward_luong"));
                    entry.setRewardCup(resultSet.getInt("reward_cup"));
                    entry.setRewardExp(resultSet.getInt("reward_exp"));
                    entry.setConfirmationMessage(resultSet.getString("confirmation_message"));
                    entry.setCompletionMessage(resultSet.getString("completion_message"));

                    SpecialItemChestJson[] jsonArray = gson.fromJson(resultSet.getString("item_require"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : jsonArray) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                        if (specialItem == null) {
                            continue;
                        }
                        entry.getItemRequire().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                    }

                    jsonArray = gson.fromJson(resultSet.getString("reward_item"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : jsonArray) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                        if (specialItem == null) {
                            continue;
                        }
                        entry.getRewardItem().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                    }

                    if (!entry.getItemRequire().isEmpty() && !entry.getRewardItem().isEmpty()) {
                        FabricateItemManager.FABRICATE_ITEMS.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
