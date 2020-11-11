package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import operations.ChangeDirectory;
import operations.GetFiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

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

    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private static boolean connected;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
        setClickActions();
        get.fire();

        Thread read = new Thread(() -> {
            while (connected) {
                readMsg();
            }
        });
        read.setDaemon(true);
        read.start();
    }

    private void readMsg() {
        StringBuilder sb = new StringBuilder();
        byte command = -1;

        try {
            while (dis.available() > 0) {
                command = dis.readByte();

                while (dis.available() > 0) {
                    sb.append((char) dis.readByte());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sb.length() > 0) {
            handleCommand(command, sb);
            sb = new StringBuilder();
        }
    }

    private void handleCommand(byte command, StringBuilder sb) {

        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                break;
            case UPLOAD:
                break;
            case MKDIR:
                break;
            case TOUCH:
                break;
            case REMOVE:
                break;
            case GET:
                GetFiles.clientFiles();
                GetFiles.serverFiles(sb.toString());
                break;
        }
    }

    private void connect() {
        connected = true;
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setClickActions() {
        mkdir.setOnAction(click -> {
            try {
                dos.write(Commands.MKDIR.getBt());
                dos.write("some text".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        get.setOnAction(click -> {
            try {
                dos.write(Commands.GET.getBt());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        clientView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                ChangeDirectory.change(clientView.getSelectionModel().getSelectedItem());
                get.fire();
            }
        });
        serverView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                try {
                    dos.write(((char) Commands.CD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            get.fire();
        });
    }

    public static void closeConnection() {
        connected = false;

        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
