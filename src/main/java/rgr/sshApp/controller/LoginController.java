package rgr.sshApp.controller;

import com.jcraft.jsch.JSchException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import rgr.sshApp.SshApp;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.utils.CustomAlert;
import rgr.sshApp.web.SecureShellSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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


    public void dragWindow(MouseEvent mouseEvent) {
        loginStage.setX(mouseEvent.getScreenX() - xOffset);
        loginStage.setY(mouseEvent.getScreenY() - yOffset);
    }

    public void grabWindow(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }

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

    public void closeWindow(ActionEvent actionEvent) {
        loginStage.close();
    }

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
            System.out.println("LoginController.tryConnect: connection/parsing error");
            CustomAlert errorAlert = new CustomAlert("Invalid connection params","Error",ButtonType.OK);
            errorAlert.initOwner(loginStage);
            errorAlert.showAndWait();
        } finally {
            if (sshSession.isEstablished()) {
                System.out.println("CONNECTED SUCCESSFULLY");
                modelData.setSshSession(sshSession);
                //убрать стоку ниже одну
                sshSession.getCheckingChannel().changeDirectory("/workspace/firstContainer/");
                FXMLLoader fxmlLoader = new FXMLLoader(SshApp.class.getResource("view/managerView.fxml"));
                Stage stage = null;
                try {
                    stage = fxmlLoader.load();
                    stage.getIcons().add(new Image(new FileInputStream("src/main/resources/rgr/sshApp/images/files-and-folders.png")));
                    loginStage.close();
                    stage.show();
                } catch (IOException exc) {
                    System.out.println("LoginController.tryConnect: loading fxml error");
                    exc.printStackTrace();
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTextFields();
        setDefaultText();
        modelData = ModelData.getInstance();
        String ab1 = "C:\\Users\\Ilya\\IdeaProjects\\rgr_javafx_jsch\\src\\main\\resources\\rgr\\sshApp\\images\\arrow.png";
        String ab2 = "C:\\Users\\Ilya\\IdeaProjects\\rgr_javafx_jsch\\arrow.png";
        Path path1 = Path.of(ab1).toAbsolutePath().normalize();
        Path path2 = Path.of(ab2).toAbsolutePath().normalize();
        boolean as1 = java.nio.file.Files.exists(path1);
        boolean as2 = java.nio.file.Files.exists(path2);
        System.out.println(as1);
        System.out.println(as2);
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
        portField.setText("51323");
        usernameField.setText("root");
        hidePassField.setText("o8cZhtwH2tDUaBLf0uQ7");
    }
}