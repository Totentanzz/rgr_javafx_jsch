package rgr.sshApp.controller;

import com.jcraft.jsch.JSchException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import rgr.sshApp.SshApp;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.utils.CustomAlert;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private StackPane passStack;
    @FXML
    private Stage loginStage;
    @FXML
    private Scene loginScene;
    @FXML
    private TextField portField;
    @FXML
    private TextField ipField;
    @FXML
    private PasswordField hidePassField;
    @FXML
    private TextField showPassField;
    @FXML
    private TextField usernameField;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button connectButton;
    @FXML
    private Button exitButton;

    private ModelData modelData;
    private double xOffset, yOffset;

    @FXML
    public void dragWindow(MouseEvent mouseEvent) {
        loginStage.setX(mouseEvent.getScreenX() - xOffset);
        loginStage.setY(mouseEvent.getScreenY() - yOffset);
    }

    @FXML
    public void grabWindow(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }

    @FXML
    public void showPassword(ActionEvent actionEvent) {
        if (passStack.getChildren().indexOf(hidePassField)==passStack.getChildren().size()-1) {
            showPassField.toFront();
            showPassField.setVisible(true);
            hidePassField.setVisible(false);
        } else {
            hidePassField.toFront();
            hidePassField.setVisible(true);
            showPassField.setVisible(false);
        }
    }

    @FXML
    public void closeWindow(ActionEvent actionEvent) {
        loginStage.close();
    }

    @FXML
    public void tryConnect(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = hidePassField.getText();
        String ip = ipField.getText();
        SecureShellSession sshSession = null;
        try {
            int port = Integer.parseInt(portField.getText());
            sshSession = new SecureShellSession(username,password,ip,port);
            sshSession.connect();
        } catch (JSchException | NumberFormatException exc) {
            String message = "Invalid connection params";
            CustomAlert errorAlert = new CustomAlert(message,"Error",ButtonType.OK);
            errorAlert.initOwner(loginStage);
            errorAlert.showAndWait();
        } finally {
            if (sshSession!=null && sshSession.isEstablished()) {
                modelData.setSshSession(sshSession);
                loginStage.close();
                ManagerController.loadNewWindow();
            }
        }
    }

    public static void loadNewWindow() {
        FXMLLoader fxmlLoader = new FXMLLoader(SshApp.class.getResource("view/loginView.fxml"));
        try {
            Stage loginStage = fxmlLoader.load();
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.show();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTextFields();
        setDefaultText();
        modelData = ModelData.getInstance();
    }

    private void initTextFields() {
        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (!newText.contains(" ")) {
                return change;
            } else {
                return null;
            }
        }));

        ipField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String octetPattern = "(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
            String regex = new StringBuilder().append("(").append(octetPattern).append("(\\.|$)")
                    .append(")").append("{0,3}").append(octetPattern).append("?").toString();
            if (newText.matches(regex)) {
                return change;
            } else {
                return null;
            }
        }));

        portField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String regex = "\\d{0,5}";
            if (newText.matches(regex)){
                return change;
            } else {
                return null;
            }
        }));

        hidePassField.textProperty().bindBidirectional(showPassField.textProperty());
        hidePassField.toFront();
    }

    private void setDefaultText() {
        ipField.setText("3.74.216.87");
        portField.setText("57066");
        usernameField.setText("root");
        hidePassField.setText("Zz5uyaHiNLzlhNm5Z1");
    }
}