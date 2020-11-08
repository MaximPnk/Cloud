package client.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ClientCommands {

    private String rootPath = "Client";

    public String getAnswer(String command) {
        if (command.equals("--help")) {
            return "---------------------------------------------------------------------" + System.lineSeparator() +
                    "ls - for show list of dirs/files" + System.lineSeparator() +
                    "cd - for change directory" + System.lineSeparator() +
                    "touch - create file" + System.lineSeparator() +
                    "mkdir - create directory" + System.lineSeparator() +
                    "rm - delete directory or file" + System.lineSeparator() +
                    "cat - read file" + System.lineSeparator() +
                    "copy - copy [filename.txt] [your/path/new_name.txt] - args in []" + System.lineSeparator() +
                    "---------------------------------------------------------------------";
        } else if (command.equals("ls")) {
            return "---------------------------------------------------------------------" + System.lineSeparator() +
                    getFilesList() + System.lineSeparator() +
                    "---------------------------------------------------------------------";
        } else if (command.matches("^cd .*$") && command.split(" ").length == 2) {
            return changeRootPath(command);
        } else if (command.matches("^touch [\\w]+\\.[a-zA-Z]+$")) {
            return createFile(command.split(" ")[1]) ?
                    "File successfully created" :
                    "File already exists or something went wrong";
        } else if (command.matches("^mkdir [\\w]+$")) {
            return makeDirectory(command.split(" ")[1]) ?
                    "Directory successfully created" :
                    "Directory already exists or something went wrong";
        } else if (command.matches("^rm [\\w]+(\\.[a-zA-Z]+)?$")) {
            return delete(command.split(" ")[1]) ?
                    "Successfully deleted!" :
                    "Path doesn't exists or directory isn't empty";
        } else if (command.matches("cat [\\w]+\\.[a-zA-Z]+$")) {
            return readFile(command.split(" ")[1]);
        } else if (command.matches("^copy [\\w]+\\.[a-zA-Z]+ ([\\w]+/)*[\\w]+\\.[a-zA-Z]+$")) {
            return copyFile(command.split(" ")[1], command.split(" ")[2]) ?
                    "Copy successfully completed" :
                    "Directory doesn't exists or something went wrong";
        } else if (command.equals("getfiles")) {
            return "Path: " + rootPath + System.lineSeparator() + String.join(System.lineSeparator(), Objects.requireNonNull(new File(rootPath).list()));
        } else {
            return "Enter \"--help\" for help";
        }
    }

    private boolean copyFile(String fileName, String dst) {
        if (dst.contains("/") && !new File(dst.substring(0, dst.lastIndexOf("/"))).exists()) {
            return false;
        } else {
            try {
                Files.copy(Paths.get(rootPath + "/" + fileName), Paths.get("Client/" + dst));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private String readFile(String fileName) {
        StringBuilder result = new StringBuilder();
        try {
            Files.newBufferedReader(Paths.get(rootPath + "/" + fileName))
                    .lines()
                    .forEach(t -> result.append(t).append(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private boolean delete(String name) {
        return new File(rootPath + "/" + name).delete();
    }


    private boolean makeDirectory(String dir) {
        return new File(rootPath + "/" + dir).mkdir();
    }

    private boolean createFile(String fileName) {
        try {
            return new File(rootPath + "/" + fileName).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String changeRootPath(String command) {
        if (command.endsWith("/")) {
            command = command.substring(0, command.length() -1);
        }

        String tmpPath = rootPath;
        String[] path = command.split(" ")[1].split("/");

        for (String s : path) {
            if (s.equals("..") && !tmpPath.equalsIgnoreCase("Client")) {
                tmpPath = tmpPath.substring(0, tmpPath.lastIndexOf("/"));
            } else if (s.equals("..") || s.matches(".*[^\\w].*")) {
                return "Invalid destination path";
            } else if (new File(tmpPath + "/" + s).exists()) {
                tmpPath = tmpPath + "/" + s;
            } else {
                return "Invalid destination path";
            }
        }

        rootPath = tmpPath;
        return "Changed your path to " + rootPath;
    }

    private String getFilesList() {
        String[] files = new File(rootPath).list();

        if (files != null) {
            return String.join(", ", files);
        } else {
            return "";
        }
    }
}
