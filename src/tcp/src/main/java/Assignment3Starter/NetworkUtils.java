package Assignment3Starter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * NetworkUtils is a helper class available to both the Client and the Server.
 * It provides methods for converting data to and from bytes so that it can be sent over the wire and
 * then converted back into a usable format on the other side.
 * */

public class NetworkUtils {
    // https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/

    public static byte[] intToBytes(final int data) {
        return new byte[] { (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff), };
    }

    // https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/
    public static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0);
    }

    /**
     * This method first measures the length of the bytes we want to send out, sends that as the first 4 bytes, then
     * sends the rest of the message.
     * @param out OutputStream
     * @param bytes the message to send in byte[] format
     * @throws IOException
     */
    public static void Send(OutputStream out, byte... bytes) throws IOException {
        out.write(intToBytes(bytes.length));
        out.write(bytes);
        out.flush();
    }



    /**
     * Reads in the InputStream object of size length, and returns a byte array that could be the whole object,
     * or it could be part of it if length < in.length
     * @param in InputStream
     * @param length length of in
     * @return byte array representation of in from 0->length-1
     * @throws IOException
     */
    private static byte[] Read(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        System.out.println("Read in bytes: " + length);
        int bytesRead = 0;
        try {
            bytesRead = in.read(bytes, 0, length);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (bytesRead < length && bytesRead > 0) {
            byte[] newBytes = Read(in, length-bytesRead);
            System.arraycopy(newBytes, 0, bytes, bytesRead, newBytes.length);
        }
        return bytes;
    }



    /**
     * First 4 bytes we read give us the length of the message we are about to receive.
     * Next, we call read again with the length of the actual bytes in the data we are interested in.
     * Remember that read can parially read an objects bytes given the length.
     * @param in InputStream
     * @return static byte[]
     * @throws IOException
     */
    public static byte[] Receive(InputStream in) throws IOException {
        byte[] lengthBytes = Read(in, 4);
        if (lengthBytes == null)
            return new byte[0];
        int length = NetworkUtils.bytesToInt(lengthBytes);
        byte[] message = Read(in, length);
        if (message == null)
            return new byte[0];
        return message;
    }
}
