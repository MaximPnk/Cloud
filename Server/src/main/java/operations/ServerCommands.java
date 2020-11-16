package operations;

import commands.Commands;
import service.Convert;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerCommands {

    private String username = ""; //TODO изменить
    private String rootPath = "Server/Storage/" + username;

    public ServerCommands() {
    }

    public ServerCommands(String username) {
        this.username = username;
    }

    public String cd(String selectedItem) {
        if (selectedItem.equals("..") && !rootPath.equals("Server/Storage" + username)) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
            return "Path changed successfully";
        } else if (!selectedItem.contains(".")) {
            rootPath = rootPath + "/" + selectedItem;
            return "Path changed successfully";
        } else {
            return "Invalid path";
        }
    }

    public String get() {
        String[] files = new File(rootPath).list();
        if (files == null) {
            return "";
        }
        return rootPath + " " + String.join(" ", files);
    }

    public boolean mkdir(String dirName) {
        return new File(rootPath + "/" + dirName).mkdir();
    }

    public boolean rm(String name) {
        System.out.println(rootPath + "/" + name);
        return new File(rootPath + "/" + name).delete();
    }

    public boolean touch (String fileName) {
        try {
            return new File(rootPath + "/" + fileName).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void upload(byte[] msg) {
        try {
            String fileName = Convert.bytesToStr(Arrays.copyOfRange(msg, 1, msg[0] + 1));
            File file = new File(rootPath + "/" + fileName);
            System.out.println(rootPath + "/" + fileName);
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

    public List<byte []> download(String fileName) {
        List<byte []> list = new ArrayList<>();
        try {
            File file = new File(rootPath + "/" + fileName);
            DataInputStream uploadDis = new DataInputStream(new FileInputStream(file));
            byte[] data = new byte[(int) file.length()];
            int size = uploadDis.read(data);
            byte[] beginArray = new byte[2 + fileName.getBytes().length];
            beginArray[0] = Commands.DOWNLOAD.getBt();
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

    public String getRootPath() {
        return rootPath;
    }
}
