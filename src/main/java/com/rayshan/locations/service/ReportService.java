package com.rayshan.locations.service;

import com.rayshan.locations.entity.Report;
import com.rayshan.locations.entity.ReportId;
import com.rayshan.locations.repository.ReportRepository;
import com.rayshan.locations.util.FileUtils;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.*;
import org.bouncycastle.math.ec.ECPoint;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.security.MessageDigest;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getAllReports() throws Exception {
        List<Report> records = reportRepository.findAll();
        test(records);
        return records;
    }

    public Optional<Report> getReportById(String idShort, Integer timestamp) {
        return reportRepository.findById(new ReportId(idShort, timestamp));
    }

    public List<Report> getReportsByIdShort(String idShort) {
        return reportRepository.findByIdShort(idShort);
    }

    public List<Report> getReportsByStatusCode(Integer statusCode) {
        return reportRepository.findByStatusCode(statusCode);
    }

    public List<Report> getReportsByDateRange(Long startDate, Long endDate) {
        return reportRepository.findByDatePublishedBetween(startDate, endDate);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    public void deleteReport(String idShort, Integer timestamp) {
        reportRepository.deleteById(new ReportId(idShort, timestamp));
    }

    public void test(List<Report> records) throws Exception {
        Map<String, String> keyMap = new HashMap<>();
        List<Path> keyFiles = FileUtils.findKeyFiles(".");
        keyFiles.stream().forEach(file -> {
            try {
                Map.Entry<String, String> entry = getKeyMapEntry(file);
                keyMap.put(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("==> keyMap: " + keyMap);

        for(Report report: records) {
            String base64HashedKey = keyMap.get(report.getId());
            byte[] keyBytes = Base64.getDecoder().decode(base64HashedKey);
            BigInteger priv = new BigInteger(1, keyBytes);
            byte[] data = Base64.getDecoder().decode(report.getPayload());
            if(data.length > 88) {
                byte[] newData = new byte[data.length - 1];
                System.arraycopy(data, 0, newData, 0, 4); // Copy first 4 bytes
                System.arraycopy(data, 5, newData, 4, data.length - 5); // Skip byte at index 4
                data = newData;
            }

            byte[] timestampBytes = Arrays.copyOfRange(data, 0, 4); // extract data[0:4]
            int unsignedTimestamp = ByteBuffer.wrap(timestampBytes).order(ByteOrder.BIG_ENDIAN).getInt();
            long timestamp = Integer.toUnsignedLong(unsignedTimestamp) + 978307200L;

            System.out.println("Timestamp: " + timestamp);

            byte[] ephPubKeyBytes = Arrays.copyOfRange(data, 5, 62);
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp224r1");
            ECCurve curve = ecSpec.getCurve();
            ECPoint point = curve.decodePoint(ephPubKeyBytes);
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            PublicKey ephKey = keyFactory.generatePublic(pubSpec);

            //System.out.println("EphPubKey: " + ephKey);

            // 2. Create private key from BigInteger (priv)
            ECPrivateKeySpec privSpec = new ECPrivateKeySpec(priv, ecSpec);
            PrivateKey privKey = keyFactory.generatePrivate(privSpec);

            //System.out.println("privKey: " + privKey);

            // 3. Perform ECDH key agreement
            KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
            ka.init(privKey);
            ka.doPhase(ephKey, true);
            byte[] sharedSecret = ka.generateSecret(); // same as shared_key in Python

            // 4. Derive symmetric key using SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(sharedSecret);
            sha256.update(new byte[]{0x00, 0x00, 0x00, 0x01}); // counter
            sha256.update(ephPubKeyBytes);
            byte[] symmetricKey = sha256.digest(); // 32 bytes

            // 5. Split into decryption key and IV
            byte[] decryptionKey = Arrays.copyOfRange(symmetricKey, 0, 16);
            byte[] iv = Arrays.copyOfRange(symmetricKey, 16, 32);

            // 6. Extract encrypted data and tag
            byte[] encData = Arrays.copyOfRange(data, 62, 72); // 10 bytes
            byte[] tag = Arrays.copyOfRange(data, 72, data.length);

            byte[] decrypted = decrypt(encData, tag, decryptionKey, iv);
            Map<String, Object> valueMap = decodeTag(decrypted);
            System.out.println("Decrypted data: " + valueMap);

        }


    }

    public static Map.Entry<String, String> getKeyMapEntry(Path filePath) throws IOException {
        final String[] key = new String[1];
        final String[] value = new String[1];
        Files.lines(filePath).forEach(line -> {
            String[] parts = line.split(":");
            if (parts[0].trim().equals("Hashed adv key")) {
                key[0] = parts[1].trim();
            } else if(parts[0].trim().equals("Private key")) {
                value[0] = parts[1].trim();
            }
        });
        System.out.println("Key: " + key[0]);
        System.out.println("value: " + value[0]);
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

    public static Map<String, Object> decodeTag(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        int latRaw = buffer.getInt(0);
        int lonRaw = buffer.getInt(4);
        double latitude = latRaw / 10_000_000.0;
        double longitude = lonRaw / 10_000_000.0;

        int confidence = Byte.toUnsignedInt(data[8]);
        int status = Byte.toUnsignedInt(data[9]);

        Map<String, Object> tag = new HashMap<>();
        tag.put("lat", latitude);
        tag.put("lon", longitude);
        tag.put("conf", confidence);
        tag.put("status", status);
        return tag;
    }
}
