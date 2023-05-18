package rgr.sshApp.utils.files.handlers;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import rgr.sshApp.utils.files.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public interface FilePath {

    String getFileName(String filePath);

    String getNextFileName(String fileName);

    LinkedList<FileInfo> getFileList(String path) throws IOException, SftpException;

    LinkedList<String> getRootDirectories();

    String getInitialPath() throws SftpException;

    String getParentDirectory(String currentPath);

    String getResolvedDirectory(String currentPath, String fileName);

    boolean isExists(String path, String fileName);

    boolean isDir(String path, String fileName);

    void transferFile(String transferPath, String fileDir, String fileName) throws JSchException, SftpException, FileNotFoundException;

    void deleteFile(String path, String fileName) throws JSchException, SftpException, FileNotFoundException;

    void moveFile(String distDir, String srcDir, String fileName, boolean forceFlag, boolean createNewFlag) throws IOException;

}
