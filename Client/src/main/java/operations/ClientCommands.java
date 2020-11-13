package operations;

import commands.Commands;
import graphics.Window;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import service.Convert;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientCommands {

    private String rootPath = "Client/Storage";

    public void change(String selectedItem) {
        if (selectedItem.equals("..") && !rootPath.equals("Client/Storage")) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        } else if (!selectedItem.contains(".")) {
            rootPath = rootPath + "/" + selectedItem;
        }
    }

    public void log(String str) {
        Window.getController().logArea.appendText(str + System.lineSeparator());
    }

    public void serverFiles(String list) {
        String[] files = list.split(" ");
        Platform.runLater(() -> {
            Window.getController().serverPath.setText(files[0]);
            files[0] = "..";
            Window.getController().serverView.setItems(FXCollections.observableArrayList(files));
        });
    }

    public void clientFiles() {
        String[] files = new File(rootPath).list();
        assert files != null;
        String[] arr = (".. " + String.join(" ", files)).split(" ");
        Platform.runLater(() -> {
            Window.getController().clientPath.setText(rootPath);
            Window.getController().clientView.setItems(FXCollections.observableArrayList(arr));
        });
    }

    public void help() {
        Window.getController().help.setOnAction(click -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Help");
            alert.setHeaderText(null);
            alert.getDialogPane().setPrefWidth(600);
            alert.setContentText(
                    "• Change directory: double click on a folder or \"..\" to return to the parent directory [Client/Server]" + System.lineSeparator() + System.lineSeparator() +
                            "• Download: select file in the server window and click the download button [Server]" + System.lineSeparator() + System.lineSeparator() +
                            "• Upload: select file in the client window and click the upload button [Client]" + System.lineSeparator() + System.lineSeparator() +
                            "• Get: click for update file lists [Client/Server]" + System.lineSeparator() + System.lineSeparator() +
                            "• Make directory: select server path and click mkdir button [Server]" + System.lineSeparator() + System.lineSeparator() +
                            "• Create file: select server path and click touch button [Server]" + System.lineSeparator() + System.lineSeparator() +
                            "• Remove: select file in the server window and click remove button [Server]" + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() +
                            "Author Pankov Maxim © 2020");
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
}
