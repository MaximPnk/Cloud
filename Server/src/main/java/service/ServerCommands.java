package service;

import java.io.File;
import java.io.IOException;

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

    public String getRootPath() {
        return rootPath;
    }
}
