package rgr.sshApp.utils.files.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;

import rgr.sshApp.utils.files.FileInfo;
import rgr.sshApp.web.SecureFtpChannel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Vector;
import java.util.stream.Collectors;

public class RemoteFiles extends Files {

    private SecureFtpChannel gettingFileListChannel;
    private SecureFtpChannel chekingChannel;

    public RemoteFiles() {
        super();
        this.gettingFileListChannel = this.sshSession.getFileListChannel();
        this.chekingChannel = this.sshSession.getCheckingChannel();
    }

    public void setChannels(SecureFtpChannel checkChannel, SecureFtpChannel fileListChannel) {
        this.gettingFileListChannel = fileListChannel;
        this.chekingChannel = checkChannel;
    }

    @Override
    public LinkedList<FileInfo> getFileList(String path) throws IOException {
        LinkedList<FileInfo> fileInfos = null;
        Vector<ChannelSftp.LsEntry> fileList = gettingFileListChannel.listDirectory(path);
        if (fileList !=null) {
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
        String pwd = chekingChannel.presentWorkingDirectory();
        return pwd;
    }

    @Override
    public boolean isExists(String path, String fileName) {
        return chekingChannel.isExists(path,fileName);
    }

    @Override
    public boolean isDir(String path, String fileName) {
        return chekingChannel.isDir(path,fileName);
    }

    @Override
    public void deleteFile(String path, String fileName) throws JSchException {
        SecureFtpChannel newChannel = new SecureFtpChannel(sshSession.getSession());
        newChannel.connect();
        String filePath = this.getResolvedDirectory(path, fileName);
        if (newChannel.isExists(path,fileName)) {
            newChannel.changeDirectory(path);
            if (newChannel.isDir(path,fileName)) {
                sshSession.executeCommand("rm -rf " + filePath);
            } else {
                newChannel.deleteFile(path,fileName);
            }
            System.out.println("Remote file = " + fileName + " deleted");
        }
        newChannel.disconnect();
    }

    @Override
    public void transferFile(String localTransferPath, String remoteFileDir, String fileName) throws JSchException {
        String remoteFilePath = remoteFileDir + "/" + fileName;
        Path transferPath = Path.of(localTransferPath).toAbsolutePath().normalize();
        SecureFtpChannel newSftpChannel = new SecureFtpChannel(sshSession.getSession());
        newSftpChannel.connect();
        System.out.println("New channel is connected = " + newSftpChannel.isConnected());
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
        System.out.println("New channel is disconnected = " + !newSftpChannel.isConnected());
    }

    @Override
    public void moveFile(String distDir, String srcDir, String fileName, boolean forceFlag, boolean createNewFlag) throws IOException {
        String srcFilePath = srcDir + "/" + fileName;
        String distFilePath = distDir + "/" + fileName;
        String moveCommand = new StringBuilder()
                         .append("mv ")
                         .append(srcFilePath)
                         .append(" ")
                         .append(distDir)
                         .toString();
        if (!isExists(distDir,fileName)) {
            System.out.println("SIT 0");
            String response = sshSession.executeCommand(moveCommand);
            System.out.println(response);
        } else if (forceFlag) {
            System.out.println("SIT 1");
            System.out.println(moveCommand);
            String response = null;
            if (isDir(srcDir,fileName)) {
                String removeDirCommand = "rmdir " + srcFilePath;
                moveCommand = moveCommand.replace(srcFilePath,srcFilePath+"/*");
                moveCommand = moveCommand +"/" + fileName + "/";
                response = sshSession.executeCommand(moveCommand + ";" + removeDirCommand);
            } else {
                moveCommand = moveCommand.replaceFirst("mv ","mv -f ");
                response = sshSession.executeCommand(moveCommand);
            }
            System.out.println(response);
        } else if (createNewFlag) {
            String newFileName = getNextFileName(fileName);
            while (isExists(distDir,newFileName)) newFileName = getNextFileName(newFileName);
            String newSrcFilePath = srcDir + "/" + newFileName;
            String renameCommand = new StringBuilder()
                                   .append("mv ")
                                   .append(srcFilePath)
                                   .append(" ")
                                   .append(newSrcFilePath)
                                   .toString();
            moveCommand = moveCommand.replace(srcFilePath,newSrcFilePath);
            String response = sshSession.executeCommand(renameCommand + ";" + moveCommand);
            System.out.println(response);
        } else {
            throw new IOException("File already exists");
        }
    }

    private void downloadFolder(Path localPath, String remotePath, String fileName, SecureFtpChannel channel) {
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