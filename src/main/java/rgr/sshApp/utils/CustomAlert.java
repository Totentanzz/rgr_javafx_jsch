package rgr.sshApp.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import rgr.sshApp.SshApp;
import rgr.sshApp.utils.files.panels.FilePanel;

import java.util.Objects;

public class CustomAlert extends Alert {

    private double xOffset, yOffset;

    public CustomAlert(AlertType alertType) {
        super(alertType);
    }

    public CustomAlert(String errorText, String windowTitle,ButtonType... buttons) {
        super(AlertType.NONE);
        this.setTitle(windowTitle);
        this.setContentText(errorText);
        this.getButtonTypes().addAll(buttons);
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.setPrefWidth(450);
        this.getDialogPane().setStyle("-fx-font-size: 16;-fx-border-color: #a04ac0;-fx-border-width: 5");
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.WINDOW_MODAL);
        this.initMoving();
    }

    public CustomAlert(FilePanel filePanel) {
        super(AlertType.NONE);
        this.setTitle("Directory chooser");
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContent(filePanel);
        dialogPane.setPrefWidth(590);
        dialogPane.setPrefHeight(650);
        this.getButtonTypes().add(ButtonType.APPLY);
//        this.initModality(Modality.APPLICATION_MODAL);
        this.setResizable(true);
        Stage panelStage = (Stage) this.getDialogPane().getScene().getWindow();
        panelStage.getIcons().add(new Image(Objects.requireNonNull(SshApp.class.getResourceAsStream("images/files-and-folders.png"))));
    }

    private void initMoving() {
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.setOnMouseDragged(mouseEvent -> {
            this.setX(mouseEvent.getScreenX() - xOffset);
            this.setY(mouseEvent.getScreenY() - yOffset);
        });
        dialogPane.setOnMousePressed(mouseEvent -> {
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
        });
    }

}
