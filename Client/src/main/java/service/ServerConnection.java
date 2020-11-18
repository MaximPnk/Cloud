package service;

import graphics.Window;
import javafx.application.Platform;
import operations.ClientCommands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;

public class ServerConnection {

    private static ServerConnection instance;

    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private static boolean connected;
    private static final ClientCommands clientCommands = ClientCommands.getInstance();

    private ServerConnection() {
        connect();

        Thread read = new Thread(() -> {
            while (connected) {
                readMsg();
            }
        });
        read.setDaemon(true);
        read.start();
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
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
            System.out.println(Convert.bytesToStr(b));
        }
    }

    private void handleCommand(byte command, byte[] input) {

        switch (Commands.getCommand(command)) {
            case REG:
                if (Convert.bytesToStr(input).equals("SUCCESS")) {
                    Platform.runLater(() -> Window.getRegController().info.setText("Registration successful"));
                } else {
                    Platform.runLater(() -> Window.getRegController().info.setText("Login already exists"));
                }
                break;
            case AUTH:
                if (Convert.bytesToStr(input).equals("SUCCESS")) {
                    Window.clientPage();
                    clientCommands.log("Welcome to File Commander");
                } else {
                    Platform.runLater(() -> Window.getAuthController().info.setText("Incorrect login or password"));
                }
                break;
            case DOWNLOAD:
                clientCommands.downloadFile(input);
                break;
            case DOWNLOAD_COMPLETED:
                Window.getClientController().get.fire();
            case MKDIR:
            case TOUCH:
            case REMOVE:
            case LOG:
            case CD:
                System.out.println("LOG");
                clientCommands.log(Convert.bytesToStr(input));
                break;
            case GET:
                clientCommands.clientFiles();
                clientCommands.serverFiles(Convert.bytesToStr(input));
                break;
        }
    }

    public void sendMsg(byte[] msg) {
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

    public void sendMsg(byte msg) {
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
