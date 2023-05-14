package rgr.sshApp.utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Window;
import lombok.SneakyThrows;
import rgr.sshApp.SshApp;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

public abstract class FilePanel extends VBox implements Initializable, FilePath {

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
        String parentDir = getParentDirectory(curDir);
        if (parentDir!=null) {
            updateTable(parentDir);
        }
    }
    @FXML
    public void moveToSelectedDir(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount()==2) {
            FileInfo selectedFile = fileTable.getSelectionModel().getSelectedItem();
            String currentPath = pathField.getText();
            System.out.println("Selected Item: " + selectedFile);
            if (selectedFile != null && selectedFile.getFileName().equals("..")) {
                backButton.fire();
            }
            else if (selectedFile != null && selectedFile.getFileSize() == -1) {
               String newDir = getResolvedDirectory(selectedFile.getFileName(),currentPath);
               System.out.println(newDir);
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
        try {
            pathField.setText(path);
            fileTable.getItems().clear();
            fileTable.getItems().addAll(getFileList(path));
            fileTable.sort();
        } catch (IOException exc) {
            updateTable(curPath);
            System.out.println("ManagerController.updateTable: getting list of files error");
            ErrorAlert errorAlert = new ErrorAlert("Entering to the folder error. Please, make sure" +
                    " to not enter the private system folder",
                    "File manager error");
            errorAlert.initOwner(Window.getWindows().get(0));
            errorAlert.showAndWait();
        }
    }

    public void transfer(String transferPath) {
        startInNewThread(() -> {
            FileInfo selectedFile = getSelectedFile();
            if (selectedFile != null) {
                String selectedFileName = selectedFile.getFileName();
                String currentDir = getCurrentDir();
                transferFile(transferPath, currentDir, selectedFileName);
                Platform.runLater(() -> updateTable(currentDir));
            }
        });
    }

    public void refresh() {
        updateTable(pathField.getText());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable();
        initTableContextMenu();
    }

    protected void initComboBox() {
        LinkedList<String> rootDirs = getRootDirectories();
        diskComboBox.getItems().addAll(rootDirs);
        diskComboBox.getSelectionModel().select(0);
    }

    protected void initTable() {
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
        fileTable.getSortOrder().add(fileNameColumn);
    }

    protected void initTableContextMenu() {
        MenuItem removeOption = new MenuItem("Remove file");
        MenuItem moveOption = new MenuItem("Move file");
        removeOption.setOnAction(action -> {
            startInNewThread(()->{
                FileInfo localFileInfo = fileTable.getSelectionModel().getSelectedItem();
                if (localFileInfo!=null) {
                    String curDir = pathField.getText();
                    String fileName = localFileInfo.getFileName();
                    deleteFile(curDir,fileName);
                    Platform.runLater(()->this.updateTable(pathField.getText()));
                }
            });
        });
        moveOption.setOnAction(action -> {
            startInNewThread(()->{
                FileInfo localFileInfo = fileTable.getSelectionModel().getSelectedItem();
                if (localFileInfo!=null) {

                }
            });
        });
        fileTable.setContextMenu(new ContextMenu(removeOption));
    }

    private void startInNewThread(Runnable action) {
        new Thread(action).start();
    }

}
