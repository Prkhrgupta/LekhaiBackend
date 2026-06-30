package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.*;
import in.lekhai.core.account_master.repository.*;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.gst.TaxProGstClient;
import in.lekhai.gsp.gst.dto.GstDetailsDto;
import in.lekhai.gsp.gst.dto.GstVerificationResponseDto;
import in.lekhai.gsp.gst.util.GstinUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static in.lekhai.core.account_master.utils.LedgerUtils.*;

@Service
public class LedgerService {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private final LedgerRepository ledgerRepository;
        private final GstDetailsRepository gstDetailsRepository;
        private final AddressRepository addressRepository;
        private final AreaRepository areaRepository;
        private final BrokerRepository brokerRepository;
        private final TransportRepository transportRepository;
        private final AccountGroupRepository accountGroupRepository;
        private final StateRepository stateRepository;
        private final VoucherRepository voucherRepository;
        private final TaxProGstClient taxProGstClient;

        public LedgerService(
                LedgerRepository ledgerRepository,
                GstDetailsRepository gstDetailsRepository,
                AddressRepository addressRepository,
                AreaRepository areaRepository,
                BrokerRepository brokerRepository,
                TransportRepository transportRepository,
                AccountGroupRepository accountGroupRepository,
                StateRepository stateRepository,
                VoucherRepository voucherRepository,
                TaxProGstClient taxProGstClient
        ) {
            this.ledgerRepository = ledgerRepository;
            this.gstDetailsRepository = gstDetailsRepository;
            this.addressRepository = addressRepository;
            this.areaRepository = areaRepository;
            this.brokerRepository = brokerRepository;
            this.transportRepository = transportRepository;
            this.accountGroupRepository = accountGroupRepository;
            this.stateRepository = stateRepository;
            this.voucherRepository = voucherRepository;
            this.taxProGstClient = taxProGstClient;
        }

        @ShopContextTransactional
        public LedgerResponse createLedger(LedgerRequest request) {
                Ledger ledger = ledgerRepository.save(createLedgerObject(request));
                log.info("Saved ledger for shop {} :: ledger id {}", request.getName(), ledger.getId());
                if (Boolean.TRUE.equals(request.getGstInDetailsPresent()) && request.getGstInDetails() != null) {
                        gstDetailsRepository.save(createGstInDetailsObject(request.getGstInDetails(), ledger.getId()));
                        log.info("Saved gst in details for shop {} :: {}", request.getName(), ledger.getId());
                }
                addressRepository.save(createAddressObject(request, ledger.getId()));
                log.info("Saved address details for shop {} :: {}", request.getName(), ledger.getId());

                Area area = ledger.getDefaultAreaId() != null
                                ? areaRepository.findById(ledger.getDefaultAreaId()).orElse(null)
                                : null;
                Broker broker = ledger.getDefaultBrokerId() != null
                                ? brokerRepository.findById(ledger.getDefaultBrokerId()).orElse(null)
                                : null;
                Transport transport = ledger.getDefaultTransportId() != null
                                ? transportRepository.findById(ledger.getDefaultTransportId()).orElse(null)
                                : null;

                AccountGroup accountGroup = ledger.getAccountGroupId() != null
                                ? accountGroupRepository.findById(ledger.getAccountGroupId()).orElse(null)
                                : null;

                GstInDetails gstInDetails = gstDetailsRepository.findByLedgerId(ledger.getId()).orElse(null);

                return LedgerUtils.mapToResponse(ledger, area, broker, transport, accountGroup, gstInDetails, null);
        }

