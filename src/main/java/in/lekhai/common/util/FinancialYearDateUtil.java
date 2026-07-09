package in.lekhai.common.util;

import in.lekhai.core.util.JwtUtil;

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

    public boolean isDateInCurrentJwtFinancialYear(LocalDate date) {
        Year jwtYear = JwtUtil.extractJwtClaim().financialYearStart();
        LocalDate april1 = LocalDate.of(jwtYear.getValue(), 4, 1);
        LocalDate march31 = LocalDate.of(jwtYear.plusYears(1).getValue(), 3, 31);
        return date.isAfter(april1) && date.isBefore(march31);
    }
}
