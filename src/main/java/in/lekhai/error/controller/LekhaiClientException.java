package in.lekhai.error.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class LekhaiClientException extends RuntimeException {
    private HttpStatus statusCode;

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public LekhaiClientException(String message) {super(message);}
    public LekhaiClientException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public LekhaiClientException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = HttpStatus.valueOf(statusCode.value());
    }

    public LekhaiClientException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
