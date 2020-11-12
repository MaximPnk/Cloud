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

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
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

        upload.setOnAction(click -> {
            if (clientView.getSelectionModel().getSelectedItem() != null && clientView.getSelectionModel().getSelectedItem().contains(".")) {
                uploadFile(clientView.getSelectionModel().getSelectedItem());
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

    private void uploadFile(String selectedItem) {
        try {

            File file = new File(ChangeDirectory.getRootPath() + "/" + selectedItem);
            DataInputStream uploadDis = new DataInputStream(new FileInputStream(file));
            byte[] data = new byte[(int) file.length()];
            int size = uploadDis.read(data);
            byte[] send = new byte[9900];
            send[0] = Commands.UPLOAD.getBt();
            send[1] = (byte) selectedItem.length();
            System.arraycopy(selectedItem.getBytes(), 0, send, 2, selectedItem.getBytes().length);
            int start = 2 + selectedItem.getBytes().length;
            int freeSpace = 9900 - start;

            File file1 = new File("Client/Storage/copy_" + selectedItem);
            file1.createNewFile();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file1));

            for (int i = 0; i + freeSpace - 1 < size; i += freeSpace) {
                System.arraycopy(data, i, send, start, Math.min(freeSpace, size - i));
//                sendMsg(send);
                byte[] arr = new byte[send.length - start];
                if (arr.length >= 0) System.arraycopy(send, start, arr, 0, arr.length);
                dos.write(arr);
            }

            uploadDis.close();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(byte[] msg) {
        try {
            System.out.println(msg.length);
            byte[] msgLengthInBytes = BigInteger.valueOf(msg.length).toByteArray();
            int lengthOfBytes = msgLengthInBytes.length;
            byte[] data = new byte[1 + msgLengthInBytes.length + msg.length];
            data[0] = (byte) lengthOfBytes;
            System.arraycopy(msgLengthInBytes, 0, data, 1, msgLengthInBytes.length);
            System.arraycopy(msg, 0, data, msgLengthInBytes.length + 1, msg.length);
            System.out.println(Arrays.toString(data));
            dos.write(data);
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
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
