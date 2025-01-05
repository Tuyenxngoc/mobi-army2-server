package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.ICharacterDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Character;
import com.teamobi.mobiarmy2.server.CharacterManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tuyen
 */
public class CharacterDAO implements ICharacterDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `characters`")) {
                while (resultSet.next()) {
                    Character character = new Character();
                    character.setId(resultSet.getByte("character_id"));
                    character.setName(resultSet.getString("name"));
                    character.setPriceXu(resultSet.getInt("xu"));
                    character.setPriceLuong(resultSet.getInt("luong"));
                    character.setWindResistance(resultSet.getByte("wind_resistance"));
                    character.setMinAngle(resultSet.getByte("min_angle"));
                    character.setDamage(resultSet.getShort("damage"));
                    character.setBulletDamage(resultSet.getByte("bullet_damage"));
                    character.setBulletCount(resultSet.getByte("bullet_count"));

                    CharacterManager.CHARACTERS.add(character);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
