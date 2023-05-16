package rgr.sshApp.web;

import com.jcraft.jsch.*;
import rgr.sshApp.utils.LocalFiles;
import rgr.sshApp.utils.RemoteFiles;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class SecureFileTransferChannel {

    ChannelSftp sftpChannel;
    LocalFiles localFiles;

    public SecureFileTransferChannel(Session session) {
        try {
            this.sftpChannel = (ChannelSftp) session.openChannel("sftp");
            this.sftpChannel.setBulkRequests(32);
            this.localFiles = new LocalFiles();
        } catch (JSchException exc) {
            System.out.println("SFTPC.SecureShellChannel: opening channel error");
            exc.printStackTrace();
        }
    }

    public void connect() {
        try {
            sftpChannel.connect();
        } catch (JSchException exc) {
            System.out.println("SFTPC.connect: connecting error");
            exc.printStackTrace();
        }
    }

    public void disconnect() {
        if (sftpChannel!=null && sftpChannel.isConnected()) sftpChannel.disconnect();
    }

    public boolean isConnnected() {
        boolean state = false;
        if (sftpChannel!=null) {
            state = sftpChannel.isConnected();
        }
        return state;
    }

    public void uploadFile(String localFilePath, String remoteDir) {
        String fileName = localFiles.getFileName(localFilePath);
        String remoteFilePath = localFiles.getResolvedDirectory(remoteDir, fileName);
        String localDir = localFiles.getParentDirectory(localFilePath);
        try {
            if (!isExists(remoteDir,fileName) || isRemoteFileNewer(remoteFilePath,localDir,fileName)<0) {
                sftpChannel.put(localFilePath, remoteDir);
                System.out.println("UPLOADING FILE = " + fileName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SFTPC.uploadFile: uploading file error");
            exc.printStackTrace();
        }
    }

    public void downloadFile(String remoteFilePath, String localDir) {
        String fileName = localFiles.getFileName(remoteFilePath);
        try {
            if (!localFiles.isExists(localDir,fileName) || isRemoteFileNewer(remoteFilePath, localDir,fileName)>0) {
                sftpChannel.get(remoteFilePath, localDir);
                System.out.println("DOWNLOADING FILE FROM " + remoteFilePath + " TO " + localDir + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SFTPC.downloadFile: downloading file error");
            exc.printStackTrace();
        }
    }

    public void deleteFile(String remoteDir, String fileName) {
        try {
            if (isExists(remoteDir,fileName)) {
                changeDirectory(remoteDir);
                sftpChannel.rm(fileName);
                System.out.println("DELETING FILE = " + fileName + " IN " + remoteDir + " HAS FINISHED");
            }
        } catch (SftpException exc){
            System.out.println("SFTPC.deleteFile: cannot remove " + fileName + ",no such file or directory");
        }
    }

    public void changeDirectory(String remoteDir) {
        try {
            sftpChannel.cd(remoteDir);
            System.out.println("CHANGING DIRECTORY TO = " + remoteDir + " HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SFTPC.changeDirectory: cannot access " + remoteDir + ", no such file or directory");
        }
    }

    public String presentWorkingDirectory(){
        String pwd = null;
        try {
            pwd = sftpChannel.pwd();
            System.out.println("GETTING CURRENT PWD = " + pwd + " HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SFTPC.presentWorkingDirectory: pwd command error");
            exc.printStackTrace();
        }
        return pwd;
    }

    public Vector<ChannelSftp.LsEntry> listDirectory(String remoteDir) {
        Vector<ChannelSftp.LsEntry> fileList = null;
        try {
            fileList = sftpChannel.ls(remoteDir);
            System.out.println("GETTING FILE LIST HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SFTPC.listDirectory: cannot access " + remoteDir + ", no such file or directory");
        }
        return fileList;
    }

    public void makeDir(String remoteFilePath) {
        String parentDir = localFiles.getParentDirectory(remoteFilePath);
        String folderName = localFiles.getFileName(remoteFilePath);
        try {
            if (!isExists(parentDir,folderName)) {
                sftpChannel.mkdir(folderName);
                System.out.println("CREATING FOLDER IN CURRENT DIR = " + folderName + " HAS FINISHED");
            }
        } catch (SftpException exc){
            System.out.println("SFTPC.makeDir: mkdir command error");
            exc.printStackTrace();
        }
    }

    public void makeDir(String remotePath, String folderName) {
        try {
            if (!isExists(remotePath,folderName)) {
                changeDirectory(remotePath);
                sftpChannel.mkdir(folderName);
                System.out.println("CREATING IN DIR = " + remotePath + " FOLDER = " + folderName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SFTPC.makeDir: mkdir command error");
            exc.printStackTrace();
        }
    }

    public SftpATTRS getAttrs(String fileName) {
        SftpATTRS attrs = null;
        try {
            attrs = sftpChannel.stat(fileName);
        } catch (SftpException exc) {
            System.out.println("SFTPC.getAttrs: file doesn't exists");
        }
        return attrs;
    }

    public boolean isExists(String path, String fileName) {
        boolean existing = false;
        SftpATTRS attrs = null;
        changeDirectory(path);
        attrs = getAttrs(fileName);
        existing = (attrs != null);
        System.out.println("FILE IS EXISTING = " + existing);
        return existing;
    }

    public boolean isDir(String path, String fileName) {
        boolean isDir = false;
        SftpATTRS attrs = null;
        changeDirectory(path);
        attrs = getAttrs(fileName);
        isDir = attrs.isDir();
        System.out.println("FILE IS DIR = " + isDir);
        return isDir;
    }

    public byte isRemoteFileNewer(String remoteFilePath, String localDir, String fileName) {
        SftpATTRS attrs = null;
        String remoteParentDir = getParent(remoteFilePath);
        Path localFilePath = Path.of(localDir).toAbsolutePath().normalize().resolve(fileName);
        long localMTime = 0, remoteMTime = 0;
        try {
            changeDirectory(remoteParentDir);
            attrs = getAttrs(fileName);
            localMTime = java.nio.file.Files.getLastModifiedTime(localFilePath).to(TimeUnit.SECONDS);
            remoteMTime = attrs.getMTime();
        } catch (IOException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: can't get LastModifiedTime");
        }
        byte state = (byte)(remoteMTime - localMTime);
        System.out.println("REMOTE FILE = " + fileName + " IS NEWER = " + state);
        return state;
    }

    private String getParent(String remoteFilePath) {
        int lastSlashIndex = remoteFilePath.lastIndexOf("/");
        String parentDir = null;
        if (lastSlashIndex==0 && !remoteFilePath.equals("/")) {
            parentDir = "/";
        }
        else if (lastSlashIndex!=0) {
            parentDir = remoteFilePath.substring(0,lastSlashIndex);
        }
        return parentDir;
    }

}
