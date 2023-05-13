package rgr.sshApp.controller;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
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
import rgr.sshApp.utils.FileInfo;
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        startInNewThread(()->{
            FileInfo selectedFile = localPanel.getSelectedFile();
            if (selectedFile!=null) {
                String selectedFileName = selectedFile.getFileName();
                String remoteDir = remotePanel.getCurrentDir();
                String localDir = localPanel.getCurrentDir();
                localPanel.transferFile(remoteDir, localDir, selectedFileName);
                Platform.runLater(() -> remotePanel.refresh());
            }
        });
    }

    public void downloadRemoteFile(ActionEvent actionEvent) {
        startInNewThread(()-> {
                    FileInfo selectedFile = remotePanel.getSelectedFile();
                    if (selectedFile != null) {
                        String selectedFileName = selectedFile.getFileName();
                        String remoteDir = remotePanel.getCurrentDir();
                        String localDir = localPanel.getCurrentDir();
                        remotePanel.transferFile(localDir, remoteDir, selectedFileName);
                        Platform.runLater(() -> localPanel.refresh());
                    }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sshSession = ModelData.getInstance().getSshSession();
        localFiles = new LocalFiles();
    }

    private void startInNewThread(Runnable action) {
        new Thread(action).start();
    }

    private void uploadFolder(Path localPath, String remotePath, SecureFileTransferChannel channel) {
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

    private void downloadFolder(String remotePath, Path localPath, SecureFileTransferChannel channel) {
        try {
            System.out.println("curLocalPath: " + localPath);
            System.out.println("creating localPath: " + remotePath);
            String localFolderName =  remotePath.substring(remotePath.lastIndexOf("/")+1);
            Path createdLocalFolder = localPath.resolve(Path.of(localFolderName));
            System.out.println("folderName: " + localFolderName);
            System.out.println("created local folder: " + createdLocalFolder);
            if (!Files.exists(createdLocalFolder)) Files.createDirectory(createdLocalFolder);
            System.out.println("Created dir path: " + createdLocalFolder);
            Vector<ChannelSftp.LsEntry> files = channel.listDirectory(remotePath);
            for (ChannelSftp.LsEntry file : files) {
                String fileName = file.getFilename();
                if (!fileName.equals(".") && !fileName.equals("..")) {
                    String remoteFilePath =  remotePath + "/" + fileName;
                    Path localFilePath = createdLocalFolder.resolve(fileName);
                    System.out.println("fileName: " + fileName);
                    if (file.getAttrs().isDir()) {
                        System.out.println("file = " + fileName + " is Dir. Creating new dir");
                        downloadFolder(remoteFilePath,createdLocalFolder,channel);
                    } else {
                        System.out.println("file = " + fileName + " is file. Downloading file");
                        channel.downloadFile(remoteFilePath,createdLocalFolder.toString());
                        System.out.println("Downloaded file with name: " + fileName + " to local path " + createdLocalFolder);
                    }
                }
            };
        } catch (IOException exc) {
            System.out.println("ManagerController.downloadFolder: recursive method/file listing error");
            exc.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void initPanels(WindowEvent windowEvent) {
    }
}
