package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import operations.ChangeDirectory;
import operations.ClientLog;
import operations.GetFiles;
import operations.Help;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
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

    @FXML
    public Button help;

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
        int length;
        byte command = -1;

        try {
            if (dis.available() > 0) {
                length = dis.readByte();
                command = dis.readByte();

                for (int i = 0; i < length; i++) {
                    sb.append((char) dis.readByte());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sb.length() > 0) {
            handleCommand(command, sb.toString());
        }
    }

    private void handleCommand(byte command, String input) {

        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                break;
            case UPLOAD:
                break;
            case MKDIR:
            case TOUCH:
            case REMOVE:
            case LOG:
                ClientLog.log(input);
                break;
            case GET:
                GetFiles.clientFiles();
                GetFiles.serverFiles(input);
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
        Help.help();
        mkdir.setOnAction(click -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create new directory");
            dialog.setHeaderText("Enter directory name");
            dialog.getDialogPane().setHeader(null);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (name.matches("^[\\w]+$")) {
                    sendMsg(((char) Commands.MKDIR.getBt() + name).getBytes());
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
                    sendMsg(((char) Commands.TOUCH.getBt() + name).getBytes());
                } else {
                    logArea.appendText("Invalid file name" + System.lineSeparator());
                }
            });
            get.fire();
        });
        remove.setOnAction(click -> {
            if (serverView.getSelectionModel().getSelectedItem() != null) {
                sendMsg(((char) Commands.REMOVE.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
            }
            get.fire();
        });
        get.setOnAction(click -> {
            sendMsg(Commands.GET.getBt());
        });
        clientView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && clientView.getSelectionModel().getSelectedItem() != null) {
                ChangeDirectory.change(clientView.getSelectionModel().getSelectedItem());
                GetFiles.clientFiles();
            }
        });
        serverView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && serverView.getSelectionModel().getSelectedItem() != null) {
                sendMsg(((char) Commands.CD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
                sendMsg(Commands.GET.getBt());
            }
        });
    }

    private void sendMsg(byte[] msg) {
        try {
            if (msg.length > 255) {
                logArea.appendText("TOO MUCH BYTES, NEED TO FIX IT");
            } else {
                dos.write(msg.length);
                dos.write(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(byte msg) {
        try {
            dos.write(1);
            dos.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
