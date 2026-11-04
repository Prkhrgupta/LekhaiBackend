package in.lekhai.salesinvoice.service;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

/**
 * Shared factories so every invoice precondition failure carries the right HTTP status.
 */
final class SalesInvoiceErrors {

    private SalesInvoiceErrors() {
    }

    static LekhaiClientException unprocessable(String message) {
        return new LekhaiClientException(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    static LekhaiClientException badRequest(String message) {
        return new LekhaiClientException(message, HttpStatus.BAD_REQUEST);
    }

    static LekhaiClientException notFound(String message) {
        return new LekhaiClientException(message, HttpStatus.NOT_FOUND);
    }
}
