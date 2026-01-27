package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.repository.GstDetailsRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static in.lekhai.core.account_master.utils.LedgerUtils.*;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final GstDetailsRepository gstDetailsRepository;

    public LedgerService(
            LedgerRepository ledgerRepository,
            GstDetailsRepository gstDetailsRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.gstDetailsRepository = gstDetailsRepository;
    }

    @ShopTransactional
    public void createLedger(LedgerRequest request) {
        Ledger ledger = ledgerRepository.save(createLedgerObject(request));
        gstDetailsRepository.save(createGstInDetailsObject(request, ledger.getId()));
    }

    @ShopTransactional
    public List<LedgerResponse> listLedgers() {
        return StreamSupport.stream(ledgerRepository.findAll().spliterator(), false)
                .map(LedgerUtils::mapToResponse)
                .toList();
    }
}
