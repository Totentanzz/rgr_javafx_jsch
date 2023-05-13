package rgr.sshApp.web;

import com.jcraft.jsch.*;

import java.util.Vector;

public class SecureFileTransferChannel {

    ChannelSftp sftpChannel;

    public SecureFileTransferChannel(Session session) {
        try {
            this.sftpChannel = (ChannelSftp) session.openChannel("sftp");
            this.sftpChannel.setBulkRequests(32);
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
        char separatorChar = uploadingFilePath.contains("/") ? '/' : '\\';
        String fileName = uploadingFilePath.substring(uploadingFilePath.lastIndexOf(separatorChar)+1);
        try {
            if (!isExisting(remoteDirectory,fileName)){
                sftpChannel.put(uploadingFilePath, remoteDirectory);
                System.out.println("UPLOADING FILE = " + fileName + " HAS FINISHED");
            }
        } catch (SftpException exc) {
            System.out.println("SecureShell.uploadFile: putting file error");
            exc.printStackTrace();
        }
    }

    public void downloadFile(String remoteFilePath, String localDirectory) {
        try {
            sftpChannel.get(remoteFilePath, localDirectory);
            System.out.println("DOWNLOADING FILE FROM " + remoteFilePath + " TO " + localDirectory + " HAS FINISHED");
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

    public void makeDir(String folderName) {
        try {
            if (!isExisting(folderName)) {
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
            sftpChannel.cd(remotePath);
            makeDir(folderName);
            System.out.println("CREATING IN DIR = " + remotePath + " FOLDER = " + folderName + " HAS FINISHED");
        } catch (SftpException exc) {
            System.out.println("SecureShell.makeDir: mkdir/cd command error");
            exc.printStackTrace();
        }
    }

    private boolean isExisting(String fileName) {
        SftpATTRS attrs = null;
        boolean existing;
        try {
            attrs = sftpChannel.stat(fileName);
        } catch (SftpException exc) {
            System.out.println("SecureShell.isExisting: file doesn't exist");
        }
        existing = (attrs != null);
        System.out.println("FILE IS EXISTING = " + existing);
        return existing;
    }

    private boolean isExisting(String remotePath, String fileName) {
        boolean existing = false;
        try {
            sftpChannel.cd(remotePath);
            existing = isExisting(fileName);
        } catch (SftpException exc) {
            System.out.println("SecureShell.isExisting: cd/stat commands error");
            exc.printStackTrace();
        }
        return existing;
    }

}
