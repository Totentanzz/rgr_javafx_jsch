package rgr.sshApp.utils.files;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.LinkedList;

public interface FilePath {

    String getFileName(String filePath);

    String getNextFileName(String fileName);

    LinkedList<FileInfo> getFileList(String path) throws IOException;

    LinkedList<String> getRootDirectories();

    String getInitialPath();

    String getParentDirectory(String currentPath);

    String getResolvedDirectory(String currentPath, String fileName);

    boolean isExists(String path, String fileName);

    boolean isDir(String path, String fileName);

    void deleteFile(String path, String fileName) throws JSchException;

    void transferFile(String transferPath, String fileDir, String fileName) throws JSchException;

    void moveFile(String distDir, String srcDir, String fileName, boolean forceFlag, boolean createNewFlag) throws IOException;

}