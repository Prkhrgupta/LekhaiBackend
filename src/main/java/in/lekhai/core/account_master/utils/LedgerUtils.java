package in.lekhai.core.account_master.utils;

import in.lekhai.core.account_master.domain.GstInDetails;
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

    public static GstInDetails createGstInDetailsObject(LedgerRequest request, Long ledgerId) {
        return new GstInDetails(
                ledgerId,
                request.gstInDetails().registrationType(),
                request.gstInDetails().isEcommerceOperator(),
                request.gstInDetails().gstInUin(),
                request.gstInDetails().partyType()
        );
    }

    public static LedgerResponse mapToResponse(Ledger ledger) {
        return null;
    }}
