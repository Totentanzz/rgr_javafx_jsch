package rgr.sshApp.web;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
    private SecureFtpChannel checkingChannel;
    private SecureFtpChannel fileListChannel;

    public boolean isEstablished() {
        return session.isConnected();
    }

    public void connect() throws JSchException {
        session = getNewSession();
        session.connect();
        checkingChannel = new SecureFtpChannel(session);
        checkingChannel.connect();
        fileListChannel = new SecureFtpChannel(session);
        fileListChannel.connect();
    }

    public void disconnect() {
        checkingChannel.disconnect();
        fileListChannel.disconnect();
        if (isEstablished()) session.disconnect();
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
            }
        }
        return (!responseBuilder.toString().isEmpty() ? responseBuilder.toString() : errorBuilder.toString());
    }

    private Session getNewSession() {
        JSch jsCh = new JSch();
        Session session = null;
        try {
            session = jsCh.getSession(username, ipAddress, port);
            session.setPassword(passowrd);
            session.setConfig(loadSessionConfig());
            session.setTimeout(5000);
        } catch (JSchException | IOException exc) {
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

    private static Properties loadSessionConfig() throws IOException {
        Properties sessionConfig = new Properties();
        sessionConfig.load(new FileInputStream(sessionConfigFile));
        sessionConfig.setProperty("PreferredAuthentications","password");
        sessionConfig.setProperty("StrictHostKeyChecking", "no");
        sessionConfig.store(new FileOutputStream(sessionConfigFile), null);
        return sessionConfig;
    }

}
