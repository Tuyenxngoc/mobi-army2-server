package com.teamobi.mobiarmy2.dao.impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.dao.IFormulaDAO;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.Formula;
import com.teamobi.mobiarmy2.model.SpecialItem;
import com.teamobi.mobiarmy2.model.SpecialItemChest;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.FormulaManager;
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
public class FormulaDAO implements IFormulaDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM formula_details fd INNER JOIN formulas f on fd.formula_id = f.formula_id ORDER BY f.material_id, fd.character_id, f.level")) {

                Gson gson = GsonUtil.getInstance();
                FormulaManager.FORMULAS.clear();

                while (resultSet.next()) {
                    Formula formula = new Formula();
                    formula.setMaterial(SpecialItemManager.getSpecialItemById(resultSet.getByte("f.material_id")));
                    formula.setLevel(resultSet.getByte("f.level"));
                    formula.setLevelRequired(resultSet.getByte("f.level_required"));
                    formula.setEquipType(resultSet.getByte("f.equip_type"));
                    formula.setCharacterId(resultSet.getByte("fd.character_id"));
                    formula.setDetails(gson.fromJson(resultSet.getString("f.details"), String[].class));
                    formula.setAddPointsMax(gson.fromJson(resultSet.getString("f.add_points_max"), byte[].class));
                    formula.setAddPointsMin(gson.fromJson(resultSet.getString("f.add_points_min"), byte[].class));
                    formula.setAddPercentsMax(gson.fromJson(resultSet.getString("f.add_percents_max"), byte[].class));
                    formula.setAddPercentsMin(gson.fromJson(resultSet.getString("f.add_percents_min"), byte[].class));
                    formula.setRequiredEquip(EquipmentManager.getEquipment(formula.getCharacterId(), formula.getEquipType(), resultSet.getShort("fd.required_equip")));
                    formula.setResultEquip(EquipmentManager.getEquipment(formula.getCharacterId(), formula.getEquipType(), resultSet.getShort("fd.result_equip")));
                    SpecialItemChestJson[] json = gson.fromJson(resultSet.getString("fd.required_items"), SpecialItemChestJson[].class);
                    for (SpecialItemChestJson itemChestJson : json) {
                        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(itemChestJson.getId());
                        if (specialItem != null) {
                            formula.getRequiredItems().add(new SpecialItemChest(itemChestJson.getQuantity(), specialItem));
                        }
                    }

                    FormulaManager.addFormula(formula);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
