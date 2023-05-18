module rgr.server.rgr_javafx_jsch {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jsch;
    requires lombok;

    opens rgr.sshApp to javafx.graphics;
    exports rgr.sshApp.web;
    exports rgr.sshApp.controller;
    exports rgr.sshApp.model;
    opens rgr.sshApp.controller to javafx.fxml;
    exports rgr.sshApp.utils.files.panels;
    opens rgr.sshApp.utils.files.panels to javafx.fxml;
    exports rgr.sshApp.utils.files;
    opens rgr.sshApp.utils.files to javafx.base;
    exports rgr.sshApp.utils.files.handlers;
    opens rgr.sshApp.utils.files.handlers to javafx.base;
}