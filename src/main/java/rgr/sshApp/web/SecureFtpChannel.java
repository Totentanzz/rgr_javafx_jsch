package rgr.sshApp.web;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpATTRS;

import rgr.sshApp.utils.files.handlers.LocalFilesHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class SecureFtpChannel {

    private ChannelSftp sftpChannel;
    private LocalFilesHandler localFiles;

    public SecureFtpChannel(Session session) throws JSchException {
        this.sftpChannel = (ChannelSftp) session.openChannel("sftp");
        this.sftpChannel.setBulkRequests(32);
        this.localFiles = new LocalFilesHandler();
    }

    public void connect() throws JSchException {
        sftpChannel.connect();
    }

    public void disconnect() {
        if (sftpChannel!=null && sftpChannel.isConnected()) sftpChannel.disconnect();
    }

    public boolean isConnected() {
        boolean state = false;
        if (sftpChannel!=null) {
            state = sftpChannel.isConnected();
        }
        return state;
    }

    public void uploadFile(String localFilePath, String remoteDir) throws SftpException {
        String fileName = localFiles.getFileName(localFilePath);
        String remoteFilePath = localFiles.getResolvedDirectory(remoteDir, fileName);
        String localDir = localFiles.getParentDirectory(localFilePath);
        if (!isExists(remoteDir,fileName) || isRemoteFileNewer(remoteFilePath,localDir,fileName)<0) {
            sftpChannel.put(localFilePath, remoteDir);
            System.out.println("UPLOADING FILE = " + fileName + " HAS FINISHED");
        }
    }

    public void downloadFile(String remoteFilePath, String localDir) throws SftpException {
        String fileName = localFiles.getFileName(remoteFilePath);
        if (!localFiles.isExists(localDir,fileName) || isRemoteFileNewer(remoteFilePath, localDir,fileName)>0) {
            sftpChannel.get(remoteFilePath, localDir);
            System.out.println("DOWNLOADING FILE FROM " + remoteFilePath + " TO " + localDir + " HAS FINISHED");
        }
    }

    public void deleteFile(String remoteDir, String fileName) throws SftpException {
        if (isExists(remoteDir,fileName)) {
            changeDirectory(remoteDir);
            sftpChannel.rm(fileName);
            System.out.println("DELETING FILE = " + fileName + " IN " + remoteDir + " HAS FINISHED");
        }
    }

    public void changeDirectory(String remoteDir) throws SftpException {
        sftpChannel.cd(remoteDir);
        System.out.println("CHANGING DIRECTORY TO = " + remoteDir + " HAS FINISHED");
    }

    public String presentWorkingDirectory() throws SftpException {
        String pwd = sftpChannel.pwd();
        System.out.println("GETTING CURRENT PWD = " + pwd + " HAS FINISHED");
        return pwd;
    }

    public Vector<ChannelSftp.LsEntry> listDirectory(String remoteDir) throws SftpException {
        Vector<ChannelSftp.LsEntry> fileList = sftpChannel.ls(remoteDir);
        System.out.println("GETTING FILE LIST HAS FINISHED");
        return fileList;
    }

    public void makeDir(String remoteFilePath) throws SftpException {
        String parentDir = localFiles.getParentDirectory(remoteFilePath);
        String folderName = localFiles.getFileName(remoteFilePath);
        if (!isExists(parentDir,folderName)) {
            sftpChannel.mkdir(folderName);
            System.out.println("CREATING FOLDER IN CURRENT DIR = " + folderName + " HAS FINISHED");
        }
    }

    public void makeDir(String remotePath, String folderName) throws SftpException {
        if (!isExists(remotePath,folderName)) {
            changeDirectory(remotePath);
            sftpChannel.mkdir(folderName);
            System.out.println("CREATING IN DIR = " + remotePath + " FOLDER = " + folderName + " HAS FINISHED");
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

    public boolean isExists(String path, String fileName) throws SftpException {
        changeDirectory(path);
        SftpATTRS attrs = getAttrs(fileName);
        boolean existing = (attrs != null);
        System.out.println("FILE IS EXISTING = " + existing);
        return existing;
    }

    public boolean isDir(String path, String fileName) throws SftpException {
        changeDirectory(path);
        SftpATTRS attrs = getAttrs(fileName);
        boolean isDir = attrs.isDir();
        System.out.println("FILE IS DIR = " + isDir);
        return isDir;
    }

    public long isRemoteFileNewer(String remoteFilePath, String localDir, String fileName) {
        SftpATTRS attrs = null;
        String remoteParentDir = localFiles.getParentDirectory(remoteFilePath);
        Path localFilePath = Path.of(localDir).toAbsolutePath().normalize().resolve(fileName);
        long localMTime = 0, remoteMTime = 0;
        try {
            changeDirectory(remoteParentDir);
            attrs = getAttrs(fileName);
            localMTime = Files.getLastModifiedTime(localFilePath).to(TimeUnit.SECONDS);
            remoteMTime = attrs.getMTime();
        } catch (IOException | SftpException exc) {
            System.out.println("SecureShell.isRemoteFileNewer: can't get LastModifiedTime");
        }
        long state = remoteMTime - localMTime;
        System.out.println("REMOTE FILE = " + fileName + " IS NEWER = " + state);
        return state;
    }

}
