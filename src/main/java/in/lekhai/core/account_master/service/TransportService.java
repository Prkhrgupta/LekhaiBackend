package in.lekhai.core.account_master.service;

import in.lekhai.accountmaster.transport.dto.TransportRequest;
import in.lekhai.accountmaster.transport.dto.TransportResponse;
import in.lekhai.core.account_master.domain.Transport;
import in.lekhai.core.account_master.repository.TransportRepository;
import in.lekhai.core.account_master.utils.DateUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    @ShopContextTransactional
    public TransportResponse createTransport(TransportRequest request) {
        Transport transport = new Transport(
                request.getName(),
                request.getPhone(),
                request.getGstNo());
        Transport saved = transportRepository.save(transport);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public List<TransportResponse> listTransports() {
        return StreamSupport.stream(transportRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private TransportResponse mapToResponse(Transport transport) {
        return new TransportResponse()
                .id(transport.getId())
                .name(transport.getName())
                .phone(transport.getPhone())
                .gstNo(transport.getGstNo())
                .createdAt(DateUtils.getCreatedAt(transport.getCreatedAt()));
    }
}
