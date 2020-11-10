import java.io.*;

public class Test {

    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream(new File("Client/photo.jpg"));
            FileInputStream fis2 = new FileInputStream(new File("Server/photo.jpg"));
            while (fis.available() > 0) {
                System.out.print(fis.read() + " " + fis2.read());
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
