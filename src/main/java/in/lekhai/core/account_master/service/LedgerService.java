package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.utils.LedgerUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static in.lekhai.core.account_master.utils.LedgerUtils.createLedgerObject;
import static in.lekhai.core.account_master.utils.LedgerUtils.mapToResponse;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @ShopTransactional
    public LedgerResponse createLedger(LedgerRequest request) {
        Ledger ledger = createLedgerObject(request);
        Ledger saved = ledgerRepository.save(ledger);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public List<LedgerResponse> listLedgers() {
        return StreamSupport.stream(ledgerRepository.findAll().spliterator(), false)
                .map(LedgerUtils::mapToResponse)
                .toList();
    }
}
