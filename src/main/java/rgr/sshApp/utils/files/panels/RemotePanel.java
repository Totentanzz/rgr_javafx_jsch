package rgr.sshApp.utils.files.panels;


import rgr.sshApp.utils.files.handlers.RemoteFiles;
import rgr.sshApp.web.SecureFtpChannel;

public class RemotePanel extends FilePanel {

    public RemotePanel() {
        super();
        this.fileHandler = new RemoteFiles();
        initComboBox();
        updateTable(fileHandler.getInitialPath());
    }

    public void setChannels(SecureFtpChannel checkChannel, SecureFtpChannel fileListChannel) {
        ((RemoteFiles)fileHandler).setChannels(checkChannel,fileListChannel);
    }

    public RemoteFiles getRemoteFiles() {
        return ((RemoteFiles) fileHandler);
    }

}
