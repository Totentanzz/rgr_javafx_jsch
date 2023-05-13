module rgr.server.rgr_javafx_jsch {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jsch;
    requires lombok;



    //exports rgr.sshApp;
    opens rgr.sshApp.controller to javafx.fxml;
    opens rgr.sshApp to javafx.graphics;
    exports rgr.sshApp.utils;
    opens rgr.sshApp.utils to javafx.fxml;
}