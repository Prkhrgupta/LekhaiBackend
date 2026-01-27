package in.lekhai.core.account_master.utils;

import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;

public class LedgerUtils {

    private LedgerUtils() {}

    public static Ledger createLedgerObject(LedgerRequest request) {
        return new Ledger(
                request.name(),
                request.legalName(),
                request.accountGroupId(),
                request.openingBalance(),
                request.openingBalanceType(),
                request.creditLimit(),
                request.defaultAreaId(),
                request.defaultBrokerId(),
                request.defaultTransportId(),
                request.pan(),
                request.aadhaar(),
                request.tan(),
                request.email(),
                request.msme()
        );
    }

    public static LedgerResponse mapToResponse(Ledger ledger) {
        return new LedgerResponse(
                ledger.getId(),
                ledger.getName(),
                ledger.getLegalName(),
                ledger.getAccountGroupId(),
                ledger.getOpeningBalance(),
                ledger.getOpeningBalanceType(),
                ledger.getCreditLimit(),
                ledger.getDefaultAreaId(),
                ledger.getDefaultBrokerId(),
                ledger.getDefaultTransportId(),
                ledger.getPan(),
                ledger.getAadhaar(),
                ledger.getTan(),
                ledger.getEmail(),
                ledger.getMsme()
        );
    }}
