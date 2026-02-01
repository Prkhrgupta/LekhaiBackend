package in.lekhai.core.account_master.utils;

import in.lekhai.core.account_master.domain.Address;
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

    public static GstInDetails createGstInDetailsObject(LedgerRequest.GstInDetail request, Long ledgerId) {
        return new GstInDetails(
                ledgerId,
                request.registrationType(),
                request.isEcommerceOperator(),
                request.gstInUin(),
                request.partyType()
        );
    }

    public static Address createAddressObject(LedgerRequest request, Long ledgerId) {
        return new Address(
                ledgerId,
                request.mailTo().lineOne(),
                request.mailTo().lineTwo(),
                request.mailTo().lineThree(),
                request.pinCode(),
                request.distance(),
                request.areaId(),
                request.stateAndCode(),
                request.city()
        );
    }

    public static LedgerResponse mapToResponse(Ledger ledger) {
        return null;
    }}
