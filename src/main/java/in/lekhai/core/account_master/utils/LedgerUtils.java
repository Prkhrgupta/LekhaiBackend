package in.lekhai.core.account_master.utils;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.*;

import java.math.BigDecimal;

public class LedgerUtils {

        private LedgerUtils() {
        }

        public static Ledger createLedgerObject(LedgerRequest request) {
                return new Ledger(
                                request.getName(),
                                request.getLegalName(),
                                request.getAccountGroup(),
                                request.getOpeningBalance(),
                                request.getAccountEntryType(),
                                request.getCreditLimit(),
                                request.getAreaId(),
                                request.getBrokerId(),
                                request.getTransportId(),
                                request.getPan(),
                                request.getAadhaarNumber(),
                                request.getTanNumber(),
                                request.getEmail(),
                                request.getMsmeNumber(),
                                request.getContactPerson(),
                                request.getPhoneNumber(),
                                request.getGstInNumber(),
                                request.getLocation()
                );
        }

        public static GstInDetails createGstInDetailsObject(GstInDetail request, Long ledgerId) {
                return new GstInDetails(
                                ledgerId,
                                request.getRegistrationType(),
                                request.getIsECommerceOperator(),
                                request.getGstInUin(),
                                request.getPartyType()
                );
        }

        public static Address createAddressObject(LedgerRequest request, Long ledgerId) {
                return new Address(
                                ledgerId,
                                request.getMailTo() != null ? request.getMailTo().getMailToLine1() : null,
                                request.getMailTo() != null ? request.getMailTo().getMailToLine2() : null,
                                request.getMailTo() != null ? request.getMailTo().getMailToLine3() : null,
                                request.getPinCode(),
                                BigDecimal.valueOf(request.getDistance() != null ? request.getDistance() : 0),
                                request.getAreaId(),
                                request.getStateAndCode(),
                                request.getCity()
                );
        }

        public static void updateLedgerFromRequest(Ledger ledger, LedgerRequest request) {
                ledger.setName(request.getName());
                ledger.setLegalName(request.getLegalName());
                ledger.setAccountGroupId(request.getAccountGroup());
                ledger.setOpeningBalance(request.getOpeningBalance());
                ledger.setOpeningBalanceType(request.getAccountEntryType());
                ledger.setCreditLimit(request.getCreditLimit());
                ledger.setDefaultAreaId(request.getAreaId());
                ledger.setDefaultBrokerId(request.getBrokerId());
                ledger.setDefaultTransportId(request.getTransportId());
                ledger.setPan(request.getPan());
                ledger.setAadhaar(request.getAadhaarNumber());
                ledger.setTan(request.getTanNumber());
                ledger.setEmail(request.getEmail());
                ledger.setMsme(request.getMsmeNumber());
                ledger.setContactPerson(request.getContactPerson());
                ledger.setPhoneNumber(request.getPhoneNumber());
                ledger.setGstInNumber(request.getGstInNumber());
                ledger.setLocation(request.getLocation());
        }

        public static LedgerResponse mapToResponse(
                Ledger ledger,
                Area area,
                Broker broker,
                Transport transport,
                AccountGroup accountGroup,
                GstInDetails gstInDetails,
                Address address
        ) {
                return new LedgerResponse()
                        .id(ledger.getId())
                        .name(ledger.getName())
                        .legalName(ledger.getLegalName())
                        .accountGroupId(accountGroup != null ? accountGroup.getId() : null)
                        .openingBalance(ledger.getOpeningBalance())
                        .openingBalanceType(ledger.getOpeningBalanceType())
                        .creditLimit(ledger.getCreditLimit())
                        .pan(ledger.getPan())
                        .aadhaar(ledger.getAadhaar())
                        .tan(ledger.getTan())
                        .email(ledger.getEmail())
                        .msme(ledger.getMsme())
                        .contactPerson(ledger.getContactPerson())
                        .phoneNumber(ledger.getPhoneNumber())
                        .gstInNumber(ledger.getGstInNumber())
                        .location(ledger.getLocation())
                        .isActive(ledger.getActive())
                        .area(area != null
                                ? new LedgerArea()
                                .id(area.getId())
                                .name(area.getAreaName())
                                : null)
                        .broker(broker != null
                                ? new LedgerBroker()
                                .id(broker.getId())
                                .name(broker.getName())
                                .phone(broker.getPhone())
                                : null)
                        .transport(transport != null
                                ? new LedgerTransport()
                                .id(transport.getId())
                                .name(transport.getName())
                                .phone(transport.getPhone())
                                .gst(transport.getGstNo())
                                : null)
                        .gstinDetails(gstInDetails != null
                                ? new GstInDetailResponse()
                                .registrationType(gstInDetails.getRegistrationType())
                                .isECommerceOperator(gstInDetails.getIsEcommerceOperator())
                                .gstInUin(gstInDetails.getGstinOrUin())
                                .partyType(gstInDetails.getPartyType())
                                : null)
                        .address(address != null
                                ? new LedgerAddress()
                                .addressLine1(address.getAddressLine1())
                                .addressLine2(address.getAddressLine2())
                                .addressLine3(address.getAddressLine3())
                                .city(address.getCity())
                                .stateId(address.getStateId())
                                .areaId(address.getAreaId())
                                .pincode(address.getPincode())
                                .distance(address.getDistance())
                                : null)
                        .createdAt(DateUtils.getCreatedAt(ledger.getCreatedAt()));
        }
}
