package graphics;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestUpload {

    public static void main(String[] args) {
        try {
            /*byte[] arr = new byte[255];
            FileInputStream fis = new FileInputStream(new File("Client/Storage" + "/" + "photo.jpg"));
            int length = "photo.jpg".length();
            arr[0] = (byte) 30;
            arr[1] = (byte) length;

            for (int i = 0; i < "photo.jpg".getBytes().length; i++) {
                arr[i + 2] = "photo.jpg".getBytes()[i];
            }

            fis.read(arr, length + 2, arr.length - length - 2);

            System.out.println(Arrays.toString(arr));

            FileInputStream testfis = new FileInputStream(new File("Client/Storage" + "/" + "photo.jpg"));
            for (int i = 0; i < 245; i++) {
                System.out.print(testfis.read() + ", ");
            }

            byte[] arr2 = new byte[100000];

            DataInputStream dis = new DataInputStream(new FileInputStream(new File("Client/Storage" + "/" + "photo.jpg")));
            System.out.println();
            dis.readFully(arr2);
            System.out.println(Arrays.toString(arr2));

            File newPhoto = new File("Client/Storage" + "/" + "photo3.jpg");
            newPhoto.createNewFile();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(newPhoto));
            dos.write(arr2);*/
            byte[] a = BigInteger.valueOf(49500).toByteArray();
            System.out.println(Arrays.toString(a));
            System.out.println(asd(a));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int pareAsBigEndianByteArray (byte[] bytes) {
        int factor = bytes.length - 1;
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (i == 0) {
                result |= bytes[i] << (8 * factor--);
            } else {
                result |= bytes[i] << (8 * factor--);
            }
        }
        return result;
    }

    public static int asd (byte[] bytes) {

        int value = 0;
        for (int i = 0; i < bytes.length; i++)
            value = (value << 8) + (bytes[i] & 0xFF);

        return value;
    }
}
