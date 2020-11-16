package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import operations.ClientCommands;
import service.Convert;
import service.ServerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    public TextField clientPath;

    @FXML
    public ListView<String> clientView;

    @FXML
    public TextField serverPath;

    @FXML
    public ListView<String> serverView;

    @FXML
    public VBox clientSide;

    @FXML
    public VBox serverSide;

    @FXML
    public Button mkdir;

    @FXML
    public Button touch;

    @FXML
    public Button remove;

    @FXML
    public Button upload;

    @FXML
    public Button download;

    @FXML
    public Button get;

    @FXML
    public TextArea logArea;

    @FXML
    public Button help;

    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final ClientCommands clientCommands = ClientCommands.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setClickActions();
        get.fire();
    }

    private void setClickActions() {
        clientCommands.help();

        upload.setOnAction(click -> {
            if (clientView.getSelectionModel().getSelectedItem() != null && clientView.getSelectionModel().getSelectedItem().contains(".")) {
                clientCommands.uploadFile(clientView.getSelectionModel().getSelectedItem()).forEach(connection::sendMsg);
                logArea.appendText("Upload completed" + System.lineSeparator());
                get.fire();
            }
        });

        download.setOnAction(click -> {
            if (serverView.getSelectionModel().getSelectedItem() != null && serverView.getSelectionModel().getSelectedItem().contains(".")) {
                connection.sendMsg(((char) Commands.DOWNLOAD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
            }
        });

        mkdir.setOnAction(click -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create new directory");
            dialog.setHeaderText("Enter directory name");
            dialog.getDialogPane().setHeader(null);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (name.matches("^[\\w]+$")) {
                    connection.sendMsg(((char) Commands.MKDIR.getBt() + name).getBytes());
                } else {
                    logArea.appendText("Invalid directory name" + System.lineSeparator());
                }
            });
            get.fire();
        });

        touch.setOnAction(click -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Make new file");
            dialog.setHeaderText("Enter file name");
            dialog.getDialogPane().setHeader(null);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (name.matches("^[\\w]+\\.[a-zA-Z]+$")) {
                    connection.sendMsg(((char) Commands.TOUCH.getBt() + name).getBytes());
                } else {
                    logArea.appendText("Invalid file name" + System.lineSeparator());
                }
            });
            get.fire();
        });

        remove.setOnAction(click -> {
            if (serverView.getSelectionModel().getSelectedItem() != null) {
                connection.sendMsg(((char) Commands.REMOVE.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
            }
            get.fire();
        });

        get.setOnAction(click -> connection.sendMsg(Commands.GET.getBt()));

        clientView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && clientView.getSelectionModel().getSelectedItem() != null) {
                clientCommands.change(clientView.getSelectionModel().getSelectedItem());
                clientCommands.clientFiles();
            }
        });

        serverView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && serverView.getSelectionModel().getSelectedItem() != null) {
                connection.sendMsg(((char) Commands.CD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
                connection.sendMsg(Commands.GET.getBt());
            }
        });
    }
}
