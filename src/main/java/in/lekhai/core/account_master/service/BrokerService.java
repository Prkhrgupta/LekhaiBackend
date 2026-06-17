package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Broker;
import in.lekhai.core.account_master.repository.BrokerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class BrokerService {

    private final BrokerRepository brokerRepository;

    public BrokerService(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    @ShopContextTransactional
    public BrokerResponse createBroker(BrokerRequest request) {
        Broker broker = new Broker(request.getName(), request.getPhone());
        Broker saved = brokerRepository.save(broker);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public List<DropdownItem> listBrokers() {
        return StreamSupport.stream(brokerRepository.findAll().spliterator(), false)
                .map(broker -> new DropdownItem().id(broker.getId()).label(broker.getName()))
                .toList();
    }

    @ShopContextTransactional
    public BrokerSummaryPageResponse listBrokerSummaries(BrokerSearchableField searchableField, String query, Pageable pageable) {
        Page<Broker> brokersPage;
        if (query != null && !query.trim().isEmpty() && searchableField == BrokerSearchableField.NAME) {
            List<Broker> brokers = brokerRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
            long total = brokerRepository.countByNameContainingIgnoreCase(query.trim());
            brokersPage = new PageImpl<>(brokers, pageable, total);
        } else {
            brokersPage = brokerRepository.findAll(pageable);
        }

        List<BrokerResponse> data = brokersPage.getContent().stream()
                .map(broker -> new BrokerResponse()
                        .id(broker.getId())
                        .name(broker.getName())
                        .phone(broker.getPhone()))
                .toList();

        return new BrokerSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(brokersPage.getNumber())
                        .size(brokersPage.getSize())
                        .totalElements(brokersPage.getTotalElements())
                        .totalPages(brokersPage.getTotalPages()));
    }

    private BrokerResponse mapToResponse(Broker broker) {
        return new BrokerResponse()
                .id(broker.getId())
                .name(broker.getName())
                .phone(broker.getPhone());
    }
}
