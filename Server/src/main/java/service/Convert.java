package service;

public class Convert {

    public static String bytesToStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }
}
