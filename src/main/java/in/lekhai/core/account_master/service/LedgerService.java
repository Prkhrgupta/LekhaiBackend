package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.*;
import in.lekhai.accountmaster.ledger.dto.LedgerRequest;
import in.lekhai.accountmaster.ledger.dto.LedgerResponse;
import in.lekhai.core.account_master.repository.*;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

        public LedgerService(
                        LedgerRepository ledgerRepository,
                        GstDetailsRepository gstDetailsRepository,
                        AddressRepository addressRepository,
                        AreaRepository areaRepository,
                        BrokerRepository brokerRepository,
                        TransportRepository transportRepository,
                        AccountGroupRepository accountGroupRepository,
                        StateRepository stateRepository) {
                this.ledgerRepository = ledgerRepository;
                this.gstDetailsRepository = gstDetailsRepository;
                this.addressRepository = addressRepository;
                this.areaRepository = areaRepository;
                this.brokerRepository = brokerRepository;
                this.transportRepository = transportRepository;
                this.accountGroupRepository = accountGroupRepository;
                this.stateRepository = stateRepository;
        }

        @ShopContextTransactional
        public LedgerResponse createLedger(LedgerRequest request) {
                Ledger ledger = ledgerRepository.save(createLedgerObject(request));
                log.info("Saved ledger for shop {} :: ledger id {}", request.getName(), ledger.getId());
                if (request.getGstInDetailsPresent() && request.getGstInDetails() != null) {
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
                        address.setDistance(request.getDistance());
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
        public List<LedgerResponse> listLedgers() {
                List<Ledger> ledgers = StreamSupport.stream(ledgerRepository.findAll().spliterator(), false).toList();

                Map<Long, Area> areas = StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(Area::getId, area -> area));
                Map<Long, Broker> brokers = StreamSupport.stream(brokerRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(Broker::getId, broker -> broker));
                Map<Long, Transport> transports = StreamSupport
                                .stream(transportRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(Transport::getId, transport -> transport));
                Map<Long, AccountGroup> accountGroups = StreamSupport
                                .stream(accountGroupRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(AccountGroup::getId, accountGroup -> accountGroup));
                Map<Long, Address> addressMap = StreamSupport
                                .stream(addressRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(Address::getLedgerId, addr -> addr));
                Map<Long, GstInDetails> gstInDetailsMap = StreamSupport
                                .stream(gstDetailsRepository.findAll().spliterator(), false)
                                .collect(Collectors.toMap(GstInDetails::getLedgerId, gst -> gst));

                return ledgers.stream()
                                .map(ledger -> LedgerUtils.mapToResponse(
                                                ledger,
                                                areas.get(ledger.getDefaultAreaId()),
                                                brokers.get(ledger.getDefaultBrokerId()),
                                                transports.get(ledger.getDefaultTransportId()),
                                                accountGroups.get(ledger.getAccountGroupId()),
                                                gstInDetailsMap.get(ledger.getId()),
                                                addressMap.get(ledger.getId())))
                                .toList();
        }

//        @ShopContextTransactional
//        public LedgerSummaryResponse listLedgerSummaries() {
//                List<Ledger> ledgers = StreamSupport.stream(ledgerRepository.findAll().spliterator(), false).toList();
//                Map<Long, String> areas = StreamSupport.stream(areaRepository.findAll().spliterator(), false)
//                                .collect(Collectors.toMap(Area::getId, Area::getAreaName));
//                Map<Long, String> accountGroups = StreamSupport
//                                .stream(accountGroupRepository.findAll().spliterator(), false)
//                                .collect(Collectors.toMap(AccountGroup::getId, AccountGroup::getName));
//                Map<Long, String> ledgerToStateId = StreamSupport
//                                .stream(addressRepository.findAll().spliterator(), false)
//                                .filter(address -> address.getLedgerId() != null)
//                                .filter(address -> address.getStateId() != null)
//                                .collect(Collectors.toMap(Address::getLedgerId, Address::getStateId,
//                                                (a, b) -> a));
//                Map<String, String> states = StreamSupport.stream(stateRepository.findAll().spliterator(), false)
//                                .collect(Collectors.toMap(State::getStateCode, State::getStateName));
//                Map<Long, String> gstinMap = StreamSupport.stream(gstDetailsRepository.findAll().spliterator(), false)
//                                .collect(Collectors.toMap(GstInDetails::getLedgerId, GstInDetails::getGstinOrUin));
//
//                List<LedgerSummaryResponse.Column> columns = List.of(
//                                new LedgerSummaryResponse.Column("ID", "number", 80),
//                                new LedgerSummaryResponse.Column("Name", "text", 150),
//                                new LedgerSummaryResponse.Column("State", "text", 120),
//                                new LedgerSummaryResponse.Column("Area", "text", 120),
//                                new LedgerSummaryResponse.Column("AccountGroup", "text", 160),
//                                new LedgerSummaryResponse.Column("GSTIN", "text", 150));
//
//                List<LedgerSummaryResponse.Item> data = ledgers.stream()
//                                .map(ledger -> new LedgerSummaryResponse.Item(
//                                                ledger.getId(),
//                                                ledger.getName(),
//                                                states.getOrDefault(ledgerToStateId.get(ledger.getId()), ""),
//                                                areas.getOrDefault(ledger.getDefaultAreaId(), ""),
//                                                accountGroups.getOrDefault(ledger.getAccountGroupId(), ""),
//                                                gstinMap.getOrDefault(ledger.getId(), "")))
//                                .toList();
//
//                return new LedgerSummaryResponse(columns, data);
//        }
}
