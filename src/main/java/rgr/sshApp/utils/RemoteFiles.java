package rgr.sshApp.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
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
        String pwd = channel.presentWorkingDirectory();
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
        channel.changeDirectory(path);
        SftpATTRS attrs = channel.getAttrs(fileName);
        existing = (attrs != null);
        System.out.println("FILE IS EXISTING = " + existing);
        return existing;
    }

    public byte isRemoteFileNewer(String remoteFilePath, String localDir, String fileName) {
        SftpATTRS attrs = null;
        String remoteParentDir = getParentDirectory(remoteFilePath);
        Path localFilePath = Path.of(localDir).toAbsolutePath().normalize().resolve(fileName);
        long localMTime = 0, remoteMTime = 0;
        try {
            channel.changeDirectory(remoteParentDir);
            attrs = channel.getAttrs(fileName);
            localMTime = java.nio.file.Files.getLastModifiedTime(localFilePath).to(TimeUnit.SECONDS);
            remoteMTime = attrs.getMTime();
        } catch (IOException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: can't get LastModifiedTime");
        }
        byte state = (byte)(remoteMTime - localMTime);
        System.out.println("REMOTE FILE = " + fileName + " IS NEWER = " + state);
        return state;
    }

    @Override
    public void deleteFile(String path, String fileName) {
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

    @Override
    public void transferFile(String localTransferPath, String remoteFileDir, String fileName) {
        String remoteFilePath = remoteFileDir + "/" + fileName;
        Path transferPath = Path.of(localTransferPath).toAbsolutePath().normalize();
        SecureFileTransferChannel newSftpChannel = new SecureFileTransferChannel(sshSession.getSession());
        newSftpChannel.connect();
        System.out.println("New channel is connected = " + newSftpChannel.isConnnected());
        System.out.println("Downloading file = " + remoteFilePath);
        System.out.println("Downloading to the dir = " + transferPath);
        newSftpChannel.changeDirectory(remoteFileDir);
        SftpATTRS fileAttrs = newSftpChannel.getAttrs(fileName);
        boolean remoteFileExists = (fileAttrs != null);
        if (remoteFileExists) {
            if (fileAttrs.isDir() && !fileName.equals("..")) {
                downloadFolder(transferPath, remoteFilePath, fileName,newSftpChannel);
            } else if (!fileAttrs.isDir()) {
                newSftpChannel.downloadFile(remoteFilePath,transferPath.toString());
            }
        }
        System.out.println("Downloading finished");
        newSftpChannel.disconnect();
        System.out.println("New channel is disconnected = " + !newSftpChannel.isConnnected());
    }

    private void downloadFolder(Path localPath, String remotePath, String fileName, SecureFileTransferChannel channel) {
        System.out.println("curLocalPath: " + localPath);
        System.out.println("creating localPath: " + remotePath);
        Path createdLocalFolder = localPath.resolve(Path.of(fileName));
        System.out.println("folderName: " + fileName);
        System.out.println("created local folder: " + createdLocalFolder);
        try {
            if (!java.nio.file.Files.exists(createdLocalFolder)) java.nio.file.Files.createDirectory(createdLocalFolder);
            System.out.println("Created dir path: " + createdLocalFolder);
            Vector<ChannelSftp.LsEntry> files = channel.listDirectory(remotePath);
            for (ChannelSftp.LsEntry file : files) {
                String remoteFileName = file.getFilename();
                if (!remoteFileName.equals(".") && !remoteFileName.equals("..")) {
                    String remoteFilePath =  remotePath + "/" + remoteFileName;
                    Path localFilePath = createdLocalFolder.resolve(remoteFileName);
                    System.out.println("fileName: " + remoteFileName);
                    if (file.getAttrs().isDir()) {
                        System.out.println("file = " + remoteFileName + " is Dir. Creating new dir");
                        downloadFolder(createdLocalFolder, remoteFilePath, remoteFileName,channel);
                    } else {
                        System.out.println("file = " + remoteFileName + " is file. Downloading file");
                        channel.downloadFile(remoteFilePath,createdLocalFolder.toString());
                        System.out.println("Downloaded file with name: " + remoteFileName + " to local path " + createdLocalFolder);
                    }
                }
            }
        } catch (IOException exc) {
            System.out.println("ManagerController.downloadFolder: recursive method/file listing error");
            exc.printStackTrace();
        }
    }
}
