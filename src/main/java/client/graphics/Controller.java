package client.graphics;

import client.service.ClientCommands;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class Controller {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button sendButton;

    @FXML
    TextField clientPath;

    @FXML
    ListView<String> clientView;

    @FXML
    TextField serverPath;

    @FXML
    ListView<String> serverView;

    @FXML
    VBox clientSide;

    @FXML
    VBox serverSide;

    @FXML
    Button mkdir;

    @FXML
    Button touch;

    @FXML
    Button remove;

    @FXML
    Button upload;

    @FXML
    Button download;

    private boolean worksWithServer = true;
    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private final ClientCommands clientCommands = new ClientCommands();
    private static boolean isConnected = true;

    public void initialize() {

        setActions();

        serverSide.setStyle("-fx-border-color: FF0000");

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
                            String[] files = answer.replace(System.lineSeparator(), "|").replaceAll(".*FILES START", "")
                                    .replaceAll("FILES END.*", "").replace("|", System.lineSeparator()).split(" ");
                            serverPath.setText(files[0]);
                            files[0] = "..";
                            Platform.runLater(() -> serverView.setItems(FXCollections.observableArrayList(files)));
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

    private void setActions() {
        clientView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                clientCommands.getAnswer("cd " + clientView.getSelectionModel().getSelectedItem());
                getFiles();
            }
        });
        serverView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                try {
                    dos.write(("cd " + serverView.getSelectionModel().getSelectedItem()).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getFiles();
            }
        });
        //TODO сделать вылетающее окно для названия
        mkdir.setOnAction(click -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Some title");
            dialog.setHeaderText("Enter directory name:");
            dialog.getDialogPane().setHeader(null);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                try {
                    dos.write(("mkdir " + s).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            getFiles();
        });
        touch.setOnAction(click -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Some title");
            dialog.setHeaderText("Enter file name:");
            dialog.getDialogPane().setHeader(null);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                try {
                    dos.write(("touch " + s).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            getFiles();
        });
        remove.setOnAction(click -> {
            try {
                dos.write(("rm " + serverView.getSelectionModel().getSelectedItem()).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            getFiles();
        });
        upload.setOnAction(click -> {
            //TODO upload file
            uploadFile(clientView.getSelectionModel().getSelectedItem());
        });
        download.setOnAction(click -> {
            //TODO download file
        });
    }

    private void uploadFile(String selectedItem) {
        try {
            dos.write(("upload " + selectedItem).getBytes());
            FileInputStream fos = new FileInputStream(new File(clientCommands.getRootPath() + "/" + selectedItem));
            while (fos.available() > 0) {
                dos.write(fos.read());
            }
            dos.write(("END OF FILE").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickAction(ActionEvent actionEvent) {

        if (textField.getText().equals("swap")) {
            worksWithServer = !worksWithServer;
            if (worksWithServer) {
                serverSide.setStyle("-fx-border-color: FF0000");
                clientSide.setStyle("-fx-border-color: FFFFFF");
            } else {
                clientSide.setStyle("-fx-border-color: FF0000");
                serverSide.setStyle("-fx-border-color: FFFFFF");
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
        String[] files = clientCommands.getAnswer("getfiles").split(" ");
        clientPath.setText(files[0]);
        files[0] = "..";
        clientView.setItems(FXCollections.observableArrayList(files));
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
