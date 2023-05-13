package rgr.sshApp.web;

import com.jcraft.jsch.*;
import rgr.sshApp.utils.LocalFiles;
import rgr.sshApp.utils.RemoteFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
            System.out.println("SecureShellChannel.SecureShellChannel: opening channel error");
            exc.printStackTrace();
        }
    }

    public void connect() {
        try {
            sftpChannel.connect();
        } catch (JSchException exc) {
            System.out.println("SecureShellChannel.connect: connecting error");
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

    public void uploadFile(String uploadingFilePath, String remoteDirectory) {
        //char separatorChar = uploadingFilePath.contains("/") ? '/' : '\\';
        char separatorChar = '\\';
        String fileName = uploadingFilePath.substring(uploadingFilePath.lastIndexOf(separatorChar)+1);
        try {
            if (!remoteFiles.isExists(remoteDirectory,fileName)){
                sftpChannel.put(uploadingFilePath, remoteDirectory);
                System.out.println("UPLOADING FILE = " + fileName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SecureShell.uploadFile: putting file error");
            exc.printStackTrace();
        }
    }

    public void downloadFile(String remoteFilePath, String localDirectory) {
        String fileName = remoteFiles.getFileName(remoteFilePath);
        try {
            if (!localFiles.isExists(localDirectory,fileName) || isRemoteFileNewer(remoteFilePath,localDirectory,fileName)) {
                sftpChannel.get(remoteFilePath, localDirectory);
                System.out.println("DOWNLOADING FILE FROM " + remoteFilePath + " TO " + localDirectory + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SecureShell.downloadFile: downloading file error");
            exc.printStackTrace();
        }
    }

    public void deleteFile(String remotePath, String fileName) {
        try {
            sftpChannel.cd(remotePath);
            sftpChannel.rm(fileName);
            System.out.println("DELETING FILE = " + fileName + " IN " + remotePath + " HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SecureShell.deleteFile: cd or rm commands error");
            exc.printStackTrace();
        }
    }

    public void changeDirectory(String remotePath) {
        try {
            sftpChannel.cd(remotePath);
            System.out.println("CHANGING DIRECTORY TO = " + remotePath + " HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SecureShell.changeDirectory: cd command error");
            exc.printStackTrace();
        }
    }

    public String presentWorkingDirectory(){
        String pwd = null;
        try {
            pwd = sftpChannel.pwd();
            System.out.println("GETTING CURRENT PWD = " + pwd + " HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SecureShell.presentWorkingDirectory: pwd command error");
            exc.printStackTrace();
        }
        return pwd;
    }

    public Vector<ChannelSftp.LsEntry> listDirectory(String remotePath) {
        Vector<ChannelSftp.LsEntry> fileList = null;
        try {
            fileList = sftpChannel.ls(remotePath);
            System.out.println("GETTING FILE LIST HAS FINISHED");
        } catch (SftpException exc){
            System.out.println("SecureShell.listDirectory: ls command error");
            exc.printStackTrace();
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
            System.out.println("SecureShell.makeDir: mkdir command error");
            exc.printStackTrace();
        }
    }

    public void makeDir(String remotePath, String folderName) {
        try {
            if (!remoteFiles.isExists(remotePath,folderName)) {
                sftpChannel.cd(remotePath);
                sftpChannel.mkdir(folderName);
                System.out.println("CREATING IN DIR = " + remotePath + " FOLDER = " + folderName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SecureShell.makeDir: mkdir/cd command error");
            exc.printStackTrace();
        }
    }

    public SftpATTRS getAttrs(String fileName) {
        SftpATTRS attrs = null;
        try {
            attrs = sftpChannel.stat(fileName);
        } catch (SftpException exc) {
            System.out.println("SecureShell.getAttrs: file doesn't exists");
        }
        return attrs;
    }

    private boolean isRemoteFileNewer(String remoteFilePath, String localPath, String fileName) {
        SftpATTRS attrs = null;
        String remoteParentDir = remoteFiles.getParentDirectory(remoteFilePath);
        Path localFilePath = Path.of(localPath).toAbsolutePath().normalize().resolve(fileName);
        long localMTime = 0, remoteMTime = 0;
        try {
            sftpChannel.cd(remoteParentDir);
            attrs = sftpChannel.stat(fileName);
            localMTime = Files.getLastModifiedTime(localFilePath).to(TimeUnit.SECONDS);
            remoteMTime = attrs.getMTime();
        } catch (SftpException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: cd/stat error");
        } catch (IOException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: can't get LastModifiedTime");
        }
        boolean isRemoteFileNewer = (remoteMTime>localMTime ? true : false);
        System.out.println("REMOTE FILE = " + fileName + " IS NEWER = " + isRemoteFileNewer);
        return isRemoteFileNewer;
    }

}
