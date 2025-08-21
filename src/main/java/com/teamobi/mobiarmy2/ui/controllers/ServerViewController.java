package com.teamobi.mobiarmy2.ui.controllers;

import com.sun.management.OperatingSystemMXBean;
import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ServerViewController {
    @FXML
    public Tab serverInfoTab;
    @FXML
    public TextField searchUserField;
    @FXML
    private Label serverStatus;
    @FXML
    private Label cpuUsage;
    @FXML
    private Label memoryUsage;
    @FXML
    private Label diskSpace;
    @FXML
    private Label uptime;
    @FXML
    private Button maintainButton;

    private Timeline countdownTimeline;

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateServerInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        // CPU usage
        double cpuLoad = osBean.getCpuLoad() * 100;
        cpuUsage.setText(String.format("%.2f%%", cpuLoad));

        // Memory usage
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        memoryUsage.setText(String.format("%d MB / %d MB", usedMemory / (1024 * 1024), totalMemory / (1024 * 1024)));

        // Disk space
        long freeSpace = new java.io.File("/").getFreeSpace();
        long totalSpace = new java.io.File("/").getTotalSpace();
        diskSpace.setText(String.format("%d GB / %d GB", freeSpace / (1024 * 1024 * 1024), totalSpace / (1024 * 1024 * 1024)));

        // Uptime
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        uptime.setText(formatUptime(uptimeMillis));
    }

    private String formatUptime(long uptimeMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMillis);
        uptimeMillis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis);
        uptimeMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis);
        uptimeMillis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis);
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
    }

    private void startCountdown(int countdownTime) {
        maintainButton.setDisable(true);
        final int[] timeRemaining = {countdownTime};

        serverStatus.setText("Maintenance in " + timeRemaining[0] + " seconds...");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining[0]--;

            serverStatus.setText("Maintenance in " + timeRemaining[0] + " seconds...");

            if (timeRemaining[0] <= 0) {
                countdownTimeline.stop();
                enterMaintenanceMode();
            }
        }));

        countdownTimeline.setCycleCount(countdownTime);
        countdownTimeline.play();
    }

    private void enterMaintenanceMode() {
        maintainButton.setDisable(false);
        serverStatus.setText("Maintenance Mode");

        ApplicationContext.getInstance()
                .getBean(ServerManager.class)
                .setMaintenanceMode(true);
    }

    @FXML
    public void initialize() {
        serverInfoTab.setOnSelectionChanged(event -> {
            if (serverInfoTab.isSelected()) {
                updateServerInfo();
            }
        });
    }

    @FXML
    public void maintainServer() {
        TextInputDialog dialog = new TextInputDialog("180");
        dialog.setTitle("Maintenance Countdown");
        dialog.setHeaderText("Enter countdown time in seconds before maintenance:");
        dialog.setContentText("Countdown (seconds):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(countdownStr -> {
            try {
                int countdownTime = Integer.parseInt(countdownStr);

                if (countdownTime > 0) {
                    startCountdown(countdownTime);
                } else {
                    showError("Invalid time", "Please enter a positive integer for the countdown.");
                }
            } catch (NumberFormatException e) {
                showError("Invalid input", "Please enter a valid number for the countdown.");
            }
        });
    }

    @FXML
    public void searchUser() {
        String query = searchUserField.getText().toLowerCase().trim();
    }
}
