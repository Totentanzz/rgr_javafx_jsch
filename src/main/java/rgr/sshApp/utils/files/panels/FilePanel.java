package rgr.sshApp.utils.files.panels;

import com.jcraft.jsch.JSchException;

import com.jcraft.jsch.SftpException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import rgr.sshApp.SshApp;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.utils.CustomAlert;
import rgr.sshApp.utils.files.FileInfo;
import rgr.sshApp.utils.files.handlers.FilesHandler;
import rgr.sshApp.web.SecureShellSession;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class FilePanel extends VBox implements Initializable {

    @FXML
    private ComboBox<java.lang.String> diskComboBox;
    @FXML
    private TextField pathField;
    @FXML
    private Button backButton;
    @FXML
    private Button rootButton;
    @FXML
    private HBox controlsBox;
    @FXML
    private TableView<FileInfo> fileTable;
    @FXML
    private VBox panelBox;

    protected FilesHandler fileHandler;

    public FilePanel() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(SshApp.class.getResource("view/panelView.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    @FXML
    public void moveToFileSystemDir(ActionEvent actionEvent) {
        String diskPath = diskComboBox.getSelectionModel().getSelectedItem();
        updateTable(diskPath);
    }

    @FXML
    public void moveToParentDir(ActionEvent actionEvent) {
        String curDir = pathField.getText();
        String parentDir = fileHandler.getParentDirectory(curDir);
        if (parentDir!=null) {
            updateTable(parentDir);
        }
    }
    @FXML
    public void moveToSelectedDir(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount()==2) {
            FileInfo selectedFile = fileTable.getSelectionModel().getSelectedItem();
            String currentPath = pathField.getText();
            if (selectedFile != null && selectedFile.getFileName().equals("..")) {
                backButton.fire();
            }
            else if (selectedFile != null && selectedFile.getFileSize() == -1) {
               String newDir = fileHandler.getResolvedDirectory(currentPath, selectedFile.getFileName());
               updateTable(newDir);
            }
        }
    }

    public FileInfo getSelectedFile() {
        return fileTable.getSelectionModel().getSelectedItem();
    }

    public String getCurrentDir() {
        return pathField.getText();
    }

    public TableView<FileInfo> getFileTable() {
        return fileTable;
    }

    public void updateTable(String path) {
        String curPath = pathField.getText();
        String message = null;
        CustomAlert errorAlert = null;
        try {
            pathField.setText(path);
            fileTable.getItems().clear();
            fileTable.getItems().addAll(fileHandler.getFileList(path));
            fileTable.sort();
        } catch (NullPointerException | SftpException exc) {
            message = "Getting list of files/connection error. Please, make sure that selected " +
                    "folder is existing and you are connected by SSH";
        } catch (IOException exc) {
            updateTable(curPath);
            message = "Entering to the folder error. Please, make sure that selected folder " +
                      "is existing and you are not entering to the private system folder";
        } finally {
            if (message!=null) {
                ObservableList<Window> windowList = Window.getWindows().filtered(window-> window instanceof Stage);
                errorAlert = new CustomAlert(message,"Error",ButtonType.OK);
                errorAlert.initOwner(windowList.get(0));
                errorAlert.showAndWait();
            }
        }
    }

    public void refresh() {
        updateTable(pathField.getText());
    }

    public void transfer(String transferPath) {
        FileInfo selectedFile = getSelectedFile();
        if (selectedFile != null && !selectedFile.getFileName().equals("..")) {
            String selectedFileName = selectedFile.getFileName();
            String currentDir = getCurrentDir();
            try {
                fileHandler.transferFile(transferPath, currentDir, selectedFileName);
                String message = "File " + selectedFileName + " was successfully transferred";
                Platform.runLater(() -> notifyAboutEvent(message));
            } catch (JSchException | SftpException exc) {
                Platform.runLater(FilePanel::notifyConnectionError);
            } catch (FileNotFoundException exc) {
                Platform.runLater(this::notifyAboutFile);
            }
        }
    }

    public static void notifyConnectionError() {
        String message = "Connection error. Please, don't spam buttons and wait for some time. " +
                         "If this message repeats, try to reconnect by SSH and make sure your " +
                         "server is ready for SSH connections.";
        CustomAlert notifyAlert = new CustomAlert(message,
                "Connection error", ButtonType.OK);
        ObservableList<Window> windowList = Window.getWindows().filtered(window-> window instanceof Stage);
        notifyAlert.initOwner(windowList.get(0));
        notifyAlert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable();
        initTableContextMenu();
    }

    protected void initComboBox() {
        LinkedList<String> rootDirs = fileHandler.getRootDirectories();
        diskComboBox.getItems().addAll(rootDirs);
        diskComboBox.getSelectionModel().select(0);
    }

    private void initTable() {
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setCellValueFactory(new PropertyValueFactory<>("fileType"));

        TableColumn<FileInfo,Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                if (value==null || empty) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(value==-1 ? "DIR" : value.toString());
                }
            }
        });

        TableColumn<FileInfo, String>  fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastModifiedDate"));

        fileTable.getColumns().addAll(fileNameColumn,fileTypeColumn,fileSizeColumn,fileDateColumn);
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fileTable.getSortOrder().add(fileNameColumn);

    }

    private void initTableContextMenu() {
        MenuItem removeOption = new MenuItem("Remove file");
        MenuItem moveOption = new MenuItem("Move file");

        removeOption.setOnAction(action -> {
            FileInfo localFileInfo = fileTable.getSelectionModel().getSelectedItem();
            if (localFileInfo!=null) {
                String curDir = pathField.getText();
                String fileName = localFileInfo.getFileName();
                SecureShellSession sshSession = ModelData.getInstance().getSshSession();
                if ((this instanceof RemotePanel && sshSession.isEstablished()) || (this instanceof LocalPanel)) {
                    startInNewThread(() -> {
                        try {
                            fileHandler.deleteFile(curDir, fileName);
                        } catch (JSchException | SftpException exc) {
                            Platform.runLater(FilePanel::notifyConnectionError);
                        } catch (FileNotFoundException exc) {
                            Platform.runLater(this::notifyAboutFile);
                        }
                    });
                } else {
                    notifyConnectionError();
                }
            }
        });

        moveOption.setOnAction(action -> {
            FileInfo localFileInfo = fileTable.getSelectionModel().getSelectedItem();
            if (localFileInfo != null) {
                String distDir = this.chooseDirectory();
                String srcDir = this.getCurrentDir();
                String fileName = localFileInfo.getFileName();
                boolean forceFlag = false, createNewFlag = false;
                if (distDir!=null && !distDir.equals(srcDir)) {
                    startInNewThread(()->tryToMoveFile(distDir, srcDir, fileName,forceFlag,createNewFlag));
                }
            }
        });

        fileTable.setContextMenu(new ContextMenu(removeOption, moveOption));
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
            Platform.runLater(()->updateTable(getCurrentDir()));
        });
        new Thread(task).start();
    }

    private void tryToMoveFile(String distDir,String srcDir,String fileName,boolean forceFlag,boolean createNewFlag) {
        try {
            fileHandler.moveFile(distDir, srcDir, fileName, forceFlag, createNewFlag);
        } catch (FileNotFoundException exc) {
            Platform.runLater(this::notifyAboutFile);
        } catch (FileAlreadyExistsException exc) {
            CompletableFuture<Optional<ButtonType>> result = new CompletableFuture<>();
            Platform.runLater(()-> result.complete(notifyAlreadyExistingFile()));
            try {
                Optional<ButtonType> notificationRes = result.get();
                if (notificationRes.isPresent() && notificationRes.get().getText().equals("Replace")) {
                    forceFlag = true;
                    fileHandler.moveFile(distDir,srcDir,fileName, forceFlag,createNewFlag);
                } else {
                    createNewFlag = true;
                    fileHandler.moveFile(distDir,srcDir,fileName,forceFlag,createNewFlag);
                }
            } catch (IOException | ExecutionException | InterruptedException exc1) {
                exc1.printStackTrace();
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private String chooseDirectory() {
        String chosenDir = null;
        try {
            FilePanel newFilePanel = getClass().getDeclaredConstructor().newInstance();
            newFilePanel.getFileTable().getContextMenu().getItems().removeIf(item->item.getText().equals("Move file"));
            CustomAlert directoryChooser = new CustomAlert(newFilePanel);
            SecureShellSession sshSession = ModelData.getInstance().getSshSession();
            Optional<ButtonType> result = Optional.empty();
            if ((newFilePanel instanceof RemotePanel && sshSession.isEstablished()) || (newFilePanel instanceof LocalPanel))
                result = directoryChooser.showAndWait();
            if (result.isPresent() && result.get()==ButtonType.APPLY){
                chosenDir = newFilePanel.getCurrentDir();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exc) {
            exc.printStackTrace();
        }
        return chosenDir;
    }

    private void notifyAboutEvent(String message) {
        CustomAlert notifyAlert = new CustomAlert(message,
                "Notification", ButtonType.OK);
        notifyAlert.showAndWait();
    }

    private void notifyAboutFile() {
        CustomAlert notifyAlert = new CustomAlert("File or target directory doesn't exist",
                "Error", ButtonType.OK);
        notifyAlert.showAndWait();
    }

    private Optional<ButtonType> notifyAlreadyExistingFile() {
        ButtonType replaceButton = new ButtonType("Replace");
        ButtonType skipButton = new ButtonType("Skip");
        String message = "File already exists. Do you want to replace it?";
        CustomAlert fileExistsAlert = new CustomAlert(message, "File exists", replaceButton,skipButton);
        fileExistsAlert.initOwner(Window.getWindows().get(Window.getWindows().size()-1));
        return fileExistsAlert.showAndWait();
    }

}
