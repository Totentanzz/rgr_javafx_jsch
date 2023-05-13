module com.example.test {
    requires javafx.controls;
    requires javafx.fxml;


    opens ex.test to javafx.fxml;
    exports ex.test;
    exports ex.test.model;
    opens ex.test.model to javafx.fxml;
}