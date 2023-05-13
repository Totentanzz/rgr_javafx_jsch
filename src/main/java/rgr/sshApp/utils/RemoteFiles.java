package rgr.sshApp.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;
import rgr.sshApp.web.SecureShellSession;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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

    public boolean isRemoteFileNewer(String remoteFilePath, String localPath, String fileName) {
        SftpATTRS attrs = null;
        String remoteParentDir = getParentDirectory(remoteFilePath);
        Path localFilePath = Path.of(localPath).toAbsolutePath().normalize().resolve(fileName);
        long localMTime = 0, remoteMTime = 0;
        try {
            channel.changeDirectory(remoteParentDir);
            attrs = channel.getAttrs(fileName);
            localMTime = java.nio.file.Files.getLastModifiedTime(localFilePath).to(TimeUnit.SECONDS);
            remoteMTime = attrs.getMTime();
        } catch (IOException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: can't get LastModifiedTime");
        }
        boolean isRemoteFileNewer = (remoteMTime>localMTime ? true : false);
        System.out.println("REMOTE FILE = " + fileName + " IS NEWER = " + isRemoteFileNewer);
        return isRemoteFileNewer;
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

    @Override
    public void transferFile(String localTransferPath, String remoteFileDir, String fileName) {
            String remoteFilePath = remoteFileDir + "/" + fileName;
            Path currentDir = Path.of(localTransferPath).toAbsolutePath().normalize();
            SecureFileTransferChannel newSftpChannel = new SecureFileTransferChannel(sshSession.getSession());
            newSftpChannel.connect();
            System.out.println("New channel is connected = " + newSftpChannel.isConnnected());
            System.out.println("Downloading file = " + remoteFilePath);
            System.out.println("Downloading to the dir = " + currentDir);
            newSftpChannel.changeDirectory(remoteFileDir);
            SftpATTRS fileAttrs = newSftpChannel.getAttrs(fileName);
            if (fileAttrs!=null) {
                if (fileAttrs.isDir() && !fileName.equals("..")) {
                    downloadFolder(remoteFilePath,currentDir,fileName,newSftpChannel);
                } else if (!fileAttrs.isDir()) {
                    newSftpChannel.downloadFile(remoteFilePath,currentDir.toString());
                }
            }
            System.out.println("Downloading finished");
            newSftpChannel.disconnect();
            System.out.println("New channel is disconnected = " + !newSftpChannel.isConnnected());
    }

    private void downloadFolder(String remotePath, Path localPath, String fileName, SecureFileTransferChannel channel) {
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
                        downloadFolder(remoteFilePath,createdLocalFolder,remoteFileName,channel);
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
            throw new RuntimeException();
        }
    }
}
