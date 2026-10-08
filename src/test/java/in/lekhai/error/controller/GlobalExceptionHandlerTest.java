package in.lekhai.error.controller;

import in.lekhai.contract.model.Error;
import in.lekhai.error.controller.voucher.exception.DuplicateBillNumberException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.error.controller.voucher.exception.SeriesImmutableException;
import in.lekhai.error.controller.voucher.exception.SeriesValidationException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void seriesFyMissing_mapsTo422Envelope() {
        ResponseEntity<Error> actual = handler.handleSeriesFyMissing(
                new SeriesFyMissingException("PAYMENT", "2025-26"));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(actual.getBody().getSuccess()).isFalse();
        assertThat(actual.getBody().getError()).isEqualTo("SERIES_FY_MISSING");
        assertThat((java.util.Map<String, Object>) actual.getBody().getData())
                .containsEntry("voucher_type", "PAYMENT")
                .containsEntry("financial_year", "2025-26");
    }

    @Test
    void duplicateBillNumber_mapsTo409() {
        ResponseEntity<Error> actual = handler.handleLekhaiClientException(
                new DuplicateBillNumberException(4L));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void seriesImmutable_mapsTo409() {
        ResponseEntity<Error> actual = handler.handleLekhaiClientException(
                new SeriesImmutableException(7L));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void seriesValidation_mapsTo400() {
        ResponseEntity<Error> actual = handler.handleLekhaiClientException(
                new SeriesValidationException("Manual bill entry is not allowed for this series"));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
