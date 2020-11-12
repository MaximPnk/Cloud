package operations;

import graphics.Window;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.io.File;

public class GetFiles {

    public static void serverFiles(String list) {
        String[] files = list.split(" ");
        Platform.runLater(() -> {
            Window.getController().serverPath.setText(files[0]);
            files[0] = "..";
            Window.getController().serverView.setItems(FXCollections.observableArrayList(files));
        });
    }

    public static void clientFiles() {
        String[] files = new File(ChangeDirectory.getRootPath()).list();
        String[] arr = (".. " + String.join(" ", files)).split(" ");
        Platform.runLater(() -> {
            Window.getController().clientPath.setText(ChangeDirectory.getRootPath());
            Window.getController().clientView.setItems(FXCollections.observableArrayList(arr));
        });
    }
}
