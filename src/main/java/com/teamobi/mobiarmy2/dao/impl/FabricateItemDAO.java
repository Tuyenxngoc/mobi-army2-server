package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IFabricateItemDAO;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.FabricateItem;
import com.teamobi.mobiarmy2.model.SpecialItem;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import com.teamobi.mobiarmy2.server.FabricateItemManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;
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
                FabricateItemManager.FABRICATE_ITEMS.clear();

                while (resultSet.next()) {
                    FabricateItem fabricateItem = new FabricateItem();
                    fabricateItem.setId(resultSet.getInt("fabricate_item_id"));
                    fabricateItem.setXuRequire(resultSet.getInt("xu_require"));
                    fabricateItem.setLuongRequire(resultSet.getInt("luong_require"));
                    fabricateItem.setRewardXu(resultSet.getInt("reward_xu"));
                    fabricateItem.setRewardLuong(resultSet.getInt("reward_luong"));
                    fabricateItem.setRewardCup(resultSet.getInt("reward_cup"));
                    fabricateItem.setRewardExp(resultSet.getInt("reward_exp"));
                    fabricateItem.setConfirmationMessage(resultSet.getString("confirmation_message"));
                    fabricateItem.setCompletionMessage(resultSet.getString("completion_message"));

                    SpecialItemChestJson[] itemRequires = gson.fromJson(resultSet.getString("item_require"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson specialItemChestJson : itemRequires) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                        if (specialItem == null) {
                            continue;
                        }
                        fabricateItem.getItemRequire().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                    }

                    String rewardItemJson = resultSet.getString("reward_item");
                    if (rewardItemJson != null) {
                        SpecialItemChestJson[] rewardItems = gson.fromJson(resultSet.getString("reward_item"), SpecialItemChestJson[].class);
                        for (SpecialItemChestJson specialItemChestJson : rewardItems) {
                            SpecialItem specialItem = SpecialItemManager.getSpecialItemById(specialItemChestJson.getId());
                            if (specialItem == null) {
                                continue;
                            }
                            fabricateItem.getRewardItem().add(new SpecialItemChest(specialItemChestJson.getQuantity(), specialItem));
                        }
                    }

                    FabricateItemManager.FABRICATE_ITEMS.add(fabricateItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
