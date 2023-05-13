package rgr.sshApp.utils;

import java.io.IOException;
import java.util.LinkedList;

public interface FilePath {

    String getFileName(String filePath);

    LinkedList<FileInfo> getFileList(String path) throws IOException;

    LinkedList<String> getRootDirectories();

    String getInitialPath();

    String getParentDirectory(String currentPath);

    String getResolvedDirectory(String fileName, String currentPath);

    boolean isExists(String path, String fileName);

    void deleteFile(String path, String fileName);

    void transferFile(String transferPath, String fileDir, String fileName);

}
