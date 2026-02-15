package in.lekhai.core.account_master.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateUtils {

    private DateUtils() {
    }

    public static OffsetDateTime getCreatedAt(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneId.SHORT_IDS.get("IST")));
    }
}
