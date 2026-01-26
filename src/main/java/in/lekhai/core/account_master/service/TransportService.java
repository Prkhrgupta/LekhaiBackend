package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Transport;
import in.lekhai.core.account_master.dto.TransportRequest;
import in.lekhai.core.account_master.dto.TransportResponse;
import in.lekhai.core.account_master.repository.TransportRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    @ShopTransactional
    public TransportResponse createTransport(TransportRequest request) {
        Transport transport = new Transport(
                request.name(),
                request.phone(),
                request.gstNo());
        Transport saved = transportRepository.save(transport);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public List<TransportResponse> listTransports() {
        return StreamSupport.stream(transportRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private TransportResponse mapToResponse(Transport transport) {
        return new TransportResponse(
                transport.getId(),
                transport.getName(),
                transport.getPhone(),
                transport.getGstNo(),
                transport.getShopCode(),
                transport.getCreatedAt());
    }
}
