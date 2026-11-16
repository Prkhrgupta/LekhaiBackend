package in.lekhai.accountbooks.interestcalculation.controller;

import in.lekhai.accountbooks.interestcalculation.service.InterestCalculationService;
import in.lekhai.contract.api.InterestCalculationApi;
import in.lekhai.contract.model.DateWiseInterestResponse;
import in.lekhai.contract.model.ManualInterestCalculateRequest;
import in.lekhai.contract.model.ManualInterestResponse;
import in.lekhai.contract.model.ProductBasisInterestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
public class InterestCalculationController implements InterestCalculationApi {

    private final InterestCalculationService interestCalculationService;

    public InterestCalculationController(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    @Override
    public ResponseEntity<ProductBasisInterestResponse> getProductBasisInterest(
            Long ledgerId,
            LocalDate fromDate,
            LocalDate toDate,
            String calculationType,
            BigDecimal interestRate,
            BigDecimal includeOpeningInterest
    ) {
        ProductBasisInterestResponse response = interestCalculationService.getProductBasisInterest(
                ledgerId, fromDate, toDate, calculationType, interestRate, includeOpeningInterest
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<DateWiseInterestResponse> getDateWiseInterest(
            Long ledgerId,
            LocalDate firstDrDate,
            LocalDate lastDrDate,
            LocalDate firstCrDate,
            LocalDate lastCrDate,
            Integer graceDays,
            BigDecimal yearlyRate,
            LocalDate asOnDate,
            Boolean includeOpening
    ) {
        DateWiseInterestResponse response = interestCalculationService.getDateWiseInterest(
                ledgerId, firstDrDate, lastDrDate, firstCrDate, lastCrDate, graceDays, yearlyRate, asOnDate, includeOpening
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ManualInterestResponse> calculateManualInterest(ManualInterestCalculateRequest request) {
        ManualInterestResponse response = interestCalculationService.calculateManualInterest(request);
        return ResponseEntity.ok(response);
    }
}
