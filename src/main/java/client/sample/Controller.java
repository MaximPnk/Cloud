package client.sample;

import client.service.ClientCommands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button sendButton;

    @FXML
    TextArea serverArea;

    @FXML
    TextArea clientArea;

    private boolean worksWithServer = true;
    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private final ClientCommands clientCommands = new ClientCommands();
    private static boolean isConnected = true;

    public void initialize() {

        serverArea.setStyle("-fx-border-color: FF0000");

        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getFiles();

        Thread reading = new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                while (isConnected) {
                    while (dis.available() > 0) {
                        sb.append((char) dis.read());
                    }
                    if (sb.length() > 0) {
                        String answer = sb.toString();
                        if (answer.contains("FILES START")) {
                            serverArea.appendText(answer.replace(System.lineSeparator(), "|").replaceAll(".*FILES START", "")
                                    .replaceAll("FILES END.*", "").replace("|", System.lineSeparator()));
                            textArea.appendText(answer.replace(System.lineSeparator(), "|").replaceAll("FILES START.*FILES END", "")
                                    .replace("|", System.lineSeparator()).replaceAll("^\\r\\n", ""));
                        } else {
                            textArea.appendText(answer.replaceAll("^\\r\\n", ""));
                        }
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        reading.setDaemon(true);
        reading.start();
    }

    public void clickAction(ActionEvent actionEvent) {

        if (textField.getText().equals("swap")) {
            worksWithServer = !worksWithServer;
            if (worksWithServer) {
                serverArea.setStyle("-fx-border-color: FF0000");
                clientArea.setStyle("-fx-border-color: FFFFFF");
            } else {
                clientArea.setStyle("-fx-border-color: FF0000");
                serverArea.setStyle("-fx-border-color: FFFFFF");
            }
        } else {
            if (worksWithServer) {
                if (!textField.getText().trim().isEmpty()) {
                    if (socket == null || socket.isClosed()) {
                        initialize();
                    }
                    try {
                        dos.write(textField.getText().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (!textField.getText().trim().isEmpty()) {
                    String command = textField.getText();
                    textArea.appendText(clientCommands.getAnswer(command) + System.lineSeparator());
                }
            }
            getFiles();
        }
        textField.clear();
    }

    private void getFiles() {
        clientArea.clear();
        serverArea.clear();

        clientArea.appendText(clientCommands.getAnswer("getfiles"));
        try {
            dos.write("getfiles".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        isConnected = false;

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
