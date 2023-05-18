package rgr.sshApp.utils.files.panels;

import lombok.SneakyThrows;
import rgr.sshApp.utils.files.handlers.LocalFilesHandler;

public class LocalPanel extends FilePanel {

    @SneakyThrows
    public LocalPanel() {
        super();
        this.fileHandler = new LocalFilesHandler();
        initComboBox();
        updateTable(fileHandler.getInitialPath());
    }

}
