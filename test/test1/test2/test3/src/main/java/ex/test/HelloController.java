package ex.test;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class HelloController implements Initializable {
    @FXML
    private Button button;
    @FXML
    private AnchorPane ppp;

    final Color startColor = Color.web("#e08090");
    final Color endColor = Color.web("#80e090");
    final ObjectProperty<Color> color = new SimpleObjectProperty<Color>(startColor);

    final StringBinding cssColorSpec = Bindings.createStringBinding(new Callable<String>() {
        @Override
        public String call() throws Exception {
            return String.format("-fx-body-color: rgb(%d, %d, %d);",
                    (int) (256*color.get().getRed()),
                    (int) (256*color.get().getGreen()),
                    (int) (256*color.get().getBlue()));
        }
    }, color);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        StringProperty backgroundColor = new SimpleStringProperty("-fx-background-color: grey; ");
//        StringProperty textColor = new SimpleStringProperty("-fx-text-fill: white; ");
//        StringBinding color = (StringBinding) backgroundColor.concat(textColor);
//        button.styleProperty().bind(color);
//        System.out.println(button.styleProperty().getValue());
//        button.setOnAction(actionEvent -> {
//            backgroundColor.setValue(backgroundColor.getValue().replace("grey","green"));
//            System.out.println(button.styleProperty().getValue());
//        });
//

        button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14pt;");
        button.setPrefSize(120, 60);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetX(0);
        innerShadow.setOffsetY(0);
        innerShadow.setColor(Color.web("#4CAF50"));
        innerShadow.setWidth(0);
        innerShadow.setHeight(0);
        button.setEffect(innerShadow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.widthProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.widthProperty(), button.getPrefWidth())),
                new KeyFrame(Duration.seconds(2), new KeyValue(innerShadow.heightProperty(), button.getPrefHeight()))
        );

        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}