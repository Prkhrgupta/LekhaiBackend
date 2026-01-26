package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Broker;
import in.lekhai.core.account_master.dto.BrokerRequest;
import in.lekhai.core.account_master.dto.BrokerResponse;
import in.lekhai.core.account_master.repository.BrokerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class BrokerService {

    private final BrokerRepository brokerRepository;

    public BrokerService(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    @ShopTransactional
    public BrokerResponse createBroker(BrokerRequest request) {
        Broker broker = new Broker(
                request.name(),
                request.phone());
        Broker saved = brokerRepository.save(broker);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public List<BrokerResponse> listBrokers() {
        return StreamSupport.stream(brokerRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private BrokerResponse mapToResponse(Broker broker) {
        return new BrokerResponse(
                broker.getId(),
                broker.getName(),
                broker.getPhone(),
                broker.getShopCode(),
                broker.getCreatedAt());
    }
}
