package rgr.sshApp.web;

import com.jcraft.jsch.*;
import lombok.Data;

import java.io.*;
import java.util.Properties;

@Data
public class SecureShellSession {

    private final String username;
    private final String passowrd;
    private final String ipAddress;
    private final int port;
    private final static String sessionConfigFile = "src/main/resources/rgr/sshApp/configs/sessionConfig.properties";

    private Session session;
    private SecureFileTransferChannel constChannel;

    public void connect() throws JSchException {
        session = getNewSession();
        session.connect();
        constChannel = new SecureFileTransferChannel(session);
        constChannel.connect();
    }

    public void disconnect() {
        constChannel.disconnect();
        if (isEstablished()) session.disconnect();
    }

    public boolean isEstablished() {
        return session.isConnected();
    }

    public String executeCommand(String command) {
        ChannelExec channelExec = null;
        BufferedReader inputStreamReader=null, errInputStreamReader = null;
        StringBuilder responseBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            inputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            errInputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));
            readInputStreamLine(responseBuilder,inputStreamReader);
            readInputStreamLine(errorBuilder,errInputStreamReader);
        } catch (JSchException exc) {
            System.out.println("SecureShell.executeCommand: opening channel/trying connection error");
            exc.printStackTrace();
        } catch (IOException exc) {
            System.out.println("SecureShell.executeCommand: getting channel streams/reading from streams error");
            exc.printStackTrace();
        } finally {
            try {
                if (errInputStreamReader!=null) {
                    errInputStreamReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (channelExec != null) {
                    channelExec.disconnect();
                }
            } catch (IOException exc) {
                System.out.println("SecureShell.executeCommand: bufferedReader closing error");
                exc.printStackTrace();
            }
        }
        return responseBuilder.toString();
    }

//    public void uploadFile(String uploadingFilePath, String remoteDirectory) {
//        String fileName = uploadingFilePath.substring(uploadingFilePath.lastIndexOf("\\")+1);
//        try {
//            if (!isExisting(remoteDirectory,fileName)){
//                sftpUploadingChannel.put(uploadingFilePath, remoteDirectory);
//                System.out.println("Successful upload---------------------------------------------------------------------------------------------------------------------------");
//            }
//        } catch (SftpException exc) {
//            System.out.println("SecureShell.uploadFile: putting file error");
//            exc.printStackTrace();
//        }
//    }
//
//    public void downloadFile(String remoteFilePath, String localDirectory) {
//        try {
//            sftpDownloadingChannel.get(remoteFilePath, localDirectory);
//            System.out.println("Successful download");
//        } catch (SftpException exc) {
//            System.out.println("SecureShell.downloadFile: downloading file error");
//            exc.printStackTrace();
//        }
//    }
//
//    public void deleteFile(String remoteFilePath) {
//        try {
//            String[] fileCommandForm = FileInfo.toCommandsForm(remoteFilePath);
//            sftpRemovingChannel.cd(fileCommandForm[0]);
//            sftpRemovingChannel.rm(fileCommandForm[1]);
//            System.out.println("Successful deleting");
//        } catch (SftpException exc){
//            System.out.println("SecureShell.deleteFile: cd or rm commands error");
//            exc.printStackTrace();
//        }
//    }
//
//    public void changeDirectory(String remotePath) {
//        try {
//            sftpUploadingChannel.cd(remotePath);
//            System.out.println("Successful changed directory");
//        } catch (SftpException exc){
//            System.out.println("SecureShell.changeDirectory: cd command error");
//            exc.printStackTrace();
//        }
//    }
//
//    public String presentWorkingDirectory(){
//        String pwd = null;
//        try {
//            pwd = sftpLsChannel.pwd();
//            System.out.println("Successful got directory");
//        } catch (SftpException exc){
//            System.out.println("SecureShell.presentWorkingDirectory: pwd command error");
//            exc.printStackTrace();
//        }
//        return pwd;
//    }
//
//    public Vector<ChannelSftp.LsEntry> listDirectory(String remotePath) {
//        Vector<ChannelSftp.LsEntry> fileList = null;
//        try {
//            fileList = sftpLsChannel.ls(remotePath);
//            System.out.println("Successful listing of directory");
//        } catch (SftpException exc){
//            System.out.println("SecureShell.listDirectory: ls command error");
//            exc.printStackTrace();
//        }
//        return fileList;
//    }
//
//    public void makeDir(String folderName) {
//        try {
//            if (!isExisting(folderName)) {
//                sftpUploadingChannel.mkdir(folderName);
//                System.out.println("Successful folder creating------------------------------------------------------------------------------------------------------");
//            }
//        } catch (SftpException exc){
//            System.out.println("SecureShell.makeDir: mkdir command error");
//            exc.printStackTrace();
//        }
//    }
//
//    public void makeDir(String remotePath, String folderName) {
//        try {
//            sftpUploadingChannel.cd(remotePath);
//            makeDir(folderName);
//        } catch (SftpException exc) {
//            System.out.println("SecureShell.makeDir: mkdir/cd command error");
//            exc.printStackTrace();
//        }
//
//    }

    private Session getNewSession() {
        JSch jsCh = new JSch();
        Session session = null;
        try {
            session = jsCh.getSession(username, ipAddress, port);
            session.setPassword(passowrd);
            session.setConfig(loadSessionConfig());
            session.setTimeout(5000);
        } catch (JSchException exc) {
            System.out.println("SecureShellSession.getNewSession: invalid session params or timeout");
            exc.printStackTrace();
        }
        return session;
    }

    private void readInputStreamLine(StringBuilder stringBuilder,BufferedReader inputStreamReader) throws IOException {
        String line = null;
        while ((line = inputStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
    }

    private static Properties loadSessionConfig() {
        Properties sessionConfig = new Properties();
        try {
            sessionConfig.load(new FileInputStream(sessionConfigFile));
        } catch (IOException e) {
            sessionConfig.setProperty("PreferredAuthentications","password");
            sessionConfig.setProperty("StrictHostKeyChecking", "no");
            try {
                sessionConfig.store(new FileOutputStream(sessionConfigFile), null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return sessionConfig;
    }

//    private boolean isExisting(String fileName) {
//        SftpATTRS attrs = null;
//        boolean exisitng = false;
//        try {
//            attrs = sftpUploadingChannel.stat(fileName);
//        } catch (SftpException exc) {
//            System.out.println("SecureShell.isExisting: file doesn't exist");
//        }
//        exisitng = attrs != null;
//        return exisitng;
//    }
//
//    private boolean isExisting(String remotePath, String fileName) {
//        boolean exisitng = false;
//        try {
//            sftpUploadingChannel.cd(remotePath);
//            exisitng = isExisting(fileName);
//        } catch (SftpException exc) {
//            System.out.println("SecureShell.isExisting: cd/stat commands error");
//            exc.printStackTrace();
//        }
//        return exisitng;
//    }

}
