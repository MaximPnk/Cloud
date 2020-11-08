package client.sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Window extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("sample.fxml")));
        primaryStage.setTitle("File Commander");
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(300);
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> Controller.closeConnection());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
