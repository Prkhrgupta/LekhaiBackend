package in.lekhai.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(
        boolean success,
        String message,
        T data,
        String error,
        LocalDateTime timestamp
) {

    // Success with data
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "Success", data, null, LocalDateTime.now());
    }

    // Success with custom message and data
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data, null, LocalDateTime.now());
    }

    // Success without data
    public static <T> Result<T> success(String message) {
        return new Result<>(true, message, null, null, LocalDateTime.now());
    }

    // Error with message
    public static <T> Result<T> error(String error) {
        return new Result<>(false, null, null, error, LocalDateTime.now());
    }

    // Error with custom message and error details
    public static <T> Result<T> error(String message, String error) {
        return new Result<>(false, message, null, error, LocalDateTime.now());
    }
}