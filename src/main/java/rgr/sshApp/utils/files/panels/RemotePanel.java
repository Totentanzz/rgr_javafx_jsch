package rgr.sshApp.utils.files.panels;


import com.jcraft.jsch.SftpException;
import rgr.sshApp.utils.files.handlers.RemoteFiles;
import rgr.sshApp.web.SecureFtpChannel;

public class RemotePanel extends FilePanel {

    public RemotePanel() {
        super();
        this.fileHandler = new RemoteFiles();
        initComboBox();
        try {
            updateTable(fileHandler.getInitialPath());
        } catch (SftpException exc) {
            FilePanel.notifyConnectionError();
        }
    }

    public void setChannels(SecureFtpChannel checkChannel, SecureFtpChannel fileListChannel) {
        ((RemoteFiles)fileHandler).setChannels(checkChannel,fileListChannel);
    }

    public RemoteFiles getRemoteFiles() {
        return ((RemoteFiles) fileHandler);
    }

}
