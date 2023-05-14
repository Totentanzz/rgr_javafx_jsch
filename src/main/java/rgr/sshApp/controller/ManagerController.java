package rgr.sshApp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.utils.*;
import rgr.sshApp.web.SecureShellSession;

import java.net.URL;
import java.util.ResourceBundle;

public class ManagerController implements Initializable {

    @FXML
    private Stage managerStage;
    @FXML
    private Scene managerScene;
    @FXML
    private VBox managerBox;
    @FXML
    private MenuBar managerMenuBar;
    @FXML
    private Button refreshButton;
    @FXML
    private HBox panelBox;
    @FXML
    private LocalPanel localPanel;
    @FXML
    private RemotePanel remotePanel;
    @FXML
    private HBox buttonBox;
    @FXML
    private Button uploadButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button exitButton;

    private SecureShellSession sshSession;
    private LocalFiles localFiles;


    public void handleCloseRequest(WindowEvent windowEvent) {
        managerStage.close();
        sshSession.disconnect();
    }

    public void closeWindow(ActionEvent actionEvent) {
        handleCloseRequest(new WindowEvent(managerStage,WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void refreshTables(ActionEvent actionEvent) {
        localPanel.refresh();
        remotePanel.refresh();
    }

    public void uploadLocalFile(ActionEvent actionEvent) {
        localPanel.transfer(remotePanel.getCurrentDir());
    }

    public void downloadRemoteFile(ActionEvent actionEvent) {
        remotePanel.transfer(localPanel.getCurrentDir());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sshSession = ModelData.getInstance().getSshSession();
        localFiles = new LocalFiles();
    }
    private void startInNewThread(Runnable action) {
        new Thread(action).start();
    }

    public void initPanels(WindowEvent windowEvent) {
    }
}
