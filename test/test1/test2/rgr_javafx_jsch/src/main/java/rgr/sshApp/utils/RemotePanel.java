package rgr.sshApp.utils;

import com.jcraft.jsch.ChannelSftp;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Vector;
import java.util.stream.Collectors;

public class RemotePanel extends FilePanel {

    private SecureShellSession sshSession;
    private SecureFileTransferChannel tableUpdateChannel;

    public RemotePanel() {
        super();
        sshSession = ModelData.getInstance().getSshSession();
        tableUpdateChannel = sshSession.getConstChannel();
        updateTable(getInitialPath());
    }

    @Override
    LinkedList<FileInfo> getFileList(String path) throws IOException {
        Vector<ChannelSftp.LsEntry> fileList = tableUpdateChannel.listDirectory(path);
        System.out.println("GETTING DIR LIST: ");
        fileList.stream().forEach(file -> {
            String fileInfo = "Name: " + file.getFilename() + " size: " +file.getAttrs().getSize() + " isDir: " +
                            file.getAttrs().isDir() + " lastModified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.getAttrs().getMTime() * 1000L);
            System.out.println(fileInfo);
        });
        return fileList.stream().filter(file-> !file.getFilename().equals("."))
               .map(FileInfo::parseFilePath).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    LinkedList<String> getRootDirectories() {
        LinkedList<String> rootDirs = new LinkedList<>();
        rootDirs.add("/");
        return rootDirs;
    }

    @Override
    String getInitialPath() {
        return sshSession.getConstChannel().presentWorkingDirectory();
    }

    @Override
    String getParentDirectory(String path) {
        int lastSlashIndex = path.lastIndexOf("/");
        String parentDir = null;
        if (lastSlashIndex==0 && !path.equals("/")) {
            parentDir = "/";
        }
        else if (lastSlashIndex!=0) {
            parentDir = path.substring(0,lastSlashIndex);
        }
        return parentDir;
    }

    @Override
    public String getResolvedDirectory(FileInfo selectedFile, String currentPath) {
        String resolvedDir = null;
        if (currentPath.equals("/")) {
            resolvedDir = currentPath + selectedFile.getFileName();
        }
        else {
            resolvedDir = currentPath + "/" + selectedFile.getFileName();
        }
        return resolvedDir;
    }
}
