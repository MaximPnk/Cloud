package service;

import java.io.*;
import java.util.Arrays;

public class ServerCommands {

    private String rootPath = "Server/Storage";

    public String cd(String selectedItem) {
        if (selectedItem.equals("..") && !rootPath.equals("Server/Storage")) {
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

    public String getRootPath() {
        return rootPath;
    }
}
