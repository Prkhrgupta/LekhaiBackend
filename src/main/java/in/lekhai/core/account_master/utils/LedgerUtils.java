package in.lekhai.core.account_master.utils;

import in.lekhai.core.account_master.domain.*;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;

public class LedgerUtils {

        private LedgerUtils() {
        }

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
                                request.msmeNumber(),
                                request.contactPerson(),
                                request.phoneNumber(),
                                request.gstInNumber(),
                                request.location());
        }

        public static GstInDetails createGstInDetailsObject(LedgerRequest.GstInDetail request, Long ledgerId) {
                return new GstInDetails(
                                ledgerId,
                                request.registrationType(),
                                request.isEcommerceOperator(),
                                request.gstInUin(),
                                request.partyType());
        }

        public static Address createAddressObject(LedgerRequest request, Long ledgerId) {
                return new Address(
                                ledgerId,
                                request.mailTo() != null ? request.mailTo().lineOne() : null,
                                request.mailTo() != null ? request.mailTo().lineTwo() : null,
                                request.mailTo() != null ? request.mailTo().lineThree() : null,
                                request.pinCode(),
                                request.distance(),
                                request.areaId(),
                                request.stateAndCode(),
                                request.city());
        }

        public static void updateLedgerFromRequest(Ledger ledger, LedgerRequest request) {
                ledger.setName(request.name());
                ledger.setLegalName(request.legalName());
                ledger.setAccountGroupId(request.accountGroup());
                ledger.setOpeningBalance(request.openingBalance());
                ledger.setOpeningBalanceType(request.accountEntryType());
                ledger.setCreditLimit(request.creditLimit());
                ledger.setDefaultAreaId(request.areaId());
                ledger.setDefaultBrokerId(request.brokerId());
                ledger.setDefaultTransportId(request.transportId());
                ledger.setPan(request.pan());
                ledger.setAadhaar(request.aadhaarNumber());
                ledger.setTan(request.tanNumber());
                ledger.setEmail(request.email());
                ledger.setMsme(request.msmeNumber());
                ledger.setContactPerson(request.contactPerson());
                ledger.setPhoneNumber(request.phoneNumber());
                ledger.setGstInNumber(request.gstInNumber());
                ledger.setLocation(request.location());
        }

        public static LedgerResponse mapToResponse(
                        Ledger ledger,
                        Area area,
                        Broker broker,
                        Transport transport,
                        AccountGroup accountGroup,
                        GstInDetails gstInDetails,
                        Address address) {
                return new LedgerResponse(
                                ledger.getId(),
                                ledger.getName(),
                                ledger.getLegalName(),
                                accountGroup != null ? accountGroup.getName() : null,
                                ledger.getOpeningBalance(),
                                ledger.getOpeningBalanceType(),
                                ledger.getCreditLimit(),
                                ledger.getPan(),
                                ledger.getAadhaar(),
                                ledger.getTan(),
                                ledger.getEmail(),
                                ledger.getMsme(),
                                ledger.getContactPerson(),
                                ledger.getPhoneNumber(),
                                ledger.getGstInNumber(),
                                ledger.getLocation(),
                                ledger.getActive(),
                                area != null ? new LedgerResponse.Area(area.getId(), area.getAreaName(),
                                                area.getStateCode()) : null,
                                broker != null ? new LedgerResponse.Broker(broker.getId(), broker.getName(),
                                                broker.getPhone()) : null,
                                transport != null
                                                ? new LedgerResponse.Transport(transport.getId(), transport.getName(),
                                                                transport.getPhone(),
                                                                transport.getGstNo())
                                                : null,
                                gstInDetails != null
                                                ? new LedgerResponse.GstInDetailResponse(
                                                                gstInDetails.getRegistrationType(),
                                                                gstInDetails.getIsEcommerceOperator(),
                                                                gstInDetails.getGstinOrUin(),
                                                                gstInDetails.getPartyType())
                                                : null,
                                address != null
                                                ? new LedgerResponse.LedgerAddress(
                                                                address.getAddressLine1(),
                                                                address.getAddressLine2(),
                                                                address.getAddressLine3(),
                                                                address.getCity(),
                                                                address.getStateId(),
                                                                address.getAreaId(),
                                                                address.getPincode(),
                                                                address.getDistance())
                                                : null,
                                ledger.getCreatedAt());
        }
}
