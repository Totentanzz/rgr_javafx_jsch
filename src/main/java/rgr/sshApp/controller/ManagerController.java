package rgr.sshApp.controller;

import com.jcraft.jsch.JSchException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import rgr.sshApp.SshApp;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.utils.CustomAlert;
import rgr.sshApp.utils.files.panels.FilePanel;
import rgr.sshApp.utils.files.panels.LocalPanel;
import rgr.sshApp.utils.files.panels.RemotePanel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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
    private Button exitButton;
    @FXML
    private MenuItem reconnectButton;
    @FXML
    private MenuItem disconnectButton;

    private SecureShellSession sshSession;

    @FXML
    public void handleCloseRequest(WindowEvent windowEvent) {
        managerStage.close();
        sshSession.disconnect();
    }

    @FXML
    public void closeWindow(ActionEvent actionEvent) {
        handleCloseRequest(new WindowEvent(managerStage,WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    public void refreshTables(ActionEvent actionEvent) {
        localPanel.refresh();
        remotePanel.refresh();
    }

    @FXML
    public void uploadLocalFile(ActionEvent actionEvent) {
        startInNewThread(()->localPanel.transfer(remotePanel.getCurrentDir()));
        disableButton(uploadButton,1);
    }

    @FXML
    public void downloadRemoteFile(ActionEvent actionEvent) {
        startInNewThread(()-> remotePanel.transfer(localPanel.getCurrentDir()));
        disableButton(downloadButton,1);
    }

    @FXML
    public void exitToLogin(ActionEvent actionEvent) {
        handleCloseRequest(new WindowEvent(managerStage,WindowEvent.WINDOW_CLOSE_REQUEST));
        LoginController.loadNewWindow();
    }

    @FXML
    public void reconnectSsh(ActionEvent actionEvent) {
        startInNewThread(()->{
            try {
                sshSession.disconnect();
                sshSession.connect();
                remotePanel.setChannels(sshSession.getCheckingChannel(),sshSession.getFileListChannel());
            } catch (JSchException exc) {
                String message = "Connection error. Please, check your SSH server. If this message repeats, restart the app";
                Platform.runLater(()->{
                    CustomAlert alert = new CustomAlert(message,"Connection error",ButtonType.OK);
                    alert.showAndWait();
                });
            }
        });
    }

    @FXML
    public void disconnectSsh(ActionEvent actionEvent) {
        sshSession.disconnect();
    }

    @FXML
    public void initPanels(WindowEvent windowEvent) {
    }

    public static void loadNewWindow() {
        FXMLLoader fxmlLoader = new FXMLLoader(SshApp.class.getResource("view/managerView.fxml"));
        try {
            Stage managerStage = fxmlLoader.load();
            managerStage.show();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sshSession = ModelData.getInstance().getSshSession();
    }

    private void startInNewThread(Runnable action) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                action.run();
                return null;
            }
        };
        task.setOnSucceeded(event->{
            System.out.println("SUCCESS COMPLETED TASK IN NEW THREAD");
            Platform.runLater(()->{
                if (managerStage.isShowing() && sshSession.isEstablished()) {
                    localPanel.refresh();
                    remotePanel.refresh();
                }
            });
        });
        new Thread(task).start();
    }

    private void disableButton(Button button,double seconds) {
        button.setDisable(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                button.setDisable(false);
                timer.cancel();
            };
        }, (long) (seconds*1000));
    }

}
