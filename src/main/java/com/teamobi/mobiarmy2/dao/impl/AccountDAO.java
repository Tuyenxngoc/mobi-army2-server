package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IAccountDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.dto.AccountDTO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO implements IAccountDAO {

    @Override
    public AccountDTO findByUsernameAndPassword(String username, String password) {
        try (Connection connection = HikariCPManager.getInstance().getConnection()) {
            String userQuery = "SELECT `user_id`, `password`, `is_enabled`, `is_locked` FROM users WHERE username = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, username);
                try (ResultSet userResultSet = userStatement.executeQuery()) {
                    if (userResultSet.next()) {
                        String hashedPassword = userResultSet.getString("password");
                        if (!BCrypt.checkpw(password, hashedPassword)) {
                            return null;
                        }
                        AccountDTO accountDTO = new AccountDTO();
                        accountDTO.setAccountId(userResultSet.getString("user_id"));
                        accountDTO.setLock(userResultSet.getBoolean("is_locked"));
                        accountDTO.setActive(userResultSet.getBoolean("is_enabled"));
                        return accountDTO;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean existsByAccountIdAndPassword(String accountId, String password) {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM users WHERE user_id = ?")) {
            statement.setString(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void changePassword(String accountId, String newPass) {
        String hashedPassword = BCrypt.hashpw(newPass, BCrypt.gensalt());
        // language=SQL
        String sql = "UPDATE `users` SET `password` = ? WHERE user_id = ?";
        HikariCPManager.getInstance().update(sql, hashedPassword, accountId);
    }

}
