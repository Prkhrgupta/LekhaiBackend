package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.repository.AddressRepository;
import in.lekhai.core.account_master.repository.GstDetailsRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static in.lekhai.core.account_master.utils.LedgerUtils.*;

@Service
public class LedgerService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final LedgerRepository ledgerRepository;
    private final GstDetailsRepository gstDetailsRepository;
    private final AddressRepository addressRepository;

    public LedgerService(
            LedgerRepository ledgerRepository,
            GstDetailsRepository gstDetailsRepository,
            AddressRepository addressRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.gstDetailsRepository = gstDetailsRepository;
        this.addressRepository = addressRepository;
    }

    @ShopTransactional
    public void createLedger(LedgerRequest request) {
        Ledger ledger = ledgerRepository.save(createLedgerObject(request));
        log.info("Saved ledger for shop {} :: ledger id {}", request.name(), ledger.getId());
        gstDetailsRepository.save(createGstInDetailsObject(request, ledger.getId()));
        log.info("Saved gst in details for shop {} :: {}", request.name(), ledger.getId());
        addressRepository.save(createAddressObject(request, ledger.getId()));
        log.info("Saved address details for shop {} :: {}", request.name(), ledger.getId());
    }

    @ShopTransactional
    public List<LedgerResponse> listLedgers() {
        return StreamSupport.stream(ledgerRepository.findAll().spliterator(), false)
                .map(LedgerUtils::mapToResponse)
                .toList();
    }
}
