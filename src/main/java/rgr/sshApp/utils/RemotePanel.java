package rgr.sshApp.utils;

import rgr.sshApp.model.ModelData;

import java.io.IOException;
import java.util.LinkedList;

public class RemotePanel extends FilePanel {

    private RemoteFiles remoteFiles;

    public RemotePanel() {
        super();
        this.remoteFiles = new RemoteFiles(ModelData.getInstance().getSshSession().getConstChannel());
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
    public java.lang.String getResolvedDirectory(String fileName, String currentPath) {
        return remoteFiles.getResolvedDirectory(fileName,currentPath);
    }

    @Override
    public boolean isExists(String path, String fileName) {
        return remoteFiles.isExists(path,fileName);
    }

    @Override
    public void deleteFile(String path, String fileName) {
        remoteFiles.deleteFile(path,fileName);
    }

    @Override
    public void transferFile(String localTransferPath, String remoteFileDir, String fileName) {
        remoteFiles.transferFile(localTransferPath,remoteFileDir,fileName);
    }
}
