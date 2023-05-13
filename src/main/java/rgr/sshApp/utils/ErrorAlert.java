package rgr.sshApp.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

public class ErrorAlert extends Alert {

    private double xOffset, yOffset;

    public ErrorAlert(AlertType alertType) {
        super(alertType);
    }

    public ErrorAlert(String errorText, String windowTitle) {
        super(AlertType.NONE);
        this.setTitle(windowTitle);
        this.setContentText(errorText);
        this.getButtonTypes().addAll(ButtonType.OK);
        this.getDialogPane().setStyle("-fx-font-size: 16;-fx-border-color: #a04ac0;-fx-border-width: 5");
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.WINDOW_MODAL);
        this.initMoving();
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
