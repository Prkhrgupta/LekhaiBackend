package in.lekhai.common.util;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;

public class FinancialYearDateUtil {
    public static final LocalDate IST = LocalDate.now(ZoneId.of("Asia/Kolkata"));
    public static Year getCurrentFinancialYear() {
        LocalDate march31 = LocalDate.of(IST.getYear(), 3, 31);

        if(IST.isAfter(march31)) {
            return Year.from(IST);
        }
        // If before march31, the current financial year is the prev year
        return Year.from(IST).minusYears(1);
    }
}
