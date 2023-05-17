package rgr.sshApp.utils.files.panels;

import lombok.SneakyThrows;
import rgr.sshApp.utils.files.handlers.LocalFiles;

public class LocalPanel extends FilePanel {

    @SneakyThrows
    public LocalPanel() {
        super();
        this.fileHandler = new LocalFiles();
        initComboBox();
        updateTable(fileHandler.getInitialPath());
    }

}
