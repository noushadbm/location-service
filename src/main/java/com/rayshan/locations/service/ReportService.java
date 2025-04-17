package com.rayshan.locations.service;

import com.rayshan.locations.entity.Report;
import com.rayshan.locations.entity.ReportId;
import com.rayshan.locations.model.LocationData;
import com.rayshan.locations.repository.ReportRepository;
import com.rayshan.locations.util.CryptoUtils;
import com.rayshan.locations.util.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
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
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.ECNamedCurveTable;

import java.util.Base64;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.rayshan.locations.common.Constants.COMMON_DATE_FORMAT;

@Log4j2
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<LocationData> getAllReports() throws Exception {
        // TODO: Read stored reports from the database
        log.info("Reading all reports from the database");
        List<Report> records = reportRepository.findAll();
        log.info("Converting to location data");
        List<LocationData> locations = convertToLocationData(records);
        return locations;
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

    public List<LocationData> convertToLocationData(List<Report> records) throws Exception {
        log.info("Converting reports to location data. Size: {}", records.size());
        Map<String, String> keyMap = new HashMap<>();
        List<Path> keyFiles = FileUtils.findKeyFiles(".");
        keyFiles.stream().forEach(file -> {
            try {
                Map.Entry<String, String> entry = CryptoUtils.getKeyMapEntry(file);
                keyMap.put(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //System.out.println("==> keyMap: " + keyMap);

        List<LocationData> locationDataList = new ArrayList<>();
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

            //System.out.println("Timestamp: " + timestamp);
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

            byte[] decrypted = CryptoUtils.decrypt(encData, tag, decryptionKey, iv);
            //Map<String, Object> valueMap = decodeTag(decrypted);
            //System.out.println("Decrypted data: " + valueMap);
            LocationData locationData = decodeTag(decrypted, timestamp);
            locationDataList.add(locationData);
        }
        log.info("Returning location data list.");
        return locationDataList;
    }
    public static LocationData decodeTag(byte[] data, long timestamp) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        int latRaw = buffer.getInt(0);
        int lonRaw = buffer.getInt(4);
        double latitude = latRaw / 10_000_000.0;
        double longitude = lonRaw / 10_000_000.0;

        int confidence = Byte.toUnsignedInt(data[8]);
        int status = Byte.toUnsignedInt(data[9]);

//        Map<String, Object> tag = new HashMap<>();
//        tag.put("lat", latitude);
//        tag.put("lon", longitude);
//        tag.put("conf", confidence);
//        tag.put("status", status);
        //return tag;

        LocationData locationData = new LocationData();
        locationData.setLongitude(longitude);
        locationData.setLatitude(latitude);
        locationData.setConfidence(confidence);
        locationData.setUrl("https://maps.google.com/maps?q="+ latitude +"," + longitude);

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        // Format the date and time as a string
        String formattedDate = dateTime.format(COMMON_DATE_FORMAT);
        locationData.setDateTime(formattedDate);
        return locationData;
    }
}
