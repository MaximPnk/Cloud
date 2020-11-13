package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import operations.ClientCommands;
import service.Convert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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

    @FXML
    public TextField login;

    @FXML
    public TextField password;

    @FXML
    public Button authenticate;

    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private static boolean connected;
    private static final ClientCommands clientCommands = new ClientCommands();
    private static boolean auth;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
        setClickActions();

        //TODO add new stage for login

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
        int length = -1;
        byte command = -1;
        byte[] b = new byte[0];

        try {
            if (dis.available() > 0) {
                int lengthOfLengthInBytes = dis.readByte();
                byte[] lengthInBytes = new byte[lengthOfLengthInBytes];
                for (int i = 0; i < lengthOfLengthInBytes; i++) {
                    lengthInBytes[i] = dis.readByte();
                }

                length = Convert.bytesToInt(lengthInBytes);
                command = dis.readByte();

                b = new byte[length - 1];
                for (int i = 0; i < length - 1; i++) {
                    b[i] = dis.readByte();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (length != -1) {
            handleCommand(command, b);
        }
    }

    private void handleCommand(byte command, byte[] input) {

        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                clientCommands.downloadFile(input);
                break;
            case DOWNLOAD_COMPLETED:
                get.fire();
            case MKDIR:
            case TOUCH:
            case REMOVE:
            case LOG:
                clientCommands.log(Convert.bytesToStr(input));
                break;
            case GET:
                clientCommands.clientFiles();
                clientCommands.serverFiles(Convert.bytesToStr(input));
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
        clientCommands.help();

        upload.setOnAction(click -> {
            if (clientView.getSelectionModel().getSelectedItem() != null && clientView.getSelectionModel().getSelectedItem().contains(".")) {
                clientCommands.uploadFile(clientView.getSelectionModel().getSelectedItem()).forEach(this::sendMsg);
                logArea.appendText("Upload completed" + System.lineSeparator());
                get.fire();
            }
        });

        download.setOnAction(click -> {
            if (serverView.getSelectionModel().getSelectedItem() != null && serverView.getSelectionModel().getSelectedItem().contains(".")) {
                sendMsg(((char) Commands.DOWNLOAD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
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

        get.setOnAction(click -> sendMsg(Commands.GET.getBt()));

        clientView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && clientView.getSelectionModel().getSelectedItem() != null) {
                clientCommands.change(clientView.getSelectionModel().getSelectedItem());
                clientCommands.clientFiles();
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
            byte[] msgLengthInBytes = BigInteger.valueOf(msg.length).toByteArray();
            int lengthOfBytes = msgLengthInBytes.length;
            byte[] data = new byte[1 + msgLengthInBytes.length + msg.length];
            data[0] = (byte) lengthOfBytes;
            System.arraycopy(msgLengthInBytes, 0, data, 1, msgLengthInBytes.length);
            System.arraycopy(msg, 0, data, msgLengthInBytes.length + 1, msg.length);
            dos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(byte msg) {
        sendMsg(new byte[]{msg});
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
