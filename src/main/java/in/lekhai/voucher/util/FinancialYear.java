package in.lekhai.voucher.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class FinancialYear {
    private static final Pattern FY_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private FinancialYear() {
    }

    public static String from(LocalDate date) {
        int year = date.getYear();
        int startYear = date.getMonthValue() >= 4 ? year : year - 1;
        return format(startYear);
    }

    public static String format(int startYear) {
        return startYear + "-" + String.format("%02d", (startYear + 1) % 100);
    }

    public static boolean isValid(String financialYear) {
        if (financialYear == null || !FY_PATTERN.matcher(financialYear).matches()) {
            return false;
        }
        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endSuffix = Integer.parseInt(financialYear.substring(5, 7));
        return (startYear + 1) % 100 == endSuffix;
    }
}
