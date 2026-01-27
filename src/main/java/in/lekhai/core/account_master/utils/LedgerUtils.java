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
                request.accountGroup(),
                request.openingBalance(),
                request.accountEntryType(),
                request.creditLimit(),
                request.areaId(),
                request.brokerId(),
                request.transportId(),
                request.pan(),
                request.aadhaarNumber(),
                request.tanNumber(),
                request.email(),
                request.msmeNumber()
        );
    }

    public static LedgerResponse mapToResponse(Ledger ledger) {
        return null;
    }}
