package com.teamobi.mobiarmy2.dao.impl;

import com.teamobi.mobiarmy2.dao.IPaymentDAO;
import com.teamobi.mobiarmy2.database.HikariCPManager;
import com.teamobi.mobiarmy2.model.Payment;
import com.teamobi.mobiarmy2.server.PaymentManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PaymentDAO implements IPaymentDAO {

    @Override
    public void loadAll() {
        try (Connection connection = HikariCPManager.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            PaymentManager.PAYMENT_MAP.clear();

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `payments`")) {
                while (resultSet.next()) {
                    Payment payment = new Payment();
                    payment.setId(resultSet.getString("payment_id"));
                    payment.setInfo(resultSet.getString("info"));
                    payment.setUrl(resultSet.getString("url"));
                    payment.setMssTo(resultSet.getString("mss_to"));
                    payment.setMssContent(resultSet.getString("mss_content"));

                    PaymentManager.PAYMENT_MAP.put(payment.getId(), payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
