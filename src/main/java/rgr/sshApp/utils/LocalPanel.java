package rgr.sshApp.utils;

import java.io.IOException;
import java.util.LinkedList;

public class LocalPanel extends FilePanel {

    private LocalFiles localFiles;

    public LocalPanel() {
        super();
        localFiles = new LocalFiles();
        initComboBox();
        updateTable(getInitialPath());
    }

    @Override
    public String getFileName(String filePath) {
        return localFiles.getFileName(filePath);
    }

    @Override
    public LinkedList<FileInfo> getFileList(java.lang.String path) throws IOException {
        return localFiles.getFileList(path);
    }

    @Override
    public LinkedList<String> getRootDirectories() {
        return localFiles.getRootDirectories();
    }

    @Override
    public String getInitialPath() {
        return localFiles.getInitialPath();
    }

    @Override
    public java.lang.String getParentDirectory(java.lang.String currentPath) {
        return localFiles.getParentDirectory(currentPath);
    }

    @Override
    public java.lang.String getResolvedDirectory(String fileName, String currentPath) {
        return localFiles.getResolvedDirectory(fileName,currentPath);
    }

    @Override
    public boolean isExists(String path, String fileName) {
        return localFiles.isExists(path,fileName);
    }

    @Override
    public void deleteFile(String path, String fileName) {
        localFiles.deleteFile(path,fileName);
    }

    @Override
    public void transferFile(String remoteTransferPath, String localFileDir, String fileName) {
        localFiles.transferFile(remoteTransferPath, localFileDir,fileName);
    }
}
