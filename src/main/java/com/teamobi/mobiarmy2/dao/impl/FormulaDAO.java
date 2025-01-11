package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IFormulaDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Formula;
import com.teamobi.mobiarmy2.model.SpecialItem;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import com.teamobi.mobiarmy2.model.SpecialItemChestJson;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.FormulaManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FormulaDAO implements IFormulaDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            FormulaManager.FORMULAS.clear();

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM formula_details fd INNER JOIN formulas f on fd.formula_id = f.formula_id ORDER BY f.material_id, fd.character_id, f.level")) {
                Gson gson = GsonUtil.getInstance();
                while (resultSet.next()) {
                    Formula entry = new Formula();
                    entry.setMaterial(SpecialItemManager.getSpecialItemById(resultSet.getByte("f.material_id")));
                    entry.setLevel(resultSet.getByte("f.level"));
                    entry.setLevelRequired(resultSet.getByte("f.level_required"));
                    entry.setEquipType(resultSet.getByte("f.equip_type"));
                    entry.setCharacterId(resultSet.getByte("fd.character_id"));
                    entry.setDetails(gson.fromJson(resultSet.getString("f.details"), String[].class));
                    entry.setAddPointsMax(gson.fromJson(resultSet.getString("f.add_points_max"), byte[].class));
                    entry.setAddPointsMin(gson.fromJson(resultSet.getString("f.add_points_min"), byte[].class));
                    entry.setAddPercentsMax(gson.fromJson(resultSet.getString("f.add_percents_max"), byte[].class));
                    entry.setAddPercentsMin(gson.fromJson(resultSet.getString("f.add_percents_min"), byte[].class));
                    entry.setRequiredEquip(EquipmentManager.getEquipment(resultSet.getShort("fd.required_equip")));
                    entry.setResultEquip(EquipmentManager.getEquipment(resultSet.getShort("fd.result_equip")));
                    SpecialItemChestJson[] json = gson.fromJson(resultSet.getString("fd.required_items"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson itemChestJson : json) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(itemChestJson.getId());
                        if (specialItem != null) {
                            entry.getRequiredItems().add(new SpecialItemChest(itemChestJson.getQuantity(), specialItem));
                        }
                    }

                    FormulaManager.addFormulaEntry(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
