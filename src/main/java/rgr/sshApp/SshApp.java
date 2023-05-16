package rgr.sshApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SshApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SshApp.class.getResource("view/loginView.fxml"));
        stage = fxmlLoader.load();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}