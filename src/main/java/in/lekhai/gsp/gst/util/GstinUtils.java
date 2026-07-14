package in.lekhai.gsp.gst.util;

import java.util.regex.Pattern;

public final class GstinUtils {

    private GstinUtils() {}

    /**
     * https://github.com/tk120404/gst
     */
    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");

    /**
     * Validates both GSTIN regex and checksum.
     */
    public static boolean isValid(String gstin) {
        if (gstin == null) {
            return false;
        }

        gstin = gstin.trim().toUpperCase();

        return GSTIN_PATTERN.matcher(gstin).matches()
                && isValidChecksum(gstin);
    }

    /**
     * Validates GSTIN checksum.
     */
    public static boolean isValidChecksum(String gstin) {
        if (gstin == null || gstin.length() != 15) {
            return false;
        }

        gstin = gstin.toUpperCase();

        char actualCheckDigit = gstin.charAt(14);

        int sum = 0;

        for (int i = 0; i < 14; i++) {
            char c = gstin.charAt(i);

            int value;
            if (Character.isDigit(c)) {
                value = c - '0';
            } else {
                value = c - 'A' + 10; // A=10, B=11 ... Z=35
            }

            value *= (i % 2) + 1;

            value = (value / 36) + (value % 36);

            sum += value;
        }

        int checksum = (36 - (sum % 36)) % 36;

        char expectedCheckDigit =
                checksum < 10
                        ? (char) ('0' + checksum)
                        : (char) ('A' + checksum - 10);

        return actualCheckDigit == expectedCheckDigit;
    }

    public static String extractPan(String gstin) {
        if (gstin == null || gstin.length() < 12) {
            throw new IllegalArgumentException("Invalid GSTIN");
        }

        return gstin.substring(2, 12);
    }

    public static String extractGstStateCode(String gstin) {
        if (gstin == null || gstin.length() < 2) {
            throw new IllegalArgumentException("Invalid GSTIN");
        }

        return gstin.substring(0, 2);
    }
}