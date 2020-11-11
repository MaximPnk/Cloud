package operations;

public class ChangeDirectory {

    private static String rootPath = "Server/Storage";

    public static void change(String selectedItem) {
        if (selectedItem.equals("..") && !rootPath.equals("Server/Storage")) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        } else if (!selectedItem.contains(".")) {
            rootPath = rootPath + "/" + selectedItem;
        }
    }

    public static String getRootPath() {
        return rootPath;
    }
}
