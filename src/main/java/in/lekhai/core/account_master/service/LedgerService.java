package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.utils.LedgerUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public LedgerResponse createLedger(LedgerRequest request) {
//        Ledger ledger = createLedgerObject(request);
//        Ledger saved = ledgerRepository.save(ledger);
//        return mapToResponse(saved);
        return null;
    }

    public List<LedgerResponse> listLedgers() {
        return StreamSupport.stream(ledgerRepository.findAll().spliterator(), false)
                .map(LedgerUtils::mapToResponse)
                .toList();
    }
}
