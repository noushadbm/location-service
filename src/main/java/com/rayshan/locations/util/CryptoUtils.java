package com.rayshan.locations.util;

import lombok.extern.log4j.Log4j2;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Log4j2
public class CryptoUtils {
    public static Map.Entry<String, String> getKeyMapEntry(Path keyFilePath) throws IOException {
        log.info("Reading key file: " + keyFilePath);
        final String[] key = new String[1];
        final String[] value = new String[1];
        Files.lines(keyFilePath).forEach(line -> {
            String[] parts = line.split(":");
            if (parts[0].trim().equals("Hashed adv key")) {
                key[0] = parts[1].trim();
            } else if(parts[0].trim().equals("Private key")) {
                value[0] = parts[1].trim();
            }
        });
        //System.out.println("Key: " + key[0]);
        //System.out.println("value: " + value[0]);
        return Map.entry(key[0], value[0]);
    }

    public static byte[] decrypt(byte[] encData, byte[] tag, byte[] decryptionKey, byte[] iv) throws Exception {
        byte[] combinedCiphertext = new byte[encData.length + tag.length];
        System.arraycopy(encData, 0, combinedCiphertext, 0, encData.length);
        System.arraycopy(tag, 0, combinedCiphertext, encData.length, tag.length);

        SecretKeySpec keySpec = new SecretKeySpec(decryptionKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(tag.length * 8, iv); // e.g., 80 bits if tag is 10 bytes

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        return cipher.doFinal(combinedCiphertext);
    }
}
