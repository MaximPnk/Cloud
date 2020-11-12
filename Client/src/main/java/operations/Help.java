package operations;

import graphics.Window;
import javafx.scene.control.Alert;

public class Help {

    public static void help() {
        Window.getController().help.setOnAction(click -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Help");
            alert.setHeaderText(null);
            alert.getDialogPane().setPrefWidth(600);
            alert.setContentText(
                    "• Change directory: double click on a folder or \"..\" to return to the parent directory [Client/Server]" + System.lineSeparator() + System.lineSeparator() +
                    "• Download: select file in the server window and click the download button [Server]" + System.lineSeparator() + System.lineSeparator() +
                    "• Upload: select file in the client window and click the upload button [Client]" + System.lineSeparator() + System.lineSeparator() +
                    "• Get: click for update file lists [Client/Server]" + System.lineSeparator() + System.lineSeparator() +
                    "• Make directory: select server path and click mkdir button [Server]" + System.lineSeparator() + System.lineSeparator() +
                    "• Create file: select server path and click touch button [Server]" + System.lineSeparator() + System.lineSeparator() +
                    "• Remove: select file in the server window and click remove button [Server]" + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() +
                    "• Author Pankov Maxim © 2020");
            alert.showAndWait();
        });
    }
}
