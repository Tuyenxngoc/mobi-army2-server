package com.teamobi.mobiarmy2.ui.controllers;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.server.ServerManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ServerViewController {

    @FXML
    private TableColumn playerIdColumn;
    @FXML
    private TableColumn usernameColumn;
    @FXML
    private TableColumn levelColumn;
    @FXML
    private TableColumn statusColumn;
    @FXML
    private Label serverStatus;
    @FXML
    private TableView<User> playerTable;
    @FXML
    private TextField maxPlayersField;
    @FXML
    private TextField serverPortField;
    @FXML
    private TextArea logArea;

    private ServerManager serverManager = ServerManager.getInstance();

    @FXML
    public void initialize() {
        updateServerStatus();
        updatePlayerTable();
    }

    private void updateServerStatus() {
        if (serverManager != null) {
            String status = serverManager.isStart() ? "Running" : "Stopped";
            serverStatus.setText(status);
        }
    }

    private void updatePlayerTable() {
        playerTable.getItems().setAll(serverManager.getUsers());
    }

    public void startServer() {
        serverStatus.setText("Running");
        logArea.appendText("Server started...\n");
    }

    public void stopServer() {
        serverStatus.setText("Stopped");
        logArea.appendText("Server stopped...\n");
    }

    public void restartServer() {
        stopServer();
        startServer();
        logArea.appendText("Server restarted...\n");
    }

    public void kickPlayer() {
        // Logic kick player here
        logArea.appendText("Player kicked from server.\n");
    }

    public void saveConfig() {
        String maxPlayers = maxPlayersField.getText();
        String serverPort = serverPortField.getText();
        logArea.appendText("Configuration saved: Max Players = " + maxPlayers + ", Port = " + serverPort + "\n");
    }

    public void clearLogs() {
        logArea.clear();
    }
}
