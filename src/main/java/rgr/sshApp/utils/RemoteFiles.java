package rgr.sshApp.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Vector;
import java.util.stream.Collectors;

public class RemoteFiles extends Files {

    private SecureShellSession sshSession;
    private SecureFileTransferChannel channel;

    public RemoteFiles() {
        this.sshSession = ModelData.getInstance().getSshSession();
        this.channel = sshSession.getConstChannel();
    }

    public RemoteFiles(SecureFileTransferChannel newChannel) {
        this.sshSession = ModelData.getInstance().getSshSession();
        this.channel = newChannel;
    }

    @Override
    public LinkedList<FileInfo> getFileList(String path) throws IOException {
        Vector<ChannelSftp.LsEntry> fileList = null;
        LinkedList<FileInfo> fileInfos = null;
        if (channel!=null) {
            fileList = channel.listDirectory(path);
            System.out.println("GETTING DIR LIST: ");
            fileList.stream().forEach(file -> {
                String fileInfo = "Name: " + file.getFilename() + " size: " +file.getAttrs().getSize() + " isDir: " +
                        file.getAttrs().isDir() + " lastModified: " +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(file.getAttrs().getMTime() * 1000L);
                System.out.println(fileInfo);
            });
            fileInfos = fileList.stream().filter(file-> !file.getFilename().equals("."))
                        .map(FileInfo::parseFilePath).collect(Collectors.toCollection(LinkedList::new));
        }
        return fileInfos;
    }

    @Override
    public LinkedList<String> getRootDirectories() {
        LinkedList<String> rootDirs = new LinkedList<>();
        rootDirs.add("/");
        return rootDirs;
    }

    @Override
    public String getInitialPath() {
        String pwd = null;
        if (channel!=null) {
            pwd = channel.presentWorkingDirectory();
        }
        return pwd;
    }

    @Override
    public String getParentDirectory(String path) {
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
    public String getResolvedDirectory(String fileName, String currentPath) {
        String resolvedDir = null;
        if (currentPath.equals("/")) {
            resolvedDir = currentPath + fileName;
        }
        else {
            resolvedDir = currentPath + "/" + fileName;
        }
        return resolvedDir;
    }

    @Override
    public boolean isExists(String path, String fileName) {
        boolean existing = false;
        SftpATTRS attrs = null;
        if (channel!=null) {
            channel.changeDirectory(path);
            attrs = channel.getAttrs(fileName);
            existing = (attrs != null);
            System.out.println("FILE IS EXISTING = " + existing);
        }
        return existing;
    }

    @Override
    public void deleteFile(String path, String fileName) {
        if (sshSession!=null) {
            SecureFileTransferChannel newChannel = new SecureFileTransferChannel(sshSession.getSession());
            newChannel.connect();
            String filePath = this.getResolvedDirectory(fileName,path);
            if (this.isExists(path,fileName)) {
                newChannel.changeDirectory(path);
                if (newChannel.getAttrs(fileName).isDir()) {
                    sshSession.executeCommand("rm -rf " + filePath);
                } else {
                    newChannel.deleteFile(path,fileName);
                }
                System.out.println("Remote file = " + fileName + " deleted");
            }
            newChannel.disconnect();
        }
    }
}
