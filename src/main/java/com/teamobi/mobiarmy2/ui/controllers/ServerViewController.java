package com.teamobi.mobiarmy2.ui.controllers;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.server.ServerListener;
import com.teamobi.mobiarmy2.server.ServerManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.util.List;

/**
 * @author tuyen
 */
public class ServerViewController implements ServerListener {

    @FXML
    private Label serverStatus;
    @FXML
    private TableView<User> playerTable;
    @FXML
    public TableColumn<User, Integer> playerIdColumn;
    @FXML
    public TableColumn<User, String> usernameColumn;
    @FXML
    public TableColumn<User, Integer> levelColumn;
    @FXML
    public TableColumn<User, String> statusColumn;
    @FXML
    public TableColumn<User, String> ipAddressColumn;
    @FXML
    private TextArea logArea;

    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        // Initialize the ObservableList to bind to the TableView
        userList = FXCollections.observableArrayList();

        // Set up the columns to show data from the User class
        playerIdColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getPlayerId()).asObject());
        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));
        levelColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCurrentLevel()).asObject());
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getState().name()));
        ipAddressColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSession().getIPAddress()));

        // Set the ObservableList into the TableView
        playerTable.setItems(userList);

        ServerManager.getInstance().addServerListener(this);
    }

    @Override
    public void onUsersUpdated(List<ISession> sessions) {
        List<User> updatedUsers = sessions.stream()
                .map(ISession::getUser)
                .filter(User::isLogged)
                .toList();
        userList.setAll(updatedUsers);
    }

    @FXML
    public void startServer() {
    }

    @FXML
    public void stopServer() {
    }

    @FXML
    public void restartServer() {
    }

    @FXML
    public void kickPlayer() {
    }

    @FXML
    public void saveConfig() {
    }

    @FXML
    public void clearLogs() {
        logArea.clear();
    }
}
