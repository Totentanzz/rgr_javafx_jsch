package rgr.sshApp.utils;

import javafx.fxml.FXMLLoader;
import rgr.sshApp.SshApp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalPanel extends FilePanel {

    public LocalPanel() {
        super();
        updateTable(getInitialPath());
    }

    @Override
    LinkedList<FileInfo> getFileList(String path) throws IOException {
        Path curPath = Path.of(path).toAbsolutePath().normalize();
        Path parentPath = curPath.getParent();
        LinkedList<FileInfo> dirList = Files.list(curPath).map(FileInfo::parseFilePath).collect(Collectors.toCollection(LinkedList::new));
        FileInfo parentDirInfo = parentPath==null ? FileInfo.parseFilePath(curPath) : FileInfo.parseFilePath(parentPath);
        parentDirInfo.setFileName("..");
        dirList.add(parentDirInfo);
        return dirList;
    }

    @Override
    LinkedList<String> getRootDirectories() {
        LinkedList<String> rootDirs = new LinkedList<>();
        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            rootDirs.add(path.toString());
        }
        return rootDirs;
    }

    @Override
    String getInitialPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    @Override
    String getParentDirectory(String currentPath) {
        Path parentPath = Path.of(currentPath).getParent();
        String parentDir = null;
        if (parentPath!=null) {
            parentDir = parentPath.toString();
        }
        return parentDir;
    }

    @Override
    public String getResolvedDirectory(FileInfo selectedFile, String currentPath) {
        return Path.of(currentPath).resolve(selectedFile.getFileName()).toString();
    }
}
