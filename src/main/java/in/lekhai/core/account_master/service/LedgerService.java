package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Area;
import in.lekhai.core.account_master.domain.Broker;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.Transport;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.repository.*;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
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

    public LedgerService(
            LedgerRepository ledgerRepository,
            GstDetailsRepository gstDetailsRepository,
            AddressRepository addressRepository,
            AreaRepository areaRepository,
            BrokerRepository brokerRepository,
            TransportRepository transportRepository) {
        this.ledgerRepository = ledgerRepository;
        this.gstDetailsRepository = gstDetailsRepository;
        this.addressRepository = addressRepository;
        this.areaRepository = areaRepository;
        this.brokerRepository = brokerRepository;
        this.transportRepository = transportRepository;
    }

    @ShopTransactional
    public void createLedger(LedgerRequest request) {
        Ledger ledger = ledgerRepository.save(createLedgerObject(request));
        log.info("Saved ledger for shop {} :: ledger id {}", request.name(), ledger.getId());
        if (request.gstInDetailsPresent() && request.gstInDetails() != null) {
            gstDetailsRepository.save(createGstInDetailsObject(request.gstInDetails(), ledger.getId()));
            log.info("Saved gst in details for shop {} :: {}", request.name(), ledger.getId());
        }
        if (request.mailTo() != null) {
            addressRepository.save(createAddressObject(request, ledger.getId()));
            log.info("Saved address details for shop {} :: {}", request.name(), ledger.getId());
        }
    }

    @ShopTransactional
    public List<LedgerResponse> listLedgers() {
        List<Ledger> ledgers = StreamSupport.stream(ledgerRepository.findAll().spliterator(), false).toList();

        Map<Long, Area> areas = StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Area::getId, area -> area));
        Map<Long, Broker> brokers = StreamSupport.stream(brokerRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Broker::getId, broker -> broker));
        Map<Long, Transport> transports = StreamSupport.stream(transportRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Transport::getId, transport -> transport));

        return ledgers.stream()
                .map(ledger -> LedgerUtils.mapToResponse(
                        ledger,
                        areas.get(ledger.getDefaultAreaId()),
                        brokers.get(ledger.getDefaultBrokerId()),
                        transports.get(ledger.getDefaultTransportId())))
                .toList();
    }
}
