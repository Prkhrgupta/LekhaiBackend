package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.*;
import in.lekhai.core.account_master.repository.*;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        private final LedgerSummaryRepository ledgerSummaryRepository;

        public LedgerService(
                        LedgerRepository ledgerRepository,
                        GstDetailsRepository gstDetailsRepository,
                        AddressRepository addressRepository,
                        AreaRepository areaRepository,
                        BrokerRepository brokerRepository,
                        TransportRepository transportRepository,
                        AccountGroupRepository accountGroupRepository,
                        StateRepository stateRepository,
                        LedgerSummaryRepository ledgerSummaryRepository) {
                this.ledgerRepository = ledgerRepository;
                this.gstDetailsRepository = gstDetailsRepository;
                this.addressRepository = addressRepository;
                this.areaRepository = areaRepository;
                this.brokerRepository = brokerRepository;
                this.transportRepository = transportRepository;
                this.accountGroupRepository = accountGroupRepository;
                this.stateRepository = stateRepository;
                this.ledgerSummaryRepository = ledgerSummaryRepository;
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

        /**
         * Returns a paginated, searchable, and sortable ledger summary.
         *
         * @param page        1-indexed page number
         * @param pageSize    number of items per page (max 100)
         * @param search      optional search query for partial case-insensitive matching
         * @param searchField optional field to restrict search to (name, state, area, accountGroup, gstin)
         * @param sortBy      column to sort by (id, name, state, area, accountGroup, gstin)
         * @param sortOrder   sort direction (asc or desc)
         * @return LedgerSummaryResponse with paginated data and pagination metadata
         */
        @ShopContextTransactional
        public LedgerSummaryResponse listLedgerSummaries(
                Integer page,
                Integer pageSize,
                String search,
                String searchField,
                String sortBy,
                String sortOrder
        ) {
            // Validate and normalize parameters
            int effectivePage = (page != null && page >= 1) ? page : 1;
            int effectivePageSize = (pageSize != null && pageSize >= 1) ? Math.min(pageSize, 100) : 50;
            String effectiveSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : "name";
            String effectiveSortOrder = (sortOrder != null && !sortOrder.isBlank()) ? sortOrder : "asc";

            // Count total matching items
            long totalItems = ledgerSummaryRepository.countSummaries(search, searchField);

            // Calculate pagination metadata
            int totalPages = (int) Math.ceil((double) totalItems / effectivePageSize);
            if (totalPages == 0) {
                totalPages = 1;
            }

            boolean hasNext = effectivePage < totalPages;
            boolean hasPrevious = effectivePage > 1;

            // Fetch the page of data
            int offset = (effectivePage - 1) * effectivePageSize;
            List<LedgerSummaryItem> data;

            if (effectivePage > totalPages && totalItems > 0) {
                // Page exceeds total pages — return empty data
                data = List.of();
            } else {
                data = ledgerSummaryRepository.findSummaries(
                        search, searchField, effectiveSortBy, effectiveSortOrder,
                        offset, effectivePageSize
                );
            }

            // Build columns (always returned)
            List<LedgerSummaryColumn> columns = List.of(
                    new LedgerSummaryColumn().name("ID")
                            .type("number")
                            .width(80),
                    new LedgerSummaryColumn().name("Name")
                            .type("text")
                            .width(150),
                    new LedgerSummaryColumn().name("State")
                            .type("text")
                            .width(120),
                    new LedgerSummaryColumn().name("Area")
                            .type("text")
                            .width(120),
                    new LedgerSummaryColumn().name("AccountGroup")
                            .type("text")
                            .width(160),
                    new LedgerSummaryColumn().name("GSTIN")
                            .type("text")
                            .width(150)
            );

            // Build pagination metadata
            PaginationMeta pagination = new PaginationMeta()
                    .page(effectivePage)
                    .pageSize(effectivePageSize)
                    .totalItems(totalItems)
                    .totalPages(totalPages)
                    .hasNext(hasNext)
                    .hasPrevious(hasPrevious);

            return new LedgerSummaryResponse()
                    .columns(columns)
                    .data(data)
                    .pagination(pagination);
        }
}
