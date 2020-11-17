package operations;

import commands.Commands;
import graphics.Window;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import service.Convert;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ClientCommands {

    private static ClientCommands instance;

    private ClientCommands() {
    }

    public static synchronized ClientCommands getInstance() {
        if (instance == null) {
            instance = new ClientCommands();
        }
        return instance;
    }

    private String rootPath = new File("Client/Storage").getAbsolutePath().replace("\\", "/");

    public void change(String selectedItem) {
        if (rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }
        if (selectedItem.equals("..") && !rootPath.equals("C:")) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        } else if (!selectedItem.contains(".")) {
            rootPath = rootPath + "/" + selectedItem;
        }
        rootPath = rootPath.concat("/");
    }

    public void log(String str) {
        Window.getClientController().logArea.appendText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ": " + str + System.lineSeparator());
    }

    public void serverFiles(String text) {
        String[] files = text.split("#");
        String[] arr = Arrays.copyOfRange(files, 1, files.length);
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("..");
        list.addAll(Arrays.stream(arr).sorted(Comparator.comparingInt(fl -> fl.indexOf("."))).toArray(String[]::new));
        Platform.runLater(() -> {
            Window.getClientController().serverPath.setText(files[0]);
            Window.getClientController().serverView.setItems(FXCollections.observableArrayList(list));
        });
    }

    public void clientFiles() {
        String[] files = new File(rootPath).list();
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("..");
        if (files != null) {
            list.addAll(Arrays.stream(files).sorted(Comparator.comparingInt(fl -> fl.indexOf("."))).toArray(String[]::new));
        }
        Platform.runLater(() -> {
            Window.getClientController().clientPath.setText(rootPath);
            Window.getClientController().clientView.setItems(FXCollections.observableArrayList(list));
        });
    }

    public void help() {
        Window.getClientController().help.setOnAction(click -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Help");
            alert.setHeaderText(null);
            alert.getDialogPane().setPrefWidth(600);
            alert.setContentText(
                    "• Change directory: double click on a folder or \"..\" to return to the parent directory" + System.lineSeparator() + System.lineSeparator() +
                            "• Download: select file on the server window and click the download button" + System.lineSeparator() + System.lineSeparator() +
                            "• Upload: select file in the client window and click the upload button" + System.lineSeparator() + System.lineSeparator() +
                            "• Update: click for update file lists" + System.lineSeparator() + System.lineSeparator() +
                            "• Make directory: right click on each window" + System.lineSeparator() + System.lineSeparator() +
                            "• Remove: right click on each window");
            alert.showAndWait();
        });
    }

    public List<byte []> uploadFile(String fileName) {
        List<byte []> list = new ArrayList<>();

        try {
            File file = new File(rootPath + "/" + fileName);
            DataInputStream uploadDis = new DataInputStream(new FileInputStream(file));
            byte[] data = new byte[(int) file.length()];
            int size = uploadDis.read(data);
            byte[] beginArray = new byte[2 + fileName.getBytes().length];
            beginArray[0] = Commands.UPLOAD.getBt();
            beginArray[1] = (byte) fileName.length();
            System.arraycopy(fileName.getBytes(), 0, beginArray, 2, fileName.getBytes().length);
            int start = 2 + fileName.getBytes().length;
            int freeSpace = 49996 - start;

            for (int i = 0; i < size; i += freeSpace) {
                byte[] send = new byte[Math.min(freeSpace, size - i) + beginArray.length];
                System.arraycopy(beginArray, 0, send, 0, beginArray.length);
                System.arraycopy(data, i, send, start, Math.min(freeSpace, size - i));
                list.add(send);
            }

            uploadDis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void downloadFile(byte[] msg) {
        try {
            String fileName = Convert.bytesToStr(Arrays.copyOfRange(msg, 1, msg[0] + 1));
            File file = new File(rootPath + "/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            DataOutputStream uploadDos = new DataOutputStream(new FileOutputStream(file, true));
            uploadDos.write(Arrays.copyOfRange(msg, msg[0] + 1, msg.length));

            uploadDos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean mkdir(String dirName) {
        return new File(rootPath + "/" + dirName).mkdir();
    }

    public boolean remove(String name) {
        return new File(rootPath + "/" + name).delete();
    }
}
