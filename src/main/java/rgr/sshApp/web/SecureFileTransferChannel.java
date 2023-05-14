package rgr.sshApp.web;

import com.jcraft.jsch.*;
import lombok.Getter;
import rgr.sshApp.utils.LocalFiles;
import rgr.sshApp.utils.RemoteFiles;

import java.util.Vector;

public class SecureFileTransferChannel {

    ChannelSftp sftpChannel;
    LocalFiles localFiles;
    RemoteFiles remoteFiles;

    public SecureFileTransferChannel(Session session) {
        try {
            this.sftpChannel = (ChannelSftp) session.openChannel("sftp");
            this.sftpChannel.setBulkRequests(32);
            this.localFiles = new LocalFiles();
            this.remoteFiles = new RemoteFiles(this);
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
        String remoteFilePath = remoteFiles.getResolvedDirectory(fileName, remoteDir);
        String localDir = localFiles.getParentDirectory(localFilePath);
        try {
            if (!remoteFiles.isExists(remoteDir,fileName) && remoteFiles.isRemoteFileNewer(remoteFilePath,localDir,fileName)<0) {
                sftpChannel.put(localFilePath, remoteDir);
                System.out.println("UPLOADING FILE = " + fileName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SFTPC.uploadFile: uploading file error");
            exc.printStackTrace();
        }
    }

    public void downloadFile(String remoteFilePath, String localDir) {
        String fileName = remoteFiles.getFileName(remoteFilePath);
        try {
            if (!localFiles.isExists(localDir,fileName) && remoteFiles.isRemoteFileNewer(remoteFilePath, localDir,fileName)>0) {
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
            if (remoteFiles.isExists(remoteDir,fileName)) {
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
        String parentDir = remoteFiles.getParentDirectory(remoteFilePath);
        String folderName = remoteFiles.getFileName(remoteFilePath);
        try {
            if (!remoteFiles.isExists(parentDir,folderName)) {
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
            if (!remoteFiles.isExists(remotePath,folderName)) {
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

}
