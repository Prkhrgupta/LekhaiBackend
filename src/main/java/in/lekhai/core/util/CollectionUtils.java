package in.lekhai.core.util;

import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        if (list == null || index < 0 || index >= list.size()) {
            return defaultValue;
        }
        return list.get(index);
    }
}
