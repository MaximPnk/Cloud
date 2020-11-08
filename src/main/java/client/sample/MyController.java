package client.sample;

import client.service.Commands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class MyController {

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

    private String rootPath = "Client";
    private boolean worksWithServer = true;
    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private final Commands commands = new Commands();

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
            StringBuilder sb = new StringBuilder("");
            try {
                while (true) {
                    while (dis.available() > 0) {
                        sb.append((char) dis.read());
                    }
                    if (!sb.toString().equals("")) {
                        textArea.appendText(sb.toString());
                        sb = new StringBuilder("");
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
        }

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
                textArea.appendText(commands.getAnswer(command));
            }
        }
        textField.clear();
    }

    private void getFiles() {
        clientArea.appendText(String.join(System.lineSeparator(), Objects.requireNonNull(new File(rootPath).list())));

        StringBuilder files = new StringBuilder();
        try {
            dos.write("getfiles".getBytes());
            while (true) {
                if (dis.available() > 0) {
                    while (dis.available() > 0) {
                        files.append((char) dis.read());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverArea.appendText(files.toString());
    }
}
