package in.lekhai.salesinvoice.service;

import in.lekhai.core.account_master.domain.Address;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.State;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.AddressRepository;
import in.lekhai.core.account_master.repository.GstDetailsRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.StateRepository;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.salesinvoice.service.PreparedInvoice.PartyInfo;
import in.lekhai.shop.context.model.ShopContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves the buyer snapshot and the seller/buyer states that decide intra- vs inter-state GST.
 */
@Component
class SalesInvoicePartyResolver {

    private static final List<String> ALLOWED_GROUPS =
            List.of("Sundry Debtors", "Sundry Debtors (Others)", "Cash in Hand");

    private final LedgerRepository ledgerRepository;
    private final AddressRepository addressRepository;
    private final GstDetailsRepository gstDetailsRepository;
    private final StateRepository stateRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final ShopsRepo shopsRepo;

    SalesInvoicePartyResolver(LedgerRepository ledgerRepository, AddressRepository addressRepository,
                              GstDetailsRepository gstDetailsRepository, StateRepository stateRepository,
                              AccountGroupRepository accountGroupRepository, ShopsRepo shopsRepo) {
        this.ledgerRepository = ledgerRepository;
        this.addressRepository = addressRepository;
        this.gstDetailsRepository = gstDetailsRepository;
        this.stateRepository = stateRepository;
        this.accountGroupRepository = accountGroupRepository;
        this.shopsRepo = shopsRepo;
    }

    PartyInfo resolveParty(Long ledgerId) {
        Ledger ledger = ledgerRepository.findById(ledgerId).orElseThrow(
                () -> SalesInvoiceErrors.notFound(String.format("Party ledger [%d] not found", ledgerId)));
        requireDebtorOrCash(ledger);
        Optional<Address> address = addressRepository.findByLedgerId(ledgerId);
        String stateGst = address.map(Address::getStateId).flatMap(stateRepository::findById)
                .map(State::getGstCode).orElse(null);
        return new PartyInfo(ledger.getId(), ledger.getName(), resolveGstin(ledger),
                address.map(this::formatAddress).orElse(null), stateGst);
    }

    /**
     * Seller GST state code of the current shop, compared with place of supply for intra/inter-state.
     */
    String resolveSellerGstCode() {
        Shops shop = shopsRepo.findByShopCode(ShopContext.getShopCode()).orElseThrow(
                () -> SalesInvoiceErrors.unprocessable("Shop not found for the current context"));
        if (shop.getStateCode() == null) {
            throw SalesInvoiceErrors.unprocessable("Shop state is not configured; set the seller state first");
        }
        return stateRepository.findById(shop.getStateCode()).map(State::getGstCode).orElseThrow(
                () -> SalesInvoiceErrors.unprocessable("Shop state is invalid: " + shop.getStateCode()));
    }

    void requireKnownGstState(String gstCode) {
        if (gstCode == null || stateRepository.findByGstCode(gstCode).isEmpty()) {
            throw SalesInvoiceErrors.badRequest("Unknown place_of_supply state code: " + gstCode);
        }
    }

    private void requireDebtorOrCash(Ledger ledger) {
        List<Long> roots = ALLOWED_GROUPS.stream()
                .map(accountGroupRepository::findByNameIgnoreCase)
                .flatMap(Optional::stream)
                .map(group -> group.getId())
                .toList();
        Set<Long> allowed = roots.isEmpty() ? Set.of() : accountGroupRepository.findHierarchyIds(roots);
        if (!allowed.contains(ledger.getAccountGroupId())) {
            throw SalesInvoiceErrors.badRequest(
                    String.format("Party ledger [%d] must be a Sundry Debtor or Cash ledger", ledger.getId()));
        }
    }

    private String resolveGstin(Ledger ledger) {
        return gstDetailsRepository.findByLedgerId(ledger.getId())
                .map(details -> details.getGstinOrUin())
                .orElse(ledger.getGstInNumber());
    }

    private String formatAddress(Address address) {
        return java.util.stream.Stream.of(address.getAddressLine1(), address.getAddressLine2(),
                        address.getAddressLine3(), address.getCity(), address.getPincode())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
    }
}