        @ShopContextTransactional
        public LedgerResponse updateLedger(Long id, LedgerRequest request) {
                Ledger ledger = ledgerRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));

                updateLedgerFromRequest(ledger, request);
                ledgerRepository.save(ledger);
                log.info("Updated ledger for shop {} :: ledger id {}", request.getName(), ledger.getId());

                if (Boolean.TRUE.equals(request.getGstInDetailsPresent())
                        && request.getGstInDetails() != null) {

                        GstInDetails gstDetails = gstDetailsRepository
                                .findByLedgerId(ledger.getId())
                                .orElseGet(() -> new GstInDetails(
                                        ledger.getId(), null, null, null, null));

                        gstDetails.setRegistrationType(
                                request.getGstInDetails().getRegistrationType());
                        gstDetails.setIsEcommerceOperator(
                                request.getGstInDetails().getIsECommerceOperator());
                        gstDetails.setGstinOrUin(
                                request.getGstInDetails().getGstInUin());
                        gstDetails.setPartyType(
                                request.getGstInDetails().getPartyType());

                        gstDetailsRepository.save(gstDetails);

                        log.info("Updated gst in details for shop {} :: {}", request.getName(), ledger.getId());
                }


                if (request.getMailTo() != null) {

                        Address address = addressRepository
                                .findByLedgerId(ledger.getId())
                                .orElseGet(() -> new Address(
                                        ledger.getId(),
                                        null, null, null, null,
                                        null, null, null, null
                                ));

                        address.setAddressLine1(request.getMailTo().getMailToLine1());
                        address.setAddressLine2(request.getMailTo().getMailToLine2());
                        address.setAddressLine3(request.getMailTo().getMailToLine3());
                        address.setPincode(request.getPinCode());
                        address.setDistance(BigDecimal.valueOf(request.getDistance()));
                        address.setAreaId(request.getAreaId());
                        address.setStateId(request.getStateAndCode());
                        address.setCity(request.getCity());

                        addressRepository.save(address);

                        log.info("Updated address details for shop {} :: {}", request.getName(), ledger.getId());
                }


                Area area = ledger.getDefaultAreaId() != null
                                ? areaRepository.findById(ledger.getDefaultAreaId()).orElse(null)
                                : null;
                Broker broker = ledger.getDefaultBrokerId() != null
                                ? brokerRepository.findById(ledger.getDefaultBrokerId()).orElse(null)
                                : null;
                Transport transport = ledger.getDefaultTransportId() != null
                                ? transportRepository.findById(ledger.getDefaultTransportId()).orElse(null)
                                : null;

                AccountGroup accountGroup = ledger.getAccountGroupId() != null
                                ? accountGroupRepository.findById(ledger.getAccountGroupId()).orElse(null)
                                : null;

                GstInDetails gstInDetails = gstDetailsRepository.findByLedgerId(ledger.getId()).orElse(null);

                return LedgerUtils.mapToResponse(ledger, area, broker, transport, accountGroup, gstInDetails, null);
        }

        @ShopContextTransactional
        public LedgerResponse getLedgerById(Long id) {
                Ledger ledger = ledgerRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));

                Area area = ledger.getDefaultAreaId() != null
                                ? areaRepository.findById(ledger.getDefaultAreaId()).orElse(null)
                                : null;
                Broker broker = ledger.getDefaultBrokerId() != null
                                ? brokerRepository.findById(ledger.getDefaultBrokerId()).orElse(null)
                                : null;
                Transport transport = ledger.getDefaultTransportId() != null
                                ? transportRepository.findById(ledger.getDefaultTransportId()).orElse(null)
                                : null;

                AccountGroup accountGroup = ledger.getAccountGroupId() != null
                                ? accountGroupRepository.findById(ledger.getAccountGroupId()).orElse(null)
                                : null;

                Address address = addressRepository.findByLedgerId(ledger.getId()).orElse(null);

                GstInDetails gstInDetails = gstDetailsRepository.findByLedgerId(ledger.getId()).orElse(null);

                return LedgerUtils.mapToResponse(ledger, area, broker, transport, accountGroup, gstInDetails, address);
        }

        @ShopContextTransactional
        public List<DropdownItem> listLedgers(List<Long> underAccountGroup) {
            List<Ledger> ledgerList = ledgerRepository.findAll();

            if(underAccountGroup != null && !underAccountGroup.isEmpty()) {
                Set<Long> filterAccountGroups = accountGroupRepository.findHierarchyIds(underAccountGroup);
                ledgerList = ledgerList.stream()
                        .filter(ledger -> filterAccountGroups.contains(ledger.getAccountGroupId()))
                        .toList();
            }

            return ledgerList.stream()
                    .map(ledger ->
                            new DropdownItem()
                                    .id(ledger.getId())
                                    .label(ledger.getName())
                    )
                    .toList();
        }

        @ShopContextTransactional
        public LedgerSummaryPageResponse listLedgerSummaries(LedgerSearchableField searchableField, String query, Pageable pageable) {
            Page<Ledger> ledgersPage;
            if (query != null && !query.trim().isEmpty() && searchableField == LedgerSearchableField.NAME) {
                List<Ledger> ledgers = ledgerRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
                long total = ledgerRepository.countByNameContainingIgnoreCase(query.trim());
                ledgersPage = new PageImpl<>(ledgers, pageable, total);
            } else {
                ledgersPage = ledgerRepository.findAll(pageable);
            }
            List<Ledger> ledgers = ledgersPage.getContent();
            Map<Long, String> areas = StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                    .collect(Collectors.toMap(Area::getId, Area::getAreaName));
            Map<Long, String> accountGroups = StreamSupport
                    .stream(accountGroupRepository.findAll().spliterator(), false)
                    .collect(Collectors.toMap(AccountGroup::getId, AccountGroup::getName));
            Map<Long, String> ledgerToStateId = StreamSupport
                    .stream(addressRepository.findAll().spliterator(), false)
                    .filter(address -> address.getLedgerId() != null)
                    .filter(address -> address.getStateId() != null)
                    .collect(Collectors.toMap(Address::getLedgerId, Address::getStateId,
                            (a, b) -> a));
            Map<String, String> states = StreamSupport.stream(stateRepository.findAll().spliterator(), false)
                    .collect(Collectors.toMap(State::getStateCode, State::getStateName));
            Map<Long, String> gstinMap = StreamSupport.stream(gstDetailsRepository.findAll().spliterator(), false)
                    .collect(Collectors.toMap(GstInDetails::getLedgerId, GstInDetails::getGstinOrUin));

            List<LedgerSummaryItem> data = ledgers.stream()
                    .map(ledger -> new LedgerSummaryItem()
                            .id(ledger.getId())
                            .name(ledger.getName())
                            .state(states.getOrDefault(ledgerToStateId.get(ledger.getId()), ""))
                            .area(areas.getOrDefault(ledger.getDefaultAreaId(), ""))
                            .accountGroup(accountGroups.getOrDefault(ledger.getAccountGroupId(), ""))
                            .gstin(gstinMap.getOrDefault(ledger.getId(), ""))
                    ).toList();

            return new LedgerSummaryPageResponse()
                    .data(data)
                    .pagination(new PaginationMeta()
                            .page(ledgersPage.getNumber())
                            .size(ledgersPage.getSize())
                            .totalElements(ledgersPage.getTotalElements())
                            .totalPages(ledgersPage.getTotalPages()));
        }

        public LedgerResponse getLedgerResponseByGstIn(String gstIn) {
            if(!GstinUtils.isValid(gstIn)) {
                throw new LekhaiClientException("Invalid Gstin number");
            }

            GstVerificationResponseDto gstDetailDto = taxProGstClient.getGstDetails(gstIn)
                    .block();

            String pan = GstinUtils.extractPan(gstIn);
            String gstCode = GstinUtils.extractGstStateCode(gstIn);
            String stateCode = stateRepository.findByGstCode(gstCode)
                    .orElseThrow(() -> new RuntimeException(String.format("Can't find gstCode=[%s] in state repo", gstCode)))
                    .getStateCode();

            GstDetailsDto data = gstDetailDto.data();
            return LedgerUtils.mapToResponse(data, pan, stateCode);
        }

        // TODO: Make this read only for performance
        @ShopContextTransactional
        public LedgerBalanceResponse calcLedgerBalance(Long ledgerId) {
            Ledger ledger = ledgerRepository.findById(ledgerId)
                    .orElseThrow(() -> new LekhaiClientException("Invalid, Ledger Not found", HttpStatus.NOT_FOUND));
            BigDecimal openingBalance = ledger.getOpeningBalanceType().equals(AccountEntryType.DR)
                    ? ledger.getOpeningBalance()
                    : ledger.getOpeningBalance().negate();

            BigDecimal currentRunningBalance = voucherRepository.ledgerCurrentBalance(ledgerId);
            BigDecimal totalCurrentBalance = openingBalance.add(currentRunningBalance);
            return new LedgerBalanceResponse()
                    .ledgerId(ledger.getId())
                    .currentBalance(totalCurrentBalance)
                    .currentBalanceType(totalCurrentBalance.compareTo(BigDecimal.ZERO) < 0
                            ? AccountEntryType.CR
                            : AccountEntryType.DR);
        }
}
