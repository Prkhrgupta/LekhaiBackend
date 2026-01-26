package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.dto.LedgerRequest;
import in.lekhai.core.account_master.dto.LedgerResponse;
import in.lekhai.core.account_master.repository.LedgerRepository;
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
        Ledger ledger = new Ledger();
        ledger.setName(request.name());
        ledger.setLegalName(request.legalName());
        ledger.setAccountGroupId(request.accountGroupId());
        if (request.openingBalance() != null) {
            ledger.setOpeningBalance(request.openingBalance());
        }
        ledger.setOpeningBalanceType(request.openingBalanceType());
        ledger.setCreditLimit(request.creditLimit());
        ledger.setDefaultAreaId(request.defaultAreaId());
        ledger.setDefaultBrokerId(request.defaultBrokerId());
        ledger.setDefaultTransportId(request.defaultTransportId());
        ledger.setPan(request.pan());
        ledger.setAadhaar(request.aadhaar());
        ledger.setTan(request.tan());
        ledger.setEmail(request.email());
        ledger.setMsme(request.msme());
        ledger.setShopCode(request.shopCode());

        Ledger saved = ledgerRepository.save(ledger);
        return mapToResponse(saved);
    }

    public List<LedgerResponse> listLedgers() {
        return StreamSupport.stream(ledgerRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private LedgerResponse mapToResponse(Ledger ledger) {
        return new LedgerResponse(
                ledger.getId(),
                ledger.getName(),
                ledger.getLegalName(),
                ledger.getAccountGroupId(),
                ledger.getOpeningBalance(),
                ledger.getOpeningBalanceType(),
                ledger.getCreditLimit(),
                ledger.getDefaultAreaId(),
                ledger.getDefaultBrokerId(),
                ledger.getDefaultTransportId(),
                ledger.getPan(),
                ledger.getAadhaar(),
                ledger.getTan(),
                ledger.getEmail(),
                ledger.getMsme(),
                ledger.getActive(),
                ledger.getShopCode(),
                ledger.getCreatedAt());
    }
}
