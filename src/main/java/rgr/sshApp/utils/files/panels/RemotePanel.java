package rgr.sshApp.utils.files.panels;


import com.jcraft.jsch.SftpException;
import rgr.sshApp.utils.files.handlers.RemoteFilesHandler;
import rgr.sshApp.web.SecureFtpChannel;

public class RemotePanel extends FilePanel {

    public RemotePanel() {
        super();
        this.fileHandler = new RemoteFilesHandler();
        initComboBox();
        try {
            updateTable(fileHandler.getInitialPath());
        } catch (SftpException exc) {
            FilePanel.notifyConnectionError();
        }
    }

    public void setChannels(SecureFtpChannel checkChannel, SecureFtpChannel fileListChannel) {
        ((RemoteFilesHandler)fileHandler).setChannels(checkChannel,fileListChannel);
    }

    public RemoteFilesHandler getRemoteFiles() {
        return ((RemoteFilesHandler) fileHandler);
    }

}
