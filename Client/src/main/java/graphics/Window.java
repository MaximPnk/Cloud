package graphics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.ServerConnection;

import java.io.IOException;

public class Window extends Application {

    static FXMLLoader loader;
    static Parent parent;
    static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        loader = new FXMLLoader(getClass().getClassLoader().getResource("AuthWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent = root;
        stage = primaryStage;

        stage.setTitle("File Commander");
        stage.setScene(new Scene(root, 350, 170));
        stage.setOnCloseRequest(event -> ServerConnection.closeConnection());
        stage.show();
    }

    public static void loginPage() {
        loader = new FXMLLoader(Window.class.getClassLoader().getResource("AuthWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent = root;
        Parent finalRoot = root;
        Platform.runLater(() -> {
            stage.setScene(new Scene(finalRoot, 350, 170));
            stage.show();
        });
    }

    public static void regPage() {
        loader = new FXMLLoader(Window.class.getClassLoader().getResource("RegWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent = root;

        Parent finalRoot = root;
        Platform.runLater(() -> {
            stage.setScene(new Scene(finalRoot, 400, 200));
            stage.show();
        });
    }

    public static void clientPage() {
        loader = new FXMLLoader(Window.class.getClassLoader().getResource("ClientWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent = root;
        Parent finalRoot = root;
        Platform.runLater(() -> {
            stage.setScene(new Scene(finalRoot, 700, 700));
            stage.centerOnScreen();
            stage.sizeToScene();
            stage.show();
        });
    }




    public static void main(String[] args) {
        launch(args);
    }

    public static ClientController getClientController() {
        return loader.getController();
    }

    public static AuthController getAuthController() {
        return loader.getController();
    }

    public static RegController getRegController() {
        return loader.getController();
    }
}
