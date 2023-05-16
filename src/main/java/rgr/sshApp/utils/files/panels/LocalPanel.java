package rgr.sshApp.utils.files.panels;

import rgr.sshApp.utils.files.handlers.LocalFiles;

public class LocalPanel extends FilePanel {

    public LocalPanel() {
        super();
        this.fileHandler = new LocalFiles();
        initComboBox();
        updateTable(fileHandler.getInitialPath());
    }

}
