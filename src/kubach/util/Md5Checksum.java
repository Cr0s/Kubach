package kubach.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class Md5Checksum {

    public static byte[] createChecksum(String filename) throws Exception {
        MessageDigest complete;
        try (InputStream fis = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];
            complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        }
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);

        return bytesToHexString(b);
    }

    /**
     * Transforms byte array into hexadecimal string
     *
     * @param digest input byte array
     * @return a hex string
     */
    public static String bytesToHexString(byte[] digest) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < digest.length; i++) {
            hexString.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }

        return hexString.toString();
    }
}
