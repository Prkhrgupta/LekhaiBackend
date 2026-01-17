package in.lekhai.error.controller;

public class LekhaiException extends RuntimeException {

    public LekhaiException(String message) {
        super(message);
    }

    public LekhaiException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
