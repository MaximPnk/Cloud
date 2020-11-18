package service;

public class Convert {

    public static String bytesToStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    public static int bytesToInt (byte[] bytes) {
        int value = 0;
        for (byte aByte : bytes) value = (value << 8) + (aByte & 0xFF);
        return value;
    }
}
