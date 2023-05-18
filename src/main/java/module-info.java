module rgr.javafx.jsch {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires lombok;
    requires jsch;

    exports rgr.sshApp;
    exports rgr.sshApp.controller;
    exports rgr.sshApp.utils.files.panels;
    exports rgr.sshApp.model;
    exports rgr.sshApp.web;
    opens rgr.sshApp.controller;
    opens rgr.sshApp.utils.files.panels;
    opens rgr.sshApp.utils.files;
}