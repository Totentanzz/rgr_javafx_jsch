package rgr.sshApp.utils;

import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;

import java.io.IOException;
import java.util.LinkedList;

public class RemotePanel extends FilePanel {

    private RemoteFiles remoteFiles;

    public RemotePanel() {
        super();
//        SecureFileTransferChannel checkingChannel = ModelData.getInstance().getSshSession().getCheckingChannel();
//        SecureFileTransferChannel fileListChannel = ModelData.getInstance().getSshSession().getGettingFileListChannel();
        this.remoteFiles = new RemoteFiles();
        initComboBox();
        updateTable(getInitialPath());
    }

    public RemoteFiles getRemoteFiles() {
        return remoteFiles;
    }

    @Override
    public String getFileName(String filePath) {
        return remoteFiles.getFileName(filePath);
    }

    @Override
    public String getNextFileName(String fileName) {
        return remoteFiles.getNextFileName(fileName);
    }

    @Override
    public LinkedList<FileInfo> getFileList(java.lang.String path) throws IOException {
        return remoteFiles.getFileList(path);
    }

    @Override
    public LinkedList<String> getRootDirectories() {
        return remoteFiles.getRootDirectories();
    }

    @Override
    public String getInitialPath() {
        return remoteFiles.getInitialPath();
    }

    @Override
    public java.lang.String getParentDirectory(java.lang.String path) {
        return remoteFiles.getParentDirectory(path);
    }

    @Override
    public java.lang.String getResolvedDirectory(String currentPath, String fileName) {
        return remoteFiles.getResolvedDirectory(currentPath, fileName);
    }

    @Override
    public boolean isExists(String path, String fileName) {
        return remoteFiles.isExists(path,fileName);
    }

    @Override
    public boolean isDir(String path, String fileName) {
        return remoteFiles.isDir(path,fileName);
    }

    @Override
    public void deleteFile(String path, String fileName) {
        remoteFiles.deleteFile(path,fileName);
    }

    @Override
    public void transferFile(String localTransferPath, String remoteFileDir, String fileName) {
        remoteFiles.transferFile(localTransferPath,remoteFileDir,fileName);
    }

    @Override
    public void moveFile(String distDir, String srcDir, String fileName, boolean forceFlag, boolean createNewFlag) throws IOException {
        remoteFiles.moveFile(distDir,srcDir,fileName, forceFlag, createNewFlag);
    }
}
