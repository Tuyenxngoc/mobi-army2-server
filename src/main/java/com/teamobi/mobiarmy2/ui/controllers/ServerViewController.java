package com.teamobi.mobiarmy2.ui.controllers;

import com.sun.management.OperatingSystemMXBean;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.Message;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
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
        long freeSpace = new File("/").getFreeSpace();
        long totalSpace = new File("/").getTotalSpace();
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

        // Bật chế độ bảo trì trên server
//        ApplicationContext.getInstance()
//                .getBean(ServerManager.class)
//                .setMaintenanceMode(true);

        final int[] timeRemaining = {countdownTime};

        serverStatus.setText("Maintenance in " + timeRemaining[0] + " seconds...");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining[0]--;

            serverStatus.setText("Maintenance in " + timeRemaining[0] + " seconds...");

            // Cứ mỗi 60 giây hoặc khi còn < 30 giây thì nhắc nhở
            if (timeRemaining[0] > 0 && (timeRemaining[0] % 60 == 0 || timeRemaining[0] == 30 || timeRemaining[0] == 10)) {
                try {
                    Message ms = new Message(Cmd.SERVER_INFO);
                    DataOutputStream ds = ms.writer();
                    ds.writeUTF("Server sẽ bảo trì sau " + timeRemaining[0] + " giây, vui lòng thoát game để tránh mất dữ liệu.");
                    ds.flush();

//                    ApplicationContext.getInstance()
//                            .getBean(MessageSender.class)
//                            .broadcast(ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (timeRemaining[0] <= 0) {
                countdownTimeline.stop();
                enterMaintenanceMode();
            }
        }));

        countdownTimeline.setCycleCount(countdownTime);
        countdownTimeline.play();
    }

    private void enterMaintenanceMode() {
        try {
            Platform.exit(); // thoát JavaFX Application Thread
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0); // thoát hẳn JVM
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
                    int minSeconds = 180;

                    if (countdownTime < minSeconds) {
                        Alert warning = new Alert(Alert.AlertType.WARNING);
                        warning.setTitle("Warning: Low Countdown Time");
                        warning.setHeaderText(null);
                        warning.setContentText("Bạn chỉ đặt " + countdownTime + " giây, ít hơn mức khuyến nghị (" + minSeconds + " giây).\n"
                                + "Người chơi có thể không kịp thoát trận. Bạn có muốn tiếp tục không?");
                        ButtonType yesButton = new ButtonType("Tiếp tục", ButtonBar.ButtonData.YES);
                        ButtonType noButton = new ButtonType("Hủy", ButtonBar.ButtonData.NO);
                        warning.getButtonTypes().setAll(yesButton, noButton);

                        Optional<ButtonType> resultWarn = warning.showAndWait();
                        if (resultWarn.isPresent() && resultWarn.get() == yesButton) {
                            startCountdown(countdownTime);
                        }
                    } else {
                        startCountdown(countdownTime);
                    }
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
