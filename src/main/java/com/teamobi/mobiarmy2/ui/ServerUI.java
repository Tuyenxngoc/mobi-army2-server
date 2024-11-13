package com.teamobi.mobiarmy2.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

/**
 * @author tuyen
 */
public class ServerUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/ServerView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/ui/css/styles.css")).toExternalForm());
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));
        primaryStage.setTitle("Server Manager UI");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Do you want to close this window?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
            }
        });
        primaryStage.show();
    }

    public static void launchUI(String[] args) {
        launch(args);
    }
}
