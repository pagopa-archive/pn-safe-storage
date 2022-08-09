package it.pagopa.pn.safestorage;

import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Sha256Component {

    private static final int BUFFER_SIZE = 128 * 1024;

    public String computeSha256(InputStream inStrm) {
        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            final MessageDigest digester = MessageDigest.getInstance("SHA-256");

            int bytesRead;
            while ((bytesRead = inStrm.read(buffer)) != -1) {
                digester.update(buffer, 0, bytesRead);
            }

            byte[] digest = digester.digest();
            return bytesToBase64( digest );
        } catch (NoSuchAlgorithmException | IOException exc) {
            throw new RuntimeException( exc );
        }
    }

    private static String bytesToBase64(byte[] hash) {
        return Base64Utils.encodeToString( hash );
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
