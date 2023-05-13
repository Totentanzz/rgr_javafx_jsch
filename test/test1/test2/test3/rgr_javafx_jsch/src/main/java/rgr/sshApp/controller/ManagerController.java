package rgr.sshApp.controller;

import javafx.application.Platform;
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
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        startInNewThread(()->{
            String selectedFileName = localPanel.getSelectedFile().getFileName();
            Path localFilePath = Path.of(localPanel.getCurrentDir()).toAbsolutePath().normalize()
                    .resolve(Path.of(selectedFileName));
            String remoteDir = remotePanel.getCurrentDir();
            SecureFileTransferChannel newSftpChannel = new SecureFileTransferChannel(sshSession.getSession());
            newSftpChannel.connect();
            System.out.println("New channel is connected = " + newSftpChannel.isConnnected());
            System.out.println("Uploading file = " + localFilePath);
            System.out.println("Uploading to the dir = " + remoteDir);
            if (Files.isDirectory(localFilePath) && !selectedFileName.equals("..")) {
                uploadFolder(localFilePath,remoteDir,newSftpChannel);
            } else {
                newSftpChannel.uploadFile(localFilePath.toString(),remoteDir);
            }
            Platform.runLater(()->remotePanel.refresh());
            System.out.println("Upload finished");
            newSftpChannel.disconnect();
            System.out.println("New channel is disconnected = " + !newSftpChannel.isConnnected());
        });
    }

    public void downloadRemoteFile(ActionEvent actionEvent) {
        FileInfo selectedFile = remotePanel.getSelectedFile();
        String selectedFileName = selectedFile.getFileName();
        String remoteFilePath = remotePanel.getCurrentDir() + "/" + selectedFileName;
        String currentDir = localPanel.getCurrentDir();
        SecureFileTransferChannel newSftpChannel = new SecureFileTransferChannel(sshSession.getSession());
        newSftpChannel.connect();
        System.out.println("New channel is connected = " + newSftpChannel.isConnnected());
        System.out.println("Downloading file = " + remoteFilePath);
        System.out.println("Downloading to the dir = " + currentDir);
        if (selectedFile.getFileSize()==-1 && !selectedFileName.equals("..")) {

        } else if (!selectedFileName.equals("..")) {
            newSftpChannel.downloadFile(remoteFilePath,currentDir);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sshSession = ModelData.getInstance().getSshSession();
    }

    private void startInNewThread(Runnable action) {
        new Thread(action).start();
    }

    private void uploadFolder(Path localPath, String remotePath,SecureFileTransferChannel channel) {
        try {
            String remoteFolderName = localPath.getFileName().toString();
            String createdRemoteFolder = remotePath + "/" + remoteFolderName;
            channel.makeDir(remotePath,remoteFolderName);
            System.out.println("Created dir path: " + createdRemoteFolder);
            LinkedList<Path> files = Files.list(localPath).collect(Collectors.toCollection(LinkedList::new));
            for (Path file : files) {
                Path localFilePath =  localPath.resolve(file);
                String fileName = file.getFileName().toString();
                System.out.println("fileName: " + fileName);
                if (Files.isDirectory(localFilePath)) {
                    System.out.println("file = " + fileName + " is Dir. Creating new dir");
                    uploadFolder(localFilePath,createdRemoteFolder,channel);
                } else {
                    System.out.println("file = " + fileName + " is file. Uploading file");
                    channel.uploadFile(localFilePath.toString(),createdRemoteFolder);
                    System.out.println("Uploaded file with name: " + fileName + " to remote path " + createdRemoteFolder);
                }
            };
        } catch (IOException exc) {
            System.out.println("ManagerController.uploadFolder: recursive method/file listing error");
            exc.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void initPanels(WindowEvent windowEvent) {
    }
}
