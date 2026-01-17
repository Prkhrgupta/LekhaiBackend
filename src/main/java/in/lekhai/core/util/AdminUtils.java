package in.lekhai.core.util;

import in.lekhai.core.enums.Roles;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AdminUtils {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
    private static final Integer DEFAULT_RANDOM_LETTER_LENGTH = 2;

    // TODO : Change this to return a UUID
    public static String createUUID(Roles role) {
        StringBuilder uuidBuilder = new StringBuilder();
        String acronym = "ACY";
        String dateStr = LocalDate.now(DEFAULT_ZONE).format(DATE_FORMATTER);
        String randomPart = generateRandomAlphanumeric();

        uuidBuilder.append(acronym);
        uuidBuilder.append(dateStr);
        uuidBuilder.append(randomPart);

        String checksum = generateChecksum(uuidBuilder.toString());
        uuidBuilder.append(checksum);
        return uuidBuilder.toString();
    }

    private static String generateRandomAlphanumeric() {
        StringBuilder sb = new StringBuilder(AdminUtils.DEFAULT_RANDOM_LETTER_LENGTH);
        for (int i = 0; i < AdminUtils.DEFAULT_RANDOM_LETTER_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private static String generateChecksum(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            return String.format("%02X", hash[0] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public static Integer createShopCode() {
        return 10000 + SECURE_RANDOM.nextInt(90000);
    }
}
